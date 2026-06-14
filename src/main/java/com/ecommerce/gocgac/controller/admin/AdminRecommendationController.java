package com.ecommerce.gocgac.controller.admin;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.service.recommendation.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sinh lại dữ liệu gợi ý (M08) — admin. Nằm dưới /api/admin/** → đã giới hạn ADMIN/SUPER_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/recommendations")
@RequiredArgsConstructor
@Tag(name = "Admin Recommendation", description = "API sinh gợi ý")
@SecurityRequirement(name = "bearerAuth")
public class AdminRecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/regenerate")
    @Operation(summary = "Sinh lại toàn bộ gợi ý (tương tự + mua kèm)")
    public ResponseEntity<MessageResponse> regenerate() {
        return ResponseEntity.ok(MessageResponse.ok("Đã sinh lại gợi ý",
            recommendationService.regenerateAll()));
    }
}
