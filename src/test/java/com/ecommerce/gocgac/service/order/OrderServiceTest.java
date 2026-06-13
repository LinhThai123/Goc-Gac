package com.ecommerce.gocgac.service.order;

import com.ecommerce.gocgac.dto.order.CheckoutRequest;
import com.ecommerce.gocgac.dto.order.OrderResponse;
import com.ecommerce.gocgac.entity.*;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.OrderStatus;
import com.ecommerce.gocgac.entity.enums.PaymentMethod;
import com.ecommerce.gocgac.entity.enums.ProductStatus;
import com.ecommerce.gocgac.entity.enums.UserType;
import com.ecommerce.gocgac.exception.BadRequestException;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.repository.*;
import com.ecommerce.gocgac.service.loyalty.LoyaltyService;
import com.ecommerce.gocgac.service.notification.NotificationService;
import com.ecommerce.gocgac.service.stock.StockService;
import com.ecommerce.gocgac.service.voucher.VoucherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho {@link OrderService} — thuần Mockito.
 * Tập trung vào các hành vi rủi ro cao: tách đơn theo gian hàng, giữ tồn khi đặt,
 * hoàn tồn khi hủy, và máy trạng thái đơn.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderStatusHistoryRepository statusHistoryRepository;
    @Mock private StockService stockService;
    @Mock private VoucherService voucherService;
    @Mock private LoyaltyService loyaltyService;
    @Mock private NotificationService notificationService;
    @Mock private UserRepository userRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private CooperativeRepository cooperativeRepository;

    @InjectMocks private OrderService orderService;

    private static final Long USER_ID = 7L;
    private static final Long CART_ID = 1L;
    private static final Long VARIANT_ID = 10L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long STORE_ID = 1L;
    private static final Long ORDER_ID = 500L;

    private Cart cart;
    private Product product;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(CART_ID);
        cart.setUserId(USER_ID);

        product = new Product();
        product.setId(PRODUCT_ID);
        product.setStoreId(STORE_ID);
        product.setProductName("Mật ong OCOP");
        product.setStatus(ProductStatus.ACTIVE);
        product.setApprovalStatus(ApprovalStatus.APPROVED);

        variant = new ProductVariant();
        variant.setId(VARIANT_ID);
        variant.setProductId(PRODUCT_ID);
        variant.setSku("SKU-MO-500");
        variant.setPrice(new BigDecimal("50000"));
        variant.setStockQuantity(5);
        variant.setReservedQuantity(0);
        variant.setAvailableQuantity(5);
        variant.setStatus("active");
    }

    private CartItem cartItem(int qty) {
        CartItem ci = new CartItem();
        ci.setId(9L);
        ci.setCartId(CART_ID);
        ci.setProductId(PRODUCT_ID);
        ci.setVariantId(VARIANT_ID);
        ci.setQuantity(qty);
        ci.setPrice(new BigDecimal("50000"));
        return ci;
    }

    private OrderItem orderItem(int qty) {
        OrderItem oi = new OrderItem();
        oi.setId(1L);
        oi.setOrderId(ORDER_ID);
        oi.setStoreId(STORE_ID);
        oi.setProductId(PRODUCT_ID);
        oi.setVariantId(VARIANT_ID);
        oi.setProductName("Mật ong OCOP");
        oi.setQuantity(qty);
        oi.setUnitPrice(new BigDecimal("50000"));
        oi.setSubtotal(new BigDecimal("50000").multiply(BigDecimal.valueOf(qty)));
        return oi;
    }

    private CheckoutRequest checkoutRequest() {
        CheckoutRequest req = new CheckoutRequest();
        req.setRecipientName("Nguyễn Văn A");
        req.setRecipientPhone("0900000000");
        req.setShippingAddress("123 Đường ABC");
        req.setPaymentMethod(PaymentMethod.COD);
        return req;
    }

    private Order pendingOrder() {
        Order o = new Order();
        o.setId(ORDER_ID);
        o.setOrderCode("ORD-TEST");
        o.setUserId(USER_ID);
        o.setOrderStatus(OrderStatus.PENDING);
        o.setSubtotal(new BigDecimal("100000"));
        o.setTotalAmount(new BigDecimal("100000"));
        return o;
    }

    @Nested
    @DisplayName("checkout")
    class Checkout {

        @Test
        @DisplayName("Đặt hàng thành công: tạo đơn, giữ tồn, dọn giỏ")
        void checkout_success() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findAllByCartId(CART_ID)).thenReturn(List.of(cartItem(2)));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
            when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                if (o.getId() == null) o.setId(ORDER_ID);
                return o;
            });
            when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(orderItem(2)));

            List<OrderResponse> result = orderService.checkout(USER_ID, checkoutRequest());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSubtotal()).isEqualByComparingTo("100000");
            assertThat(result.get(0).getStoreId()).isEqualTo(STORE_ID);
            assertThat(result.get(0).getOrderStatus()).isEqualTo(OrderStatus.PENDING);

            // giữ tồn đúng số lượng
            verify(stockService).reserve(VARIANT_ID, 2, ORDER_ID, USER_ID);
            // dọn giỏ sau khi đặt
            verify(cartItemRepository).deleteAllByCartId(CART_ID);
            // ghi lịch sử + thông báo
            verify(statusHistoryRepository).save(any());
            verify(notificationService).notify(eq(USER_ID), eq("ORDER"), anyString(), anyString(), anyString(), eq(ORDER_ID));
        }

        @Test
        @DisplayName("Giỏ rỗng → BadRequestException, không tạo đơn")
        void checkout_emptyCart_throws() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findAllByCartId(CART_ID)).thenReturn(List.of());

            assertThatThrownBy(() -> orderService.checkout(USER_ID, checkoutRequest()))
                .isInstanceOf(BadRequestException.class);

            verify(orderRepository, never()).save(any());
            verify(stockService, never()).reserve(anyLong(), anyInt(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("Đặt hàng có voucher: trừ giảm giá, ghi nhận sử dụng")
        void checkout_withVoucher_appliesDiscount() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findAllByCartId(CART_ID)).thenReturn(List.of(cartItem(2)));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
            when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                if (o.getId() == null) o.setId(ORDER_ID);
                return o;
            });
            when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(orderItem(2)));
            when(voucherService.previewDiscount(eq("SALE10"), eq(USER_ID), eq(STORE_ID),
                    any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(new VoucherService.VoucherDiscount(55L, "SALE10", new BigDecimal("10000")));

            CheckoutRequest req = checkoutRequest();
            req.setVoucherCode("SALE10");
            List<OrderResponse> result = orderService.checkout(USER_ID, req);

            assertThat(result.get(0).getVoucherDiscount()).isEqualByComparingTo("10000");
            assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("90000"); // 100.000 - 10.000
            verify(voucherService).commitUsage(eq(55L), eq("SALE10"), eq(USER_ID), eq(ORDER_ID), any(BigDecimal.class));
        }

        @Test
        @DisplayName("Đặt hàng đổi điểm: trừ giảm giá, ghi nhận trừ điểm")
        void checkout_withLoyaltyPoints_appliesDiscount() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findAllByCartId(CART_ID)).thenReturn(List.of(cartItem(2)));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
            when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                if (o.getId() == null) o.setId(ORDER_ID);
                return o;
            });
            when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(orderItem(2)));
            when(loyaltyService.previewRedeem(eq(USER_ID), eq(5), any(BigDecimal.class)))
                .thenReturn(new LoyaltyService.RedeemResult(5, new BigDecimal("5000")));

            CheckoutRequest req = checkoutRequest();
            req.setLoyaltyPointsToUse(5);
            List<OrderResponse> result = orderService.checkout(USER_ID, req);

            assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("95000"); // 100.000 - 5.000
            verify(loyaltyService).commitRedeem(USER_ID, 5, ORDER_ID);
        }

        @Test
        @DisplayName("Sản phẩm chưa duyệt → BusinessException")
        void checkout_productNotApproved_throws() {
            product.setApprovalStatus(ApprovalStatus.PENDING);
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findAllByCartId(CART_ID)).thenReturn(List.of(cartItem(1)));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> orderService.checkout(USER_ID, checkoutRequest()))
                .isInstanceOf(BusinessException.class);

            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("cancelMyOrder")
    class CancelOrder {

        @Test
        @DisplayName("Hủy đơn PENDING → hoàn tồn + trạng thái CANCELLED")
        void cancel_pending_releasesStock() {
            Order order = pendingOrder();
            when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
            when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(orderItem(2)));

            orderService.cancelMyOrder(USER_ID, ORDER_ID, "Đổi ý");

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getCancellationReason()).isEqualTo("Đổi ý");
            verify(stockService).release(VARIANT_ID, 2, ORDER_ID, USER_ID);
        }

        @Test
        @DisplayName("Hủy đơn đã giao → BusinessException (sai chuyển trạng thái)")
        void cancel_delivered_throws() {
            Order order = pendingOrder();
            order.setOrderStatus(OrderStatus.DELIVERED);
            when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelMyOrder(USER_ID, ORDER_ID, "Đổi ý"))
                .isInstanceOf(BusinessException.class);

            verify(stockService, never()).release(anyLong(), anyInt(), anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("updateStatus (seller)")
    class UpdateStatus {

        private void mockSellerStore() {
            User seller = new User();
            seller.setId(USER_ID);
            seller.setUserType(UserType.SELLER);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(seller));
            Store store = new Store();
            store.setId(STORE_ID);
            store.setSellerId(USER_ID);
            when(storeRepository.findBySellerId(USER_ID)).thenReturn(Optional.of(store));
            when(orderItemRepository.existsByOrderIdAndStoreId(ORDER_ID, STORE_ID)).thenReturn(true);
        }

        @Test
        @DisplayName("PENDING → CONFIRMED: set confirmedAt, không tác động tồn")
        void confirm_success() {
            mockSellerStore();
            Order order = pendingOrder();
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(orderItem(2)));

            orderService.updateStatus(USER_ID, ORDER_ID, OrderStatus.CONFIRMED, "OK");

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(order.getConfirmedAt()).isNotNull();
            verify(stockService, never()).commit(anyLong(), anyInt(), anyLong(), anyLong());
            verify(stockService, never()).release(anyLong(), anyInt(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("Chuyển trạng thái không hợp lệ (PENDING → DELIVERED) → BusinessException")
        void invalidTransition_throws() {
            mockSellerStore();
            Order order = pendingOrder();
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.updateStatus(USER_ID, ORDER_ID, OrderStatus.DELIVERED, null))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("SHIPPING → DELIVERED: chốt xuất tồn (commit)")
        void deliver_commitsStock() {
            mockSellerStore();
            Order order = pendingOrder();
            order.setOrderStatus(OrderStatus.SHIPPING);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(orderItem(2)));

            orderService.updateStatus(USER_ID, ORDER_ID, OrderStatus.DELIVERED, null);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
            assertThat(order.getDeliveredAt()).isNotNull();
            verify(stockService).commit(VARIANT_ID, 2, ORDER_ID, USER_ID);
        }
    }
}
