package com.ecommerce.gocgac.controller.recommendation;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.service.recommendation.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Phục vụ gợi ý sản phẩm (M08).
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "API gợi ý sản phẩm")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final CurrentUserService currentUserService;

    @GetMapping("/public/products/{productId}/related")
    @Operation(summary = "Sản phẩm liên quan (công khai)")
    public ResponseEntity<MessageResponse> related(@PathVariable Long productId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy sản phẩm liên quan thành công",
            recommendationService.getRelatedProducts(productId, limit)));
    }

    @GetMapping("/for-you")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Gợi ý cho bạn", description = "Cá nhân hóa theo lịch sử xem")
    public ResponseEntity<MessageResponse> forYou(@RequestParam(defaultValue = "10") int limit) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy gợi ý thành công",
            recommendationService.getForYou(userId, limit)));
    }
}
