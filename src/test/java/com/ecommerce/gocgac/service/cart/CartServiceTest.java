package com.ecommerce.gocgac.service.cart;

import com.ecommerce.gocgac.dto.cart.AddToCartRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho {@link CartService} — thuần Mockito, không cần Spring context / CSDL.
 * Kiểm chứng các quy tắc nghiệp vụ: tạo giỏ, gộp SKU, kiểm tra tồn kho, tính tiền,
 * và các trường hợp ngoại lệ.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks private CartService cartService;

    private static final Long USER_ID = 7L;
    private static final Long CART_ID = 1L;
    private static final Long VARIANT_ID = 10L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long STORE_ID = 1L;

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
        product.setProductName("Áo thun OCOP");
        product.setSlug("ao-thun-ocop");
        product.setStatus(ProductStatus.ACTIVE);
        product.setApprovalStatus(ApprovalStatus.APPROVED);
        product.setDeletedAt(null);

        variant = new ProductVariant();
        variant.setId(VARIANT_ID);
        variant.setProductId(PRODUCT_ID);
        variant.setSku("SKU-ATO-L");
        variant.setVariantName("Đỏ - L");
        variant.setPrice(new BigDecimal("50000"));
        variant.setStockQuantity(5);
        variant.setReservedQuantity(0);
        variant.setAvailableQuantity(5);
        variant.setStatus("active");
    }

    private AddToCartRequest addRequest(int quantity) {
        AddToCartRequest req = new AddToCartRequest();
        req.setVariantId(VARIANT_ID);
        req.setQuantity(quantity);
        return req;
    }

    private CartItem cartItem(int quantity, BigDecimal price) {
        CartItem item = new CartItem();
        item.setId(50L);
        item.setCartId(CART_ID);
        item.setProductId(PRODUCT_ID);
        item.setVariantId(VARIANT_ID);
        item.setQuantity(quantity);
        item.setPrice(price);
        return item;
    }

    @Nested
    @DisplayName("addItem")
    class AddItem {

        @Test
        @DisplayName("Thêm SKU mới: lưu cart_item đúng giá & số lượng, subtotal đúng")
        void addNewItem_success() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
            when(cartItemRepository.findByCartIdAndVariantId(CART_ID, VARIANT_ID)).thenReturn(Optional.empty());
            // sau khi thêm, giỏ chứa 1 dòng số lượng 2
            when(cartItemRepository.findAllByCartId(CART_ID))
                .thenReturn(List.of(cartItem(2, new BigDecimal("50000"))));

            CartResponse response = cartService.addItem(USER_ID, addRequest(2));

            ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
            verify(cartItemRepository).save(captor.capture());
            CartItem saved = captor.getValue();
            assertThat(saved.getVariantId()).isEqualTo(VARIANT_ID);
            assertThat(saved.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(saved.getQuantity()).isEqualTo(2);
            assertThat(saved.getPrice()).isEqualByComparingTo("50000");

            assertThat(response.getTotalItems()).isEqualTo(1);
            assertThat(response.getTotalQuantity()).isEqualTo(2);
            assertThat(response.getSubtotal()).isEqualByComparingTo("100000");
            assertThat(response.getItems().get(0).getStoreId()).isEqualTo(STORE_ID);
            assertThat(response.getItems().get(0).getInStock()).isTrue();
            assertThat(response.getItems().get(0).getLineTotal()).isEqualByComparingTo("100000");
        }

        @Test
        @DisplayName("Thêm cùng SKU đã có: cộng dồn số lượng")
        void addExistingItem_mergesQuantity() {
            CartItem existing = cartItem(1, new BigDecimal("50000"));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
            when(cartItemRepository.findByCartIdAndVariantId(CART_ID, VARIANT_ID)).thenReturn(Optional.of(existing));
            when(cartItemRepository.findAllByCartId(CART_ID)).thenReturn(List.of(existing));

            cartService.addItem(USER_ID, addRequest(2));

            ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
            verify(cartItemRepository).save(captor.capture());
            assertThat(captor.getValue().getQuantity()).isEqualTo(3); // 1 + 2
        }

        @Test
        @DisplayName("Tự tạo giỏ nếu người dùng chưa có")
        void createsCartWhenMissing() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
            when(cartItemRepository.findByCartIdAndVariantId(CART_ID, VARIANT_ID)).thenReturn(Optional.empty());
            when(cartItemRepository.findAllByCartId(CART_ID))
                .thenReturn(List.of(cartItem(1, new BigDecimal("50000"))));

            cartService.addItem(USER_ID, addRequest(1));

            // Giỏ được tạo (save ở getOrCreateCart); touchCart cũng save → atLeastOnce
            verify(cartRepository, atLeastOnce()).save(any(Cart.class));
        }

        @Test
        @DisplayName("Số lượng vượt tồn kho → BusinessException, không lưu item")
        void exceedingStock_throws() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
            when(cartItemRepository.findByCartIdAndVariantId(CART_ID, VARIANT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addItem(USER_ID, addRequest(6)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tồn kho");

            verify(cartItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Sản phẩm chưa duyệt → BusinessException")
        void productNotApproved_throws() {
            product.setApprovalStatus(ApprovalStatus.PENDING);
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> cartService.addItem(USER_ID, addRequest(1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không khả dụng");

            verify(cartItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("SKU không active → BusinessException")
        void variantInactive_throws() {
            variant.setStatus("inactive");
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> cartService.addItem(USER_ID, addRequest(1)))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("SKU chưa định giá → BusinessException")
        void variantWithoutPrice_throws() {
            variant.setPrice(null);
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> cartService.addItem(USER_ID, addRequest(1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("định giá");
        }

        @Test
        @DisplayName("SKU không tồn tại → ResourceNotFoundException")
        void variantNotFound_throws() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addItem(USER_ID, addRequest(1)))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateItem")
    class UpdateItem {

        @Test
        @DisplayName("Cập nhật số lượng hợp lệ")
        void update_success() {
            CartItem item = cartItem(1, new BigDecimal("50000"));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByIdAndCartId(50L, CART_ID)).thenReturn(Optional.of(item));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(cartItemRepository.findAllByCartId(CART_ID)).thenReturn(List.of(item));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            UpdateCartItemRequest req = new UpdateCartItemRequest();
            req.setQuantity(3);
            cartService.updateItem(USER_ID, 50L, req);

            ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
            verify(cartItemRepository).save(captor.capture());
            assertThat(captor.getValue().getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("Cập nhật vượt tồn → BusinessException")
        void update_exceedingStock_throws() {
            CartItem item = cartItem(1, new BigDecimal("50000"));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByIdAndCartId(50L, CART_ID)).thenReturn(Optional.of(item));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));

            UpdateCartItemRequest req = new UpdateCartItemRequest();
            req.setQuantity(99);
            assertThatThrownBy(() -> cartService.updateItem(USER_ID, 50L, req))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Item không tồn tại → ResourceNotFoundException")
        void update_itemNotFound_throws() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByIdAndCartId(50L, CART_ID)).thenReturn(Optional.empty());

            UpdateCartItemRequest req = new UpdateCartItemRequest();
            req.setQuantity(2);
            assertThatThrownBy(() -> cartService.updateItem(USER_ID, 50L, req))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("removeItem & clearCart")
    class RemoveAndClear {

        @Test
        @DisplayName("Xóa dòng tồn tại → gọi delete")
        void remove_success() {
            CartItem item = cartItem(1, new BigDecimal("50000"));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByIdAndCartId(50L, CART_ID)).thenReturn(Optional.of(item));
            when(cartItemRepository.findAllByCartId(CART_ID)).thenReturn(List.of());

            cartService.removeItem(USER_ID, 50L);

            verify(cartItemRepository).delete(item);
        }

        @Test
        @DisplayName("Xóa dòng không tồn tại → ResourceNotFoundException")
        void remove_notFound_throws() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByIdAndCartId(50L, CART_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.removeItem(USER_ID, 50L))
                .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Xóa toàn bộ giỏ → gọi deleteAllByCartId")
        void clear_success() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

            cartService.clearCart(USER_ID);

            verify(cartItemRepository).deleteAllByCartId(CART_ID);
        }
    }

    @Nested
    @DisplayName("getCart")
    class GetCart {

        @Test
        @DisplayName("Giỏ rỗng → subtotal = 0, không có dòng nào")
        void emptyCart() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findAllByCartId(CART_ID)).thenReturn(List.of());

            CartResponse response = cartService.getCart(USER_ID);

            assertThat(response.getTotalItems()).isZero();
            assertThat(response.getTotalQuantity()).isZero();
            assertThat(response.getSubtotal()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Nhiều dòng → subtotal cộng đúng")
        void multipleItems_subtotal() {
            CartItem item = cartItem(2, new BigDecimal("50000")); // 2 x 50.000 = 100.000
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findAllByCartId(CART_ID)).thenReturn(List.of(item));
            when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(variant));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            CartResponse response = cartService.getCart(USER_ID);

            assertThat(response.getSubtotal()).isEqualByComparingTo("100000");
            assertThat(response.getTotalQuantity()).isEqualTo(2);
        }
    }
}
