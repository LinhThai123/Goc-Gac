package com.ecommerce.gocgac.controller.promotion;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.promotion.AddPromotionProductsRequest;
import com.ecommerce.gocgac.dto.promotion.CreatePromotionRequest;
import com.ecommerce.gocgac.dto.promotion.UpdatePromotionRequest;
import com.ecommerce.gocgac.service.promotion.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Khuyến mãi (M12) — phía người bán / quản lý HTX.
 */
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion", description = "API chương trình khuyến mãi")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('SELLER', 'COOPERATIVE_MANAGER', 'SUPER_ADMIN')")
public class PromotionController {

    private final PromotionService promotionService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @Operation(summary = "Tạo khuyến mãi")
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreatePromotionRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.created("Tạo khuyến mãi thành công",
            promotionService.createPromotion(userId, request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật khuyến mãi")
    public ResponseEntity<MessageResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdatePromotionRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Cập nhật khuyến mãi thành công",
            promotionService.updatePromotion(userId, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hủy khuyến mãi")
    public ResponseEntity<MessageResponse> cancel(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        promotionService.cancelPromotion(userId, id);
        return ResponseEntity.ok(MessageResponse.ok("Đã hủy khuyến mãi"));
    }

    @GetMapping("/my-store")
    @Operation(summary = "Khuyến mãi của gian hàng tôi")
    public ResponseEntity<MessageResponse> getStorePromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách khuyến mãi thành công",
            promotionService.getStorePromotions(userId, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết khuyến mãi")
    public ResponseEntity<MessageResponse> getOne(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy chi tiết khuyến mãi thành công",
            promotionService.getPromotion(userId, id)));
    }

    @PostMapping("/{id}/products")
    @Operation(summary = "Thêm sản phẩm vào khuyến mãi")
    public ResponseEntity<MessageResponse> addProducts(@PathVariable Long id,
            @Valid @RequestBody AddPromotionProductsRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Đã thêm sản phẩm vào khuyến mãi",
            promotionService.addProducts(userId, id, request.getProductIds())));
    }

    @DeleteMapping("/{id}/products/{productId}")
    @Operation(summary = "Bỏ sản phẩm khỏi khuyến mãi")
    public ResponseEntity<MessageResponse> removeProduct(@PathVariable Long id, @PathVariable Long productId) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Đã bỏ sản phẩm khỏi khuyến mãi",
            promotionService.removeProduct(userId, id, productId)));
    }
}
