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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho Store Category Management
 * - Public: xem danh mục active theo storeId (storefront)
 * - Protected: CRUD store categories của store mình
 */
@Slf4j
@RestController
@RequestMapping("/api/store-categories")
@RequiredArgsConstructor
@Tag(name = "Store Category", description = "API quản lý Store Category")
@SecurityRequirement(name = "bearerAuth")
public class StoreCategoryController {

    private final StoreCategoryService storeCategoryService;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        String email = JwtUtils.getEmail();
        if (email == null) {
            throw new StoreCategoryException("Không thể xác định user từ token");
        }
        return userRepository.findByEmail(email)
            .map(User::getId)
            .orElseThrow(() -> new StoreCategoryException("User không tồn tại"));
    }

    // ========== Public Endpoints ==========

    @GetMapping("/public/store/{storeId}")
    @Operation(summary = "Lấy danh mục active của store (public)",
               description = "Danh sách phẳng, sort displayOrder, không cần đăng nhập")
    public ResponseEntity<MessageResponse> getPublicActiveCategories(@PathVariable Long storeId) {
        try {
            List<StoreCategoryResponse> categories = storeCategoryService.getPublicActiveCategories(storeId);
            return ok("Lấy danh sách store categories thành công", categories);
        } catch (StoreCategoryException e) {
            return notFound(e.getMessage());
        }
    }

    @GetMapping("/public/store/{storeId}/tree")
    @Operation(summary = "Lấy cây danh mục active của store (public)",
               description = "Root kèm children lồng nhau trong field `children`")
    public ResponseEntity<MessageResponse> getPublicCategoryTree(@PathVariable Long storeId) {
        try {
            List<StoreCategoryResponse> tree = storeCategoryService.getPublicCategoryTree(storeId);
            return ok("Lấy cây danh mục thành công", tree);
        } catch (StoreCategoryException e) {
            return notFound(e.getMessage());
        }
    }

    @GetMapping("/public/store/{storeId}/roots")
    @Operation(summary = "Lấy root categories active của store (public)")
    public ResponseEntity<MessageResponse> getPublicRootCategories(@PathVariable Long storeId) {
        try {
            List<StoreCategoryResponse> categories = storeCategoryService.getPublicRootCategories(storeId);
            return ok("Lấy danh sách root categories thành công", categories);
        } catch (StoreCategoryException e) {
            return notFound(e.getMessage());
        }
    }

    @GetMapping("/public/store/{storeId}/parent/{parentId}/children")
    @Operation(summary = "Lấy danh mục con active (public)",
               description = "Parent phải active và thuộc store")
    public ResponseEntity<MessageResponse> getPublicChildCategories(
            @PathVariable Long storeId,
            @PathVariable Long parentId) {
        try {
            List<StoreCategoryResponse> categories = storeCategoryService.getPublicChildCategories(storeId, parentId);
            return ok("Lấy danh sách child categories thành công", categories);
        } catch (StoreCategoryException e) {
            return notFound(e.getMessage());
        }
    }

    // ========== Protected Endpoints ==========

    @PostMapping
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Tạo store category mới")
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
            return badRequest(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy danh sách store categories",
               description = "Mặc định trả tất cả (gồm inactive). Dùng page & size để phân trang, activeOnly=true chỉ lấy active")
    public ResponseEntity<MessageResponse> getMyStoreCategories(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        try {
            Long userId = getCurrentUserId();
            if (page != null && size != null) {
                PageRequest pageable = PageRequest.of(page, size, Sort.by("displayOrder").ascending());
                Page<StoreCategoryResponse> result = activeOnly
                    ? storeCategoryService.getMyActiveStoreCategories(userId, pageable)
                    : storeCategoryService.getMyStoreCategories(userId, pageable);
                return ok("Lấy danh sách store categories thành công", result);
            }
            List<StoreCategoryResponse> categories = activeOnly
                ? storeCategoryService.getMyActiveStoreCategories(userId)
                : storeCategoryService.getMyStoreCategories(userId);
            return ok("Lấy danh sách store categories thành công", categories);
        } catch (StoreCategoryException e) {
            return badRequest(e.getMessage());
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy danh sách store categories active",
               description = "Alias: chỉ active. Hỗ trợ page & size để phân trang")
    public ResponseEntity<MessageResponse> getMyActiveStoreCategories(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return getMyStoreCategories(page, size, true);
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy cây danh mục của store",
               description = "activeOnly=false (mặc định): tất cả; activeOnly=true: chỉ active")
    public ResponseEntity<MessageResponse> getStoreCategoryTree(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        try {
            Long userId = getCurrentUserId();
            List<StoreCategoryResponse> tree = storeCategoryService.getStoreCategoryTree(userId, activeOnly);
            return ok("Lấy cây danh mục thành công", tree);
        } catch (StoreCategoryException e) {
            return badRequest(e.getMessage());
        }
    }

    @GetMapping("/roots")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy root store categories",
               description = "activeOnly=false (mặc định): gồm inactive; activeOnly=true: chỉ active")
    public ResponseEntity<MessageResponse> getRootStoreCategories(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        try {
            Long userId = getCurrentUserId();
            List<StoreCategoryResponse> categories = storeCategoryService.getRootStoreCategories(userId, activeOnly);
            return ok("Lấy danh sách root store categories thành công", categories);
        } catch (StoreCategoryException e) {
            return badRequest(e.getMessage());
        }
    }

    @GetMapping("/{parentId}/children")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy child store categories",
               description = "activeOnly=false (mặc định): gồm inactive")
    public ResponseEntity<MessageResponse> getChildStoreCategories(
            @PathVariable Long parentId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        try {
            Long userId = getCurrentUserId();
            List<StoreCategoryResponse> categories = storeCategoryService.getChildStoreCategories(parentId, userId, activeOnly);
            return ok("Lấy danh sách child categories thành công", categories);
        } catch (StoreCategoryException e) {
            return badRequest(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy store category theo ID")
    public ResponseEntity<MessageResponse> getStoreCategoryById(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            StoreCategoryResponse category = storeCategoryService.getStoreCategoryById(id, userId);
            return ok("Lấy thông tin store category thành công", category);
        } catch (StoreCategoryException e) {
            return notFound(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Cập nhật store category",
               description = "Dùng makeRoot=true để chuyển về root (parentId=null)")
    public ResponseEntity<MessageResponse> updateStoreCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreCategoryRequest request) {
        try {
            Long userId = getCurrentUserId();
            StoreCategoryResponse category = storeCategoryService.updateStoreCategory(id, userId, request);
            return ok("Cập nhật store category thành công", category);
        } catch (StoreCategoryException e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Xóa store category",
               description = "Soft delete. Không xóa được nếu còn con active hoặc còn sản phẩm")
    public ResponseEntity<MessageResponse> deleteStoreCategory(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            storeCategoryService.deleteStoreCategory(id, userId);
            MessageResponse response = new MessageResponse();
            response.setMessage("Xóa store category thành công");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (StoreCategoryException e) {
            return badRequest(e.getMessage());
        }
    }

    private ResponseEntity<MessageResponse> ok(String message, Object data) {
        MessageResponse response = new MessageResponse();
        response.setMessage(message);
        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<MessageResponse> badRequest(String message) {
        log.error("Store category error: {}", message);
        MessageResponse response = new MessageResponse();
        response.setMessage(message);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }

    private ResponseEntity<MessageResponse> notFound(String message) {
        log.error("Store category not found: {}", message);
        MessageResponse response = new MessageResponse();
        response.setMessage(message);
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
