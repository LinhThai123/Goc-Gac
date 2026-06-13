package com.ecommerce.gocgac.service.cart;

import com.ecommerce.gocgac.dto.cart.AddToCartRequest;
import com.ecommerce.gocgac.dto.cart.CartItemResponse;
import com.ecommerce.gocgac.dto.cart.CartResponse;
import com.ecommerce.gocgac.dto.cart.UpdateCartItemRequest;
import com.ecommerce.gocgac.entity.Cart;
import com.ecommerce.gocgac.entity.CartItem;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.ProductVariant;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.ProductStatus;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.CartItemRepository;
import com.ecommerce.gocgac.repository.CartRepository;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dịch vụ giỏ hàng (Sprint 1 - M09).
 *
 * <p>Giỏ hàng làm việc ở cấp SKU ({@link ProductVariant}). Mỗi người dùng có
 * tối đa một giỏ; thêm cùng một SKU sẽ cộng dồn số lượng. Giá được "chốt ảnh"
 * (snapshot) từ SKU tại thời điểm thao tác và làm tươi lại mỗi lần cập nhật.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    private static final String VARIANT_ACTIVE = "active";

    /** Lấy giỏ của người dùng, tạo mới nếu chưa có. */
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                Cart cart = new Cart();
                cart.setUserId(userId);
                return cartRepository.save(cart);
            });
    }

    /** Lấy nội dung giỏ hàng hiện tại. */
    @Transactional
    public CartResponse getCart(Long userId) {
        return buildResponse(getOrCreateCart(userId));
    }

    /** Thêm một SKU vào giỏ (cộng dồn nếu đã có). */
    @Transactional
    public CartResponse addItem(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);

        ProductVariant variant = variantRepository.findById(request.getVariantId())
            .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm (SKU) không tồn tại"));
        Product product = productRepository.findById(variant.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        validatePurchasable(product, variant);
        BigDecimal price = requirePrice(variant);

        CartItem existing = cartItemRepository
            .findByCartIdAndVariantId(cart.getId(), variant.getId())
            .orElse(null);

        int desiredQty = request.getQuantity() + (existing != null ? existing.getQuantity() : 0);
        ensureStock(variant, desiredQty);

        if (existing != null) {
            existing.setQuantity(desiredQty);
            existing.setPrice(price);
            cartItemRepository.save(existing);
        } else {
            CartItem item = new CartItem();
            item.setCartId(cart.getId());
            item.setProductId(product.getId());
            item.setVariantId(variant.getId());
            item.setQuantity(request.getQuantity());
            item.setPrice(price);
            cartItemRepository.save(item);
        }

        touchCart(cart);
        log.info("User {} added variant {} (qty {}) to cart {}", userId, variant.getId(), request.getQuantity(), cart.getId());
        return buildResponse(cart);
    }

    /** Cập nhật số lượng một dòng trong giỏ. */
    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm trong giỏ không tồn tại"));

        ProductVariant variant = variantRepository.findById(item.getVariantId())
            .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm (SKU) không tồn tại"));

        ensureStock(variant, request.getQuantity());

        item.setQuantity(request.getQuantity());
        item.setPrice(requirePrice(variant));
        cartItemRepository.save(item);

        touchCart(cart);
        return buildResponse(cart);
    }

    /** Xóa một dòng khỏi giỏ. */
    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm trong giỏ không tồn tại"));
        cartItemRepository.delete(item);
        touchCart(cart);
        return buildResponse(cart);
    }

    /** Xóa toàn bộ giỏ. */
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteAllByCartId(cart.getId());
        touchCart(cart);
    }

    // ========== Helpers ==========

    private void validatePurchasable(Product product, ProductVariant variant) {
        if (product.getDeletedAt() != null
                || product.getStatus() != ProductStatus.ACTIVE
                || product.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new BusinessException("Sản phẩm hiện không khả dụng để mua");
        }
        if (!VARIANT_ACTIVE.equalsIgnoreCase(variant.getStatus())) {
            throw new BusinessException("Biến thể sản phẩm (SKU) hiện không khả dụng");
        }
    }

    private BigDecimal requirePrice(ProductVariant variant) {
        if (variant.getPrice() == null) {
            throw new BusinessException("Sản phẩm chưa được định giá");
        }
        return variant.getPrice();
    }

    private void ensureStock(ProductVariant variant, int quantity) {
        int available = variant.getAvailableQuantity() != null ? variant.getAvailableQuantity() : 0;
        if (quantity > available) {
            throw new BusinessException("Số lượng vượt quá tồn kho. Số lượng còn lại: " + available);
        }
    }

    private void touchCart(Cart cart) {
        cartRepository.save(cart); // kích hoạt @PreUpdate cập nhật updatedAt
    }

    private CartResponse buildResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findAllByCartId(cart.getId());

        List<CartItemResponse> itemResponses = items.stream()
            .map(this::toItemResponse)
            .toList();

        int totalQuantity = itemResponses.stream()
            .mapToInt(CartItemResponse::getQuantity)
            .sum();

        BigDecimal subtotal = itemResponses.stream()
            .map(CartItemResponse::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
            .cartId(cart.getId())
            .items(itemResponses)
            .totalItems(itemResponses.size())
            .totalQuantity(totalQuantity)
            .subtotal(subtotal)
            .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        ProductVariant variant = variantRepository.findById(item.getVariantId()).orElse(null);
        Product product = productRepository.findById(item.getProductId()).orElse(null);

        BigDecimal unitPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
        int available = variant != null && variant.getAvailableQuantity() != null
            ? variant.getAvailableQuantity() : 0;

        return CartItemResponse.builder()
            .id(item.getId())
            .storeId(product != null ? product.getStoreId() : null)
            .productId(item.getProductId())
            .productName(product != null ? product.getProductName() : null)
            .slug(product != null ? product.getSlug() : null)
            .variantId(item.getVariantId())
            .sku(variant != null ? variant.getSku() : null)
            .variantName(variant != null ? variant.getVariantName() : null)
            .imageUrl(variant != null ? variant.getImageUrl() : null)
            .unitPrice(unitPrice)
            .quantity(item.getQuantity())
            .lineTotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())))
            .availableQuantity(available)
            .inStock(item.getQuantity() <= available)
            .build();
    }
}
