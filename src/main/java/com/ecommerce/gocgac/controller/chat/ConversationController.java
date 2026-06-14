package com.ecommerce.gocgac.controller.chat;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.chat.SendMessageRequest;
import com.ecommerce.gocgac.service.chat.ConversationService;
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
 * Controller hội thoại phía khách hàng + dùng chung (M16).
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversation", description = "API tin nhắn khách hàng ↔ gian hàng")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class ConversationController {

    private final ConversationService conversationService;
    private final CurrentUserService currentUserService;

    @PostMapping("/store/{storeId}/messages")
    @Operation(summary = "Gửi tin tới gian hàng", description = "Tự tạo hội thoại nếu chưa có")
    public ResponseEntity<MessageResponse> sendToStore(@PathVariable Long storeId,
            @Valid @RequestBody SendMessageRequest request) {
        Long buyerId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.created("Đã gửi tin nhắn",
            conversationService.sendAsBuyer(buyerId, storeId, request)));
    }

    @GetMapping("/my")
    @Operation(summary = "Hội thoại của tôi (khách)")
    public ResponseEntity<MessageResponse> myConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long buyerId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy hội thoại thành công",
            conversationService.getMyConversations(buyerId, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Tin nhắn của hội thoại", description = "Tự đánh dấu đã đọc cho phía đang xem")
    public ResponseEntity<MessageResponse> messages(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy tin nhắn thành công",
            conversationService.getMessages(userId, id, PageRequest.of(page, size))));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Đánh dấu đã đọc")
    public ResponseEntity<MessageResponse> read(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        conversationService.markConversationRead(userId, id);
        return ResponseEntity.ok(MessageResponse.ok("Đã đánh dấu đã đọc"));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Lưu trữ hội thoại")
    public ResponseEntity<MessageResponse> archive(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        conversationService.archive(userId, id);
        return ResponseEntity.ok(MessageResponse.ok("Đã lưu trữ hội thoại"));
    }
}
