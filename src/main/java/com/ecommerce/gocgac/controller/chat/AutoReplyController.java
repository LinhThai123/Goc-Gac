package com.ecommerce.gocgac.controller.chat;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.chat.UpsertAutoReplyRequest;
import com.ecommerce.gocgac.service.chat.AutoReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller trả lời tự động của gian hàng (M16).
 */
@RestController
@RequestMapping("/api/seller/auto-replies")
@RequiredArgsConstructor
@Tag(name = "Auto Reply", description = "API trả lời tự động của gian hàng")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('SELLER', 'COOPERATIVE_MANAGER', 'SUPER_ADMIN')")
public class AutoReplyController {

    private final AutoReplyService autoReplyService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @Operation(summary = "Danh sách trả lời tự động")
    public ResponseEntity<MessageResponse> list() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách thành công", autoReplyService.list(userId)));
    }

    @PostMapping
    @Operation(summary = "Tạo trả lời tự động")
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody UpsertAutoReplyRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.created("Tạo thành công",
            autoReplyService.create(userId, request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật trả lời tự động")
    public ResponseEntity<MessageResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpsertAutoReplyRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Cập nhật thành công",
            autoReplyService.update(userId, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa trả lời tự động")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        autoReplyService.delete(userId, id);
        return ResponseEntity.ok(MessageResponse.ok("Đã xóa"));
    }
}
