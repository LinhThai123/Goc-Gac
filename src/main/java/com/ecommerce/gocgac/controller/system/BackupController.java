package com.ecommerce.gocgac.controller.system;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.service.system.BackupLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller sao lưu dữ liệu (M20) — chỉ SUPER_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/backups")
@RequiredArgsConstructor
@Tag(name = "Backup", description = "API sao lưu dữ liệu")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class BackupController {

    private final BackupLogService backupLogService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @Operation(summary = "Tạo bản sao lưu ngay")
    public ResponseEntity<MessageResponse> create() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.created("Yêu cầu sao lưu đã được xử lý",
            backupLogService.createBackup(userId, "MANUAL")));
    }

    @GetMapping
    @Operation(summary = "Lịch sử sao lưu")
    public ResponseEntity<MessageResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy lịch sử sao lưu thành công",
            backupLogService.list(PageRequest.of(page, size))));
    }
}
