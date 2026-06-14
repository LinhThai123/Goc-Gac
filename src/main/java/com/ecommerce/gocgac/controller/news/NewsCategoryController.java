package com.ecommerce.gocgac.controller.news;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.news.CreateNewsCategoryRequest;
import com.ecommerce.gocgac.dto.news.UpdateNewsCategoryRequest;
import com.ecommerce.gocgac.service.news.NewsCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller danh mục tin tức (M19).
 * - Công khai: xem danh mục đang hoạt động.
 * - Admin: CRUD danh mục.
 */
@RestController
@RequestMapping("/api/news-categories")
@RequiredArgsConstructor
@Tag(name = "News Category", description = "API danh mục tin tức")
public class NewsCategoryController {

    private final NewsCategoryService categoryService;

    private static final String ADMIN_ROLES = "hasAnyRole('ADMIN', 'SUPER_ADMIN')";

    @GetMapping("/public")
    @Operation(summary = "Danh mục tin đang hoạt động (công khai)")
    public ResponseEntity<MessageResponse> listActive() {
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh mục tin thành công", categoryService.listActive()));
    }

    @PostMapping
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo danh mục tin")
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateNewsCategoryRequest request) {
        return ResponseEntity.ok(MessageResponse.created("Tạo danh mục tin thành công",
            categoryService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật danh mục tin")
    public ResponseEntity<MessageResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdateNewsCategoryRequest request) {
        return ResponseEntity.ok(MessageResponse.ok("Cập nhật danh mục tin thành công",
            categoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ngừng danh mục tin")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(MessageResponse.ok("Đã ngừng danh mục tin"));
    }
}
