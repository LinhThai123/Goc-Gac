package com.ecommerce.gocgac.controller.review;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.review.CreateReviewRequest;
import com.ecommerce.gocgac.dto.review.ReplyRequest;
import com.ecommerce.gocgac.service.review.ProductReviewService;
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
 * Controller Đánh giá sản phẩm (M14).
 * - Công khai: xem đánh giá đã duyệt.
 * - Khách: viết đánh giá (đã mua), đánh dấu hữu ích.
 * - Người bán: phản hồi đánh giá.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "API đánh giá sản phẩm")
public class ReviewController {

    private final ProductReviewService reviewService;
    private final CurrentUserService currentUserService;

    @GetMapping("/public/product/{productId}")
    @Operation(summary = "Đánh giá đã duyệt của sản phẩm", description = "Công khai")
    public ResponseEntity<MessageResponse> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy đánh giá thành công",
            reviewService.getProductReviews(productId, PageRequest.of(page, size))));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Viết đánh giá", description = "Chỉ khách đã mua & nhận hàng")
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateReviewRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.created("Gửi đánh giá thành công, chờ kiểm duyệt",
            reviewService.createReview(userId, request)));
    }

    @PostMapping("/{id}/helpful")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Đánh dấu hữu ích")
    public ResponseEntity<MessageResponse> markHelpful(@PathVariable Long id) {
        reviewService.markHelpful(id);
        return ResponseEntity.ok(MessageResponse.ok("Đã đánh dấu hữu ích"));
    }

    @PostMapping("/{id}/reply")
    @PreAuthorize("hasAnyRole('SELLER', 'COOPERATIVE_MANAGER', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Phản hồi đánh giá", description = "Người bán phản hồi đánh giá sản phẩm của gian hàng")
    public ResponseEntity<MessageResponse> reply(@PathVariable Long id,
            @Valid @RequestBody ReplyRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Đã phản hồi đánh giá",
            reviewService.reply(userId, id, request.getContent())));
    }
}
