package com.ecommerce.gocgac.controller.category;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.category.CategoryResponse;
import com.ecommerce.gocgac.dto.category.CreateCategoryRequest;
import com.ecommerce.gocgac.dto.category.UpdateCategoryRequest;
import com.ecommerce.gocgac.exception.CategoryException;
import com.ecommerce.gocgac.service.category.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho Category Management
 * - Public endpoints: Lấy danh sách categories, lấy category theo slug
 * - Protected endpoints: CRUD categories (yêu cầu ADMIN hoặc SUPER_ADMIN)
 */
//TODO đang làm dở cập nhật thông tin category 
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "API quản lý Category")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    // ========== Public Endpoints ==========
    
    /**
     * Lấy tất cả categories active (public)
     */
    @GetMapping("/public")
    @Operation(summary = "Lấy danh sách categories active (public)", 
               description = "Lấy tất cả categories đang active, không cần authentication")
    public ResponseEntity<MessageResponse> getAllActiveCategories() {
        try {
            List<CategoryResponse> categories = categoryService.getAllActiveCategories();
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách categories thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting active categories: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi lấy danh sách categories: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Lấy category theo slug (public)
     */
    @GetMapping("/public/slug/{slug}")
    @Operation(summary = "Lấy category theo slug (public)", 
               description = "Lấy thông tin category theo slug, không cần authentication")
    public ResponseEntity<MessageResponse> getCategoryBySlug(@PathVariable String slug) {
        try {
            CategoryResponse category = categoryService.getCategoryBySlug(slug);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy thông tin category thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(category);
            return ResponseEntity.ok(response);
        } catch (CategoryException e) {
            log.error("Error getting category by slug: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error getting category by slug: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi lấy thông tin category: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Lấy tất cả root categories (public)
     */
    @GetMapping("/public/roots")
    @Operation(summary = "Lấy danh sách root categories (public)", 
               description = "Lấy tất cả root categories (parent_id = null) đang active")
    public ResponseEntity<MessageResponse> getRootCategories() {
        try {
            List<CategoryResponse> categories = categoryService.getRootCategories();
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách root categories thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting root categories: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi lấy danh sách root categories: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Lấy danh sách categories con (public)
     */
    @GetMapping("/public/parent/{parentId}/children")
    @Operation(summary = "Lấy danh sách categories con (public)", 
               description = "Lấy tất cả categories con của một category")
    public ResponseEntity<MessageResponse> getChildCategories(@PathVariable Long parentId) {
        try {
            List<CategoryResponse> categories = categoryService.getChildCategories(parentId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách categories con thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting child categories: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi lấy danh sách categories con: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ========== Protected Endpoints (Admin only) ==========
    
    /**
     * Lấy tất cả categories (bao gồm cả inactive)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy danh sách tất cả categories", 
               description = "Lấy tất cả categories (bao gồm cả inactive), yêu cầu ADMIN hoặc SUPER_ADMIN")
    public ResponseEntity<MessageResponse> getAllCategories() {
        try {
            List<CategoryResponse> categories = categoryService.getAllCategories();
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách categories thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting all categories: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi lấy danh sách categories: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Lấy category theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy category theo ID", 
               description = "Lấy thông tin category theo ID, yêu cầu ADMIN hoặc SUPER_ADMIN")
    public ResponseEntity<MessageResponse> getCategoryById(@PathVariable Long id) {
        try {
            CategoryResponse category = categoryService.getCategoryById(id);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy thông tin category thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(category);
            return ResponseEntity.ok(response);
        } catch (CategoryException e) {
            log.error("Error getting category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error getting category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi lấy thông tin category: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Tạo category mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Tạo category mới", 
               description = "Tạo category mới, yêu cầu ADMIN hoặc SUPER_ADMIN")
    public ResponseEntity<MessageResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        try {
            CategoryResponse category = categoryService.createCategory(request);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Tạo category thành công");
            response.setStatus(HttpStatus.CREATED.value());
            response.setData(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (CategoryException e) {
            log.error("Error creating category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi tạo category: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Cập nhật category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Cập nhật category", 
               description = "Cập nhật thông tin category, yêu cầu ADMIN hoặc SUPER_ADMIN")
    public ResponseEntity<MessageResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        try {
            CategoryResponse category = categoryService.updateCategory(id, request);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Cập nhật category thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(category);
            return ResponseEntity.ok(response);
        } catch (CategoryException e) {
            log.error("Error updating category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error updating category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi cập nhật category: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Xóa category (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Xóa category", 
               description = "Xóa category (soft delete - set isActive = false), yêu cầu ADMIN hoặc SUPER_ADMIN")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Xóa category thành công");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (CategoryException e) {
            log.error("Error deleting category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error deleting category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi xóa category: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

