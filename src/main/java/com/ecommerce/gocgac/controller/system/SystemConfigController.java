package com.ecommerce.gocgac.controller.system;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.system.UpsertSystemConfigRequest;
import com.ecommerce.gocgac.service.system.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cấu hình hệ thống (M20).
 * - Công khai: đọc các cấu hình is_public.
 * - Admin: quản lý toàn bộ cấu hình.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "System Config", description = "API cấu hình hệ thống")
public class SystemConfigController {

    private final SystemConfigService configService;

    private static final String ADMIN_ROLES = "hasAnyRole('ADMIN', 'SUPER_ADMIN')";

    @GetMapping("/system-configs/public")
    @Operation(summary = "Cấu hình công khai")
    public ResponseEntity<MessageResponse> listPublic() {
        return ResponseEntity.ok(MessageResponse.ok("Lấy cấu hình công khai thành công",
            configService.listPublic()));
    }

    @GetMapping("/admin/system-configs")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tất cả cấu hình (admin)")
    public ResponseEntity<MessageResponse> listAll() {
        return ResponseEntity.ok(MessageResponse.ok("Lấy cấu hình thành công", configService.listAll()));
    }

    @PutMapping("/admin/system-configs")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo/cập nhật cấu hình")
    public ResponseEntity<MessageResponse> upsert(@Valid @RequestBody UpsertSystemConfigRequest request) {
        return ResponseEntity.ok(MessageResponse.ok("Lưu cấu hình thành công", configService.upsert(request)));
    }

    @DeleteMapping("/admin/system-configs/{id}")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa cấu hình")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        configService.delete(id);
        return ResponseEntity.ok(MessageResponse.ok("Đã xóa cấu hình"));
    }
}
