package com.ecommerce.gocgac.controller.admin;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.service.review.ProductReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller kiểm duyệt đánh giá (M14) — dành cho admin.
 * Nằm dưới /api/admin/** nên đã được SecurityConfig giới hạn ADMIN/SUPER_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "Admin Review", description = "API kiểm duyệt đánh giá")
@SecurityRequirement(name = "bearerAuth")
public class AdminReviewController {

    private final ProductReviewService reviewService;

    @GetMapping("/pending")
    @Operation(summary = "Đánh giá chờ duyệt")
    public ResponseEntity<MessageResponse> pending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy đánh giá chờ duyệt thành công",
            reviewService.getPendingReviews(PageRequest.of(page, size))));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Duyệt đánh giá")
    public ResponseEntity<MessageResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(MessageResponse.ok("Đã duyệt đánh giá", reviewService.approve(id)));
    }

    @PostMapping("/{id}/hide")
    @Operation(summary = "Ẩn đánh giá", description = "Từ chối / ẩn đánh giá khỏi hiển thị công khai")
    public ResponseEntity<MessageResponse> hide(@PathVariable Long id) {
        return ResponseEntity.ok(MessageResponse.ok("Đã ẩn đánh giá", reviewService.hide(id)));
    }
}
