package com.ecommerce.gocgac.controller.admin;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.admin.AssignRoleRequest;
import com.ecommerce.gocgac.service.auth.UserRoleAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller gán vai trò cho người dùng (M02).
 * Nằm dưới /api/admin/** → đã được giới hạn ADMIN/SUPER_ADMIN bởi SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/users/{userId}/roles")
@RequiredArgsConstructor
@Tag(name = "User Role", description = "API gán vai trò cho người dùng")
@SecurityRequirement(name = "bearerAuth")
public class UserRoleController {

    private final UserRoleAdminService userRoleAdminService;

    @GetMapping
    @Operation(summary = "Danh sách vai trò của người dùng")
    public ResponseEntity<MessageResponse> getRoles(@PathVariable Long userId) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy vai trò thành công",
            userRoleAdminService.getUserRoles(userId)));
    }

    @PostMapping
    @Operation(summary = "Gán vai trò cho người dùng")
    public ResponseEntity<MessageResponse> assign(@PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(MessageResponse.ok("Gán vai trò thành công",
            userRoleAdminService.assignRole(userId, request.getRoleId())));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Thu hồi vai trò của người dùng")
    public ResponseEntity<MessageResponse> revoke(@PathVariable Long userId, @PathVariable Long roleId) {
        return ResponseEntity.ok(MessageResponse.ok("Thu hồi vai trò thành công",
            userRoleAdminService.revokeRole(userId, roleId)));
    }
}
