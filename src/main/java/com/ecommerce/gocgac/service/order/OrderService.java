package com.ecommerce.gocgac.service.order;

import com.ecommerce.gocgac.common.response.PageResponse;
import com.ecommerce.gocgac.dto.order.CheckoutRequest;
import com.ecommerce.gocgac.dto.order.OrderItemResponse;
import com.ecommerce.gocgac.dto.order.OrderResponse;
import com.ecommerce.gocgac.entity.*;
import com.ecommerce.gocgac.entity.enums.OrderStatus;
import com.ecommerce.gocgac.entity.enums.PaymentStatus;
import com.ecommerce.gocgac.entity.enums.ProductStatus;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.UserType;
import com.ecommerce.gocgac.exception.BadRequestException;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.*;
import com.ecommerce.gocgac.service.affiliate.AffiliateService;
import com.ecommerce.gocgac.service.loyalty.LoyaltyService;
import com.ecommerce.gocgac.service.notification.NotificationService;
import com.ecommerce.gocgac.service.stock.StockService;
import com.ecommerce.gocgac.service.voucher.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dịch vụ đơn hàng (Sprint 2 - M09 + M15).
 *
 * <p>Checkout tách giỏ thành <b>mỗi gian hàng một đơn</b> (vì {@code Order} không có
 * store_id, mỗi đơn chứa item của đúng một store). Tồn kho được giữ nguyên tử khi đặt,
 * hoàn lại khi hủy, và chốt xuất khi giao.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final StockService stockService;
    private final VoucherService voucherService;
    private final LoyaltyService loyaltyService;
    private final AffiliateService affiliateService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final CooperativeRepository cooperativeRepository;

    private static final String REF_ORDER = "ORDER";
    private static final String VARIANT_ACTIVE = "active";

    // ========== Checkout ==========

    /**
     * Đặt hàng từ giỏ. Trả về danh sách đơn (mỗi gian hàng một đơn).
     */
    @Transactional
    public List<OrderResponse> checkout(Long userId, CheckoutRequest req) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Giỏ hàng trống"));
        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống");
        }

        // Gom item theo gian hàng
        Map<Long, List<PreparedItem>> byStore = new LinkedHashMap<>();
        for (CartItem ci : cartItems) {
            if (ci.getVariantId() == null) {
                throw new BusinessException("Sản phẩm trong giỏ thiếu thông tin SKU");
            }
            ProductVariant variant = variantRepository.findById(ci.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm (SKU) không tồn tại"));
            Product product = productRepository.findById(variant.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
            validatePurchasable(product, variant);

            BigDecimal price = variant.getPrice() != null ? variant.getPrice() : ci.getPrice();
            if (price == null) {
                throw new BusinessException("Sản phẩm " + product.getProductName() + " chưa được định giá");
            }
            byStore.computeIfAbsent(product.getStoreId(), k -> new ArrayList<>())
                .add(new PreparedItem(product, variant, ci.getQuantity(), price));
        }

        List<OrderResponse> created = new ArrayList<>();
        boolean first = true;
        for (Map.Entry<Long, List<PreparedItem>> entry : byStore.entrySet()) {
            // Điểm thưởng chỉ áp cho đơn đầu tiên (tránh chia điểm giữa nhiều gian hàng)
            int pointsToUse = first ? nz(req.getLoyaltyPointsToUse()) : 0;
            created.add(createStoreOrder(userId, req, entry.getKey(), entry.getValue(), pointsToUse));
            first = false;
        }

        // Dọn giỏ sau khi đặt thành công
        cartItemRepository.deleteAllByCartId(cart.getId());
        log.info("User {} checked out {} order(s)", userId, created.size());
        return created;
    }

    private OrderResponse createStoreOrder(Long userId, CheckoutRequest req, Long storeId,
                                           List<PreparedItem> items, int loyaltyPointsToUse) {
        BigDecimal subtotal = items.stream()
            .map(pi -> pi.price.multiply(BigDecimal.valueOf(pi.quantity)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = BigDecimal.ZERO;     // Sprint 4 (vận chuyển)

        // Sprint 5: áp voucher cho đơn của gian hàng này (nếu mã thuộc store này)
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        VoucherService.VoucherDiscount appliedVoucher = null;
        if (StringUtils.hasText(req.getVoucherCode())) {
            appliedVoucher = voucherService.previewDiscount(
                req.getVoucherCode(), userId, storeId, subtotal, shippingFee);
            if (appliedVoucher != null) {
                voucherDiscount = appliedVoucher.amount();
            }
        }

        // Sprint 6: đổi điểm thưởng (giảm trên phần còn lại sau voucher)
        BigDecimal loyaltyDiscount = BigDecimal.ZERO;
        int loyaltyPointsUsed = 0;
        if (loyaltyPointsToUse > 0) {
            BigDecimal redeemableBase = subtotal.subtract(voucherDiscount).max(BigDecimal.ZERO);
            LoyaltyService.RedeemResult redeem =
                loyaltyService.previewRedeem(userId, loyaltyPointsToUse, redeemableBase);
            loyaltyPointsUsed = redeem.pointsUsed();
            loyaltyDiscount = redeem.discount();
        }

        BigDecimal total = subtotal.add(shippingFee).subtract(voucherDiscount).subtract(loyaltyDiscount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setUserId(userId);
        order.setRecipientName(req.getRecipientName());
        order.setRecipientPhone(req.getRecipientPhone());
        order.setRecipientEmail(req.getRecipientEmail());
        order.setShippingAddress(req.getShippingAddress());
        order.setShippingProvince(req.getShippingProvince());
        order.setShippingDistrict(req.getShippingDistrict());
        order.setShippingWard(req.getShippingWard());
        order.setPaymentMethod(req.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setVoucherDiscount(voucherDiscount);
        order.setLoyaltyPointsUsed(loyaltyPointsUsed);
        order.setLoyaltyDiscount(loyaltyDiscount);
        order.setTotalAmount(total);
        order.setNotes(req.getNotes());
        order = orderRepository.save(order);

        for (PreparedItem pi : items) {
            // Giữ tồn nguyên tử (khóa bi quan bên trong StockService) — chống oversell
            stockService.reserve(pi.variant.getId(), pi.quantity, order.getId(), userId);

            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setStoreId(storeId);
            oi.setProductId(pi.product.getId());
            oi.setVariantId(pi.variant.getId());
            oi.setProductName(pi.product.getProductName());
            oi.setProductImage(pi.variant.getImageUrl());
            oi.setQuantity(pi.quantity);
            oi.setUnitPrice(pi.price);
            oi.setDiscountAmount(BigDecimal.ZERO);
            oi.setSubtotal(pi.price.multiply(BigDecimal.valueOf(pi.quantity)));
            orderItemRepository.save(oi);
        }

        // Ghi nhận sử dụng voucher (sau khi đơn đã có id)
        if (appliedVoucher != null) {
            voucherService.commitUsage(appliedVoucher.voucherId(), appliedVoucher.code(),
                userId, order.getId(), appliedVoucher.amount());
        }

        // Trừ điểm thưởng đã đổi
        if (loyaltyPointsUsed > 0) {
            loyaltyService.commitRedeem(userId, loyaltyPointsUsed, order.getId());
        }

        // Sprint 10: quy gán đơn cho đối tác tiếp thị liên kết (last-click) + tạo hoa hồng
        affiliateService.attributeOrder(order, userId);

        saveHistory(order.getId(), null, OrderStatus.PENDING, userId, "Khởi tạo đơn hàng");
        notificationService.notify(userId, "ORDER", "Đặt hàng thành công",
            "Đơn hàng " + order.getOrderCode() + " đã được tạo và đang chờ xử lý.",
            REF_ORDER, order.getId());

        return buildResponse(order);
    }

    // ========== Customer ==========

    public PageResponse<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        return PageResponse.from(
            orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::buildResponse));
    }

    public OrderResponse getMyOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        return buildResponse(order);
    }

    @Transactional
    public OrderResponse cancelMyOrder(Long userId, Long orderId, String reason) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        applyStatusChange(order, OrderStatus.CANCELLED, reason, userId);
        return buildResponse(order);
    }

    // ========== Seller ==========

    public PageResponse<OrderResponse> getStoreOrders(Long sellerUserId, Pageable pageable) {
        Long storeId = resolveSellerStoreId(sellerUserId);
        List<Long> orderIds = orderItemRepository.findDistinctOrderIdsByStoreId(storeId);
        if (orderIds.isEmpty()) {
            return PageResponse.from(Page.<OrderResponse>empty(pageable));
        }
        Page<Order> page = orderRepository.findByIdInOrderByCreatedAtDesc(orderIds, pageable);
        return PageResponse.from(page.map(this::buildResponse));
    }

    public OrderResponse getStoreOrder(Long sellerUserId, Long orderId) {
        Long storeId = resolveSellerStoreId(sellerUserId);
        ensureStoreOwnsOrder(orderId, storeId);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        return buildResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long sellerUserId, Long orderId, OrderStatus newStatus, String note) {
        Long storeId = resolveSellerStoreId(sellerUserId);
        ensureStoreOwnsOrder(orderId, storeId);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        applyStatusChange(order, newStatus, note, sellerUserId);
        return buildResponse(order);
    }

    // ========== Core status machine ==========

    private void applyStatusChange(Order order, OrderStatus to, String note, Long actorUserId) {
        OrderStatus from = order.getOrderStatus();
        if (from == to) {
            throw new BadRequestException("Đơn hàng đã ở trạng thái " + to);
        }
        if (!isValidTransition(from, to)) {
            throw new BusinessException("Không thể chuyển trạng thái đơn từ " + from + " sang " + to);
        }

        // Tác động tồn kho theo trạng thái
        switch (to) {
            case CANCELLED -> applyToItems(order, (variantId, qty) ->
                stockService.release(variantId, qty, order.getId(), actorUserId));
            case DELIVERED -> applyToItems(order, (variantId, qty) ->
                stockService.commit(variantId, qty, order.getId(), actorUserId));
            case RETURNED -> applyToItems(order, (variantId, qty) ->
                stockService.restock(variantId, qty, order.getId(), actorUserId));
            default -> { /* các trạng thái khác không tác động tồn */ }
        }

        // Sprint 10: đơn hủy/hoàn → hủy hoa hồng tiếp thị liên kết
        if (to == OrderStatus.CANCELLED || to == OrderStatus.RETURNED) {
            affiliateService.voidCommissionForOrder(order.getId());
        }

        LocalDateTime now = LocalDateTime.now();
        order.setOrderStatus(to);
        switch (to) {
            case CONFIRMED -> order.setConfirmedAt(now);
            case SHIPPING -> order.setShippedAt(now);
            case DELIVERED -> order.setDeliveredAt(now);
            case CANCELLED -> {
                order.setCancelledAt(now);
                order.setCancellationReason(note);
            }
            default -> { /* không có mốc thời gian riêng */ }
        }
        orderRepository.save(order);

        // Sprint 6: đơn giao thành công → tích điểm & tính lại hạng thành viên
        if (to == OrderStatus.DELIVERED) {
            loyaltyService.earnForOrder(order.getUserId(), order.getId(), order.getTotalAmount());
            loyaltyService.recalculateRank(order.getUserId());
        }

        saveHistory(order.getId(), from, to, actorUserId, note);
        notificationService.notify(order.getUserId(), "ORDER", "Cập nhật đơn hàng",
            "Đơn hàng " + order.getOrderCode() + " chuyển sang trạng thái " + to + ".",
            REF_ORDER, order.getId());
    }

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING -> to == OrderStatus.SHIPPING || to == OrderStatus.CANCELLED;
            case SHIPPING -> to == OrderStatus.DELIVERED;
            case DELIVERED -> to == OrderStatus.RETURNED;
            default -> false; // CANCELLED, RETURNED là trạng thái cuối
        };
    }

    private void applyToItems(Order order, StockAction action) {
        for (OrderItem item : orderItemRepository.findByOrderId(order.getId())) {
            if (item.getVariantId() != null) {
                action.apply(item.getVariantId(), item.getQuantity());
            }
        }
    }

    @FunctionalInterface
    private interface StockAction {
        void apply(Long variantId, int quantity);
    }

    // ========== Helpers ==========

    private void validatePurchasable(Product product, ProductVariant variant) {
        if (product.getDeletedAt() != null
                || product.getStatus() != ProductStatus.ACTIVE
                || product.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new BusinessException("Sản phẩm " + product.getProductName() + " hiện không khả dụng để mua");
        }
        if (!VARIANT_ACTIVE.equalsIgnoreCase(variant.getStatus())) {
            throw new BusinessException("Biến thể sản phẩm (SKU) hiện không khả dụng");
        }
    }

    private String generateOrderCode() {
        String code;
        do {
            code = "ORD-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        } while (orderRepository.existsByOrderCode(code));
        return code;
    }

    private void saveHistory(Long orderId, OrderStatus from, OrderStatus to, Long actorUserId, String note) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(orderId);
        history.setFromStatus(from != null ? from.name() : null);
        history.setToStatus(to.name());
        history.setNote(note);
        history.setCreatedBy(actorUserId);
        statusHistoryRepository.save(history);
    }

    private OrderResponse buildResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        Long storeId = items.isEmpty() ? null : items.get(0).getStoreId();
        List<OrderItemResponse> itemResponses = items.stream()
            .map(OrderItemResponse::from)
            .toList();
        return OrderResponse.from(order, itemResponses, storeId);
    }

    private int nz(Integer value) {
        return value != null ? value : 0;
    }

    private void ensureStoreOwnsOrder(Long orderId, Long storeId) {
        if (!orderItemRepository.existsByOrderIdAndStoreId(orderId, storeId)) {
            throw new ResourceNotFoundException("Đơn hàng không thuộc gian hàng của bạn");
        }
    }

    private Long resolveSellerStoreId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        if (user.getUserType() == UserType.COOPERATIVE_MANAGER) {
            Cooperative cooperative = cooperativeRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Bạn chưa có HTX"));
            return storeRepository.findByCooperativeId(cooperative.getId())
                .orElseThrow(() -> new BusinessException("HTX của bạn chưa có gian hàng"))
                .getId();
        } else if (user.getUserType() == UserType.SELLER) {
            return storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new BusinessException("Bạn chưa có gian hàng"))
                .getId();
        }
        throw new BusinessException("Chỉ SELLER hoặc COOPERATIVE_MANAGER mới quản lý được đơn hàng gian hàng");
    }

    /** Gói dữ liệu đã chuẩn bị cho 1 dòng đơn trong lúc checkout. */
    private static class PreparedItem {
        final Product product;
        final ProductVariant variant;
        final int quantity;
        final BigDecimal price;

        PreparedItem(Product product, ProductVariant variant, int quantity, BigDecimal price) {
            this.product = product;
            this.variant = variant;
            this.quantity = quantity;
            this.price = price;
        }
    }
}
