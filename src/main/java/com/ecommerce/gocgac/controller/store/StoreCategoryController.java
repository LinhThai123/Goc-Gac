package com.ecommerce.gocgac.controller.store;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.dto.store.CreateStoreCategoryRequest;
import com.ecommerce.gocgac.dto.store.StoreCategoryResponse;
import com.ecommerce.gocgac.dto.store.UpdateStoreCategoryRequest;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.exception.StoreCategoryException;
import com.ecommerce.gocgac.repository.UserRepository;
import com.ecommerce.gocgac.service.store.StoreCategoryService;
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
 * Controller cho Store Category Management
 * - Protected endpoints: CRUD store categories của store mình
 */
//TODO đang làm dở cập nhật thông tin store category
//TODO đang test dở product api với store category api
@Slf4j
@RestController
@RequestMapping("/api/store-categories")
@RequiredArgsConstructor
@Tag(name = "Store Category", description = "API quản lý Store Category")
@SecurityRequirement(name = "bearerAuth")
public class StoreCategoryController {
    
    private final StoreCategoryService storeCategoryService;
    private final UserRepository userRepository;
    
    /**
     * Lấy userId từ JWT token
     */
    private Long getCurrentUserId() {
        String email = JwtUtils.getEmail();
        if (email == null) {
            throw new StoreCategoryException("Không thể xác định user từ token");
        }
        return userRepository.findByEmail(email)
            .map(User::getId)
            .orElseThrow(() -> new StoreCategoryException("User không tồn tại"));
    }
    
    /**
     * Tạo store category mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Tạo store category mới", 
               description = "Tạo store category mới cho store của bạn")
    public ResponseEntity<MessageResponse> createStoreCategory(
            @Valid @RequestBody CreateStoreCategoryRequest request) {
        try {
            Long userId = getCurrentUserId();
            StoreCategoryResponse category = storeCategoryService.createStoreCategory(userId, request);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Tạo store category thành công");
            response.setStatus(HttpStatus.CREATED.value());
            response.setData(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (StoreCategoryException e) {
            log.error("Error creating store category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Lấy tất cả store categories của store hiện tại
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy danh sách store categories", 
               description = "Lấy tất cả store categories của store của bạn")
    public ResponseEntity<MessageResponse> getMyStoreCategories() {
        try {
            Long userId = getCurrentUserId();
            List<StoreCategoryResponse> categories = storeCategoryService.getMyStoreCategories(userId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách store categories thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(categories);
            return ResponseEntity.ok(response);
        } catch (StoreCategoryException e) {
            log.error("Error getting store categories: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Lấy tất cả store categories active của store hiện tại
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy danh sách store categories active", 
               description = "Lấy tất cả store categories đang active của store của bạn")
    public ResponseEntity<MessageResponse> getMyActiveStoreCategories() {
        try {
            Long userId = getCurrentUserId();
            List<StoreCategoryResponse> categories = storeCategoryService.getMyActiveStoreCategories(userId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách store categories active thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(categories);
            return ResponseEntity.ok(response);
        } catch (StoreCategoryException e) {
            log.error("Error getting active store categories: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Lấy store category theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy store category theo ID", 
               description = "Lấy thông tin store category theo ID (phải thuộc về store của bạn)")
    public ResponseEntity<MessageResponse> getStoreCategoryById(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            StoreCategoryResponse category = storeCategoryService.getStoreCategoryById(id, userId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy thông tin store category thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(category);
            return ResponseEntity.ok(response);
        } catch (StoreCategoryException e) {
            log.error("Error getting store category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * Lấy tất cả root store categories (parent_id = null)
     */
    @GetMapping("/roots")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy danh sách root store categories", 
               description = "Lấy tất cả root store categories (parent_id = null) của store của bạn")
    public ResponseEntity<MessageResponse> getRootStoreCategories() {
        try {
            Long userId = getCurrentUserId();
            List<StoreCategoryResponse> categories = storeCategoryService.getRootStoreCategories(userId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách root store categories thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(categories);
            return ResponseEntity.ok(response);
        } catch (StoreCategoryException e) {
            log.error("Error getting root store categories: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Lấy danh sách child categories của một category
     */
    @GetMapping("/{parentId}/children")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy danh sách child categories", 
               description = "Lấy tất cả child categories của một store category")
    public ResponseEntity<MessageResponse> getChildStoreCategories(@PathVariable Long parentId) {
        try {
            Long userId = getCurrentUserId();
            List<StoreCategoryResponse> categories = storeCategoryService.getChildStoreCategories(parentId, userId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách child categories thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(categories);
            return ResponseEntity.ok(response);
        } catch (StoreCategoryException e) {
            log.error("Error getting child categories: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Cập nhật store category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Cập nhật store category", 
               description = "Cập nhật thông tin store category (phải thuộc về store của bạn)")
    public ResponseEntity<MessageResponse> updateStoreCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreCategoryRequest request) {
        try {
            Long userId = getCurrentUserId();
            StoreCategoryResponse category = storeCategoryService.updateStoreCategory(id, userId, request);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Cập nhật store category thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(category);
            return ResponseEntity.ok(response);
        } catch (StoreCategoryException e) {
            log.error("Error updating store category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Xóa store category (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Xóa store category", 
               description = "Xóa store category (soft delete - set isActive = false). " +
                           "Không thể xóa nếu còn category con hoặc còn sản phẩm đang sử dụng.")
    public ResponseEntity<MessageResponse> deleteStoreCategory(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            storeCategoryService.deleteStoreCategory(id, userId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Xóa store category thành công");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (StoreCategoryException e) {
            log.error("Error deleting store category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

