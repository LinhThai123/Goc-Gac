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
 * Controller hội thoại phía gian hàng (M16).
 */
@RestController
@RequestMapping("/api/seller/conversations")
@RequiredArgsConstructor
@Tag(name = "Seller Conversation", description = "API tin nhắn phía gian hàng")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('SELLER', 'COOPERATIVE_MANAGER', 'SUPER_ADMIN')")
public class SellerConversationController {

    private final ConversationService conversationService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @Operation(summary = "Hội thoại của gian hàng tôi")
    public ResponseEntity<MessageResponse> storeConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy hội thoại thành công",
            conversationService.getStoreConversations(userId, PageRequest.of(page, size))));
    }

    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "Gian hàng trả lời hội thoại")
    public ResponseEntity<MessageResponse> reply(@PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.created("Đã gửi tin nhắn",
            conversationService.sendAsSeller(userId, conversationId, request)));
    }
}
