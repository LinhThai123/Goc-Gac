package com.ecommerce.gocgac.controller.cart;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.cart.AddToCartRequest;
import com.ecommerce.gocgac.dto.cart.UpdateCartItemRequest;
import com.ecommerce.gocgac.service.cart.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller giỏ hàng của người dùng đang đăng nhập (M09).
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "API giỏ hàng")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @Operation(summary = "Xem giỏ hàng", description = "Lấy nội dung giỏ hàng hiện tại")
    public ResponseEntity<MessageResponse> getCart() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy giỏ hàng thành công", cartService.getCart(userId)));
    }

    @PostMapping("/items")
    @Operation(summary = "Thêm vào giỏ", description = "Thêm một SKU vào giỏ (cộng dồn nếu đã có)")
    public ResponseEntity<MessageResponse> addItem(@Valid @RequestBody AddToCartRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Đã thêm vào giỏ hàng", cartService.addItem(userId, request)));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Cập nhật số lượng", description = "Cập nhật số lượng một dòng trong giỏ")
    public ResponseEntity<MessageResponse> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Đã cập nhật giỏ hàng", cartService.updateItem(userId, itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ", description = "Xóa một dòng khỏi giỏ")
    public ResponseEntity<MessageResponse> removeItem(@PathVariable Long itemId) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Đã xóa sản phẩm khỏi giỏ", cartService.removeItem(userId, itemId)));
    }

    @DeleteMapping
    @Operation(summary = "Xóa toàn bộ giỏ", description = "Xóa tất cả sản phẩm trong giỏ")
    public ResponseEntity<MessageResponse> clearCart() {
        Long userId = currentUserService.getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(MessageResponse.ok("Đã xóa toàn bộ giỏ hàng"));
    }
}
