package com.ecommerce.gocgac.controller.notification;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller cho thông báo trong ứng dụng của người dùng đang đăng nhập.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "API thông báo trong ứng dụng")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @Operation(summary = "Danh sách thông báo", description = "Lấy thông báo của tôi (mới nhất trước), có phân trang")
    public ResponseEntity<MessageResponse> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = currentUserService.getCurrentUserId();
        var data = notificationService.getMyNotifications(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách thông báo thành công", data));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Số thông báo chưa đọc", description = "Đếm số thông báo chưa đọc của tôi")
    public ResponseEntity<MessageResponse> getUnreadCount() {
        Long userId = currentUserService.getCurrentUserId();
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(MessageResponse.ok("Đếm thông báo chưa đọc thành công",
            Map.of("unreadCount", count)));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Đánh dấu đã đọc", description = "Đánh dấu một thông báo là đã đọc")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(MessageResponse.ok("Đã đánh dấu thông báo là đã đọc"));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả đã đọc", description = "Đánh dấu tất cả thông báo của tôi là đã đọc")
    public ResponseEntity<MessageResponse> markAllAsRead() {
        Long userId = currentUserService.getCurrentUserId();
        int updated = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(MessageResponse.ok("Đã đánh dấu tất cả là đã đọc",
            Map.of("updated", updated)));
    }
}
