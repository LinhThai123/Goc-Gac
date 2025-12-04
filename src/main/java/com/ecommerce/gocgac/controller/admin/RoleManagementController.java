package com.ecommerce.gocgac.controller.admin;

import com.ecommerce.gocgac.entity.Role;
import com.ecommerce.gocgac.entity.Permission;
import com.ecommerce.gocgac.service.auth.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý roles và permissions
 * Chỉ Super Admin mới có quyền truy cập
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class RoleManagementController {
    
    private final RoleService roleService;
    
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }
    
    @GetMapping("/permissions")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(roleService.getAllPermissions());
    }
    
    @PostMapping
    public ResponseEntity<Role> createRole(
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam(required = false) String description) {
        Role role = roleService.createRole(name, code, description);
        return ResponseEntity.ok(role);
    }
    
    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Role> assignPermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        Role role = roleService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(role);
    }
}

