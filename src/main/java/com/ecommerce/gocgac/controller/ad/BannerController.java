package com.ecommerce.gocgac.controller.ad;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.ad.CreateBannerRequest;
import com.ecommerce.gocgac.dto.ad.UpdateBannerRequest;
import com.ecommerce.gocgac.service.ad.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller banner trang chủ (M18).
 * - Công khai: lấy banner đang hiển thị theo vị trí.
 * - Admin: CRUD banner.
 */
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
@Tag(name = "Banner", description = "API banner")
public class BannerController {

    private final BannerService bannerService;

    private static final String ADMIN_ROLES = "hasAnyRole('ADMIN', 'SUPER_ADMIN')";

    @GetMapping("/public")
    @Operation(summary = "Banner đang hiển thị theo vị trí (công khai)")
    public ResponseEntity<MessageResponse> listActive(@RequestParam String position) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy banner thành công", bannerService.listActive(position)));
    }

    @GetMapping("/all")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tất cả banner (admin)")
    public ResponseEntity<MessageResponse> listAll() {
        return ResponseEntity.ok(MessageResponse.ok("Lấy banner thành công", bannerService.listAll()));
    }

    @PostMapping
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo banner")
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateBannerRequest request) {
        return ResponseEntity.ok(MessageResponse.created("Tạo banner thành công", bannerService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật banner")
    public ResponseEntity<MessageResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdateBannerRequest request) {
        return ResponseEntity.ok(MessageResponse.ok("Cập nhật banner thành công", bannerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa banner")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ResponseEntity.ok(MessageResponse.ok("Đã xóa banner"));
    }
}
