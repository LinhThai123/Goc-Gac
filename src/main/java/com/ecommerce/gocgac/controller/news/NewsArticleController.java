package com.ecommerce.gocgac.controller.news;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.news.CreateNewsArticleRequest;
import com.ecommerce.gocgac.dto.news.UpdateNewsArticleRequest;
import com.ecommerce.gocgac.service.news.NewsArticleService;
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
 * Controller bài viết tin tức (M19).
 * - Công khai: danh sách & chi tiết bài đã đăng (đếm lượt xem).
 * - Admin: CRUD + đăng/gỡ bài.
 */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "News Article", description = "API bài viết tin tức")
public class NewsArticleController {

    private final NewsArticleService articleService;
    private final CurrentUserService currentUserService;

    private static final String ADMIN_ROLES = "hasAnyRole('ADMIN', 'SUPER_ADMIN')";

    // ===== Public =====

    @GetMapping("/public")
    @Operation(summary = "Danh sách bài viết đã đăng (công khai)")
    public ResponseEntity<MessageResponse> listPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy tin tức thành công",
            articleService.listPublished(PageRequest.of(page, size))));
    }

    @GetMapping("/public/category/{categoryId}")
    @Operation(summary = "Bài viết đã đăng theo danh mục (công khai)")
    public ResponseEntity<MessageResponse> listByCategory(@PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy tin tức thành công",
            articleService.listPublishedByCategory(categoryId, PageRequest.of(page, size))));
    }

    @GetMapping("/public/{slug}")
    @Operation(summary = "Chi tiết bài viết theo slug (công khai)", description = "Tăng lượt xem")
    public ResponseEntity<MessageResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy bài viết thành công",
            articleService.getPublishedBySlug(slug)));
    }

    // ===== Admin =====

    @GetMapping("/all")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tất cả bài viết (admin)")
    public ResponseEntity<MessageResponse> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách bài viết thành công",
            articleService.listAll(PageRequest.of(page, size))));
    }

    @PostMapping
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo bài viết")
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateNewsArticleRequest request) {
        Long authorId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.created("Tạo bài viết thành công",
            articleService.create(authorId, request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật bài viết")
    public ResponseEntity<MessageResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdateNewsArticleRequest request) {
        return ResponseEntity.ok(MessageResponse.ok("Cập nhật bài viết thành công",
            articleService.update(id, request)));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Đăng bài viết")
    public ResponseEntity<MessageResponse> publish(@PathVariable Long id) {
        return ResponseEntity.ok(MessageResponse.ok("Đã đăng bài viết", articleService.publish(id)));
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Gỡ bài viết")
    public ResponseEntity<MessageResponse> unpublish(@PathVariable Long id) {
        return ResponseEntity.ok(MessageResponse.ok("Đã gỡ bài viết", articleService.unpublish(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa bài viết")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.ok(MessageResponse.ok("Đã xóa bài viết"));
    }
}
