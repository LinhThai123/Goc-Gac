package com.ecommerce.gocgac.controller.system;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.menu.CreateMenuItemRequest;
import com.ecommerce.gocgac.dto.menu.UpdateMenuItemRequest;
import com.ecommerce.gocgac.entity.enums.MenuType;
import com.ecommerce.gocgac.service.system.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller menu động (M20).
 * - Người dùng: lấy menu theo loại, đã lọc theo quyền.
 * - Admin: CRUD menu.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Menu", description = "API menu động")
@SecurityRequirement(name = "bearerAuth")
public class MenuController {

    private final MenuItemService menuItemService;

    private static final String ADMIN_ROLES = "hasAnyRole('ADMIN', 'SUPER_ADMIN')";

    @GetMapping("/menus")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Menu của tôi theo loại", description = "Đã lọc theo quyền người dùng")
    public ResponseEntity<MessageResponse> myMenu(@RequestParam MenuType type) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy menu thành công",
            menuItemService.getMenuForCurrentUser(type)));
    }

    @GetMapping("/admin/menus")
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Toàn bộ menu theo loại (admin)")
    public ResponseEntity<MessageResponse> allByType(@RequestParam MenuType type) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy menu thành công",
            menuItemService.getAllByType(type)));
    }

    @PostMapping("/admin/menus")
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Tạo menu")
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateMenuItemRequest request) {
        return ResponseEntity.ok(MessageResponse.created("Tạo menu thành công", menuItemService.create(request)));
    }

    @PutMapping("/admin/menus/{id}")
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Cập nhật menu")
    public ResponseEntity<MessageResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        return ResponseEntity.ok(MessageResponse.ok("Cập nhật menu thành công",
            menuItemService.update(id, request)));
    }

    @DeleteMapping("/admin/menus/{id}")
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Xóa menu")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        menuItemService.delete(id);
        return ResponseEntity.ok(MessageResponse.ok("Đã xóa menu"));
    }
}
