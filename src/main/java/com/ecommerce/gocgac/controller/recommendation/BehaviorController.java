package com.ecommerce.gocgac.controller.recommendation;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.recommendation.RecordSearchRequest;
import com.ecommerce.gocgac.dto.recommendation.RecordViewRequest;
import com.ecommerce.gocgac.service.recommendation.BehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Ghi nhận hành vi & xem lại lịch sử của người dùng (M08).
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Tag(name = "User Behavior", description = "API hành vi người dùng (xem, tìm kiếm)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class BehaviorController {

    private final BehaviorService behaviorService;
    private final CurrentUserService currentUserService;

    @PostMapping("/views")
    @Operation(summary = "Ghi nhận lượt xem sản phẩm")
    public ResponseEntity<MessageResponse> recordView(@Valid @RequestBody RecordViewRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        behaviorService.recordView(userId, request.getProductId(), request.getViewDuration());
        return ResponseEntity.ok(MessageResponse.ok("Đã ghi nhận lượt xem"));
    }

    @GetMapping("/views")
    @Operation(summary = "Sản phẩm đã xem gần đây")
    public ResponseEntity<MessageResponse> recentViews(@RequestParam(defaultValue = "20") int limit) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy lịch sử xem thành công",
            behaviorService.getRecentViewedProducts(userId, limit)));
    }

    @PostMapping("/search-history")
    @Operation(summary = "Ghi nhận tìm kiếm")
    public ResponseEntity<MessageResponse> recordSearch(@Valid @RequestBody RecordSearchRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        behaviorService.recordSearch(userId, request.getKeyword(), request.getResultCount());
        return ResponseEntity.ok(MessageResponse.ok("Đã ghi nhận tìm kiếm"));
    }

    @GetMapping("/search-history")
    @Operation(summary = "Từ khóa tìm kiếm gần đây")
    public ResponseEntity<MessageResponse> searchHistory() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy lịch sử tìm kiếm thành công",
            behaviorService.getRecentSearchKeywords(userId)));
    }
}
