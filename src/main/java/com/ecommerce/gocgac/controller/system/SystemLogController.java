package com.ecommerce.gocgac.controller.system;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.service.system.SystemLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller truy vấn nhật ký hệ thống (M20) — chỉ admin.
 */
@RestController
@RequestMapping("/api/admin/system-logs")
@RequiredArgsConstructor
@Tag(name = "System Log", description = "API nhật ký hệ thống")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class SystemLogController {

    private final SystemLogService systemLogService;

    @GetMapping
    @Operation(summary = "Danh sách nhật ký", description = "Lọc theo module hoặc userId (tùy chọn)")
    public ResponseEntity<MessageResponse> query(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy nhật ký thành công",
            systemLogService.query(module, userId, PageRequest.of(page, size))));
    }
}
