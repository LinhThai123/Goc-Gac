package com.ecommerce.gocgac.controller.catalog;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.dto.catalog.AddProductToCatalogRequest;
import com.ecommerce.gocgac.dto.catalog.CatalogProductResponse;
import com.ecommerce.gocgac.dto.catalog.CatalogResponse;
import com.ecommerce.gocgac.dto.catalog.CreateCatalogRequest;
import com.ecommerce.gocgac.dto.catalog.UpdateCatalogRequest;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.exception.CatalogException;
import com.ecommerce.gocgac.repository.UserRepository;
import com.ecommerce.gocgac.service.catalog.CatalogService;
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
 * Controller cho Catalog (Catalog riêng cho từng shop)
 * - Protected endpoints: CRUD catalog của mình, quản lý products trong catalog
 */
//TODO đang làm dở cập nhật thông tin catalog
@Slf4j
@RestController
@RequestMapping("/api/catalogs")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "API quản lý Catalog (Catalog riêng cho shop)")
@SecurityRequirement(name = "bearerAuth")
public class CatalogController {
    
    private final CatalogService catalogService;
    private final UserRepository userRepository;
    
    /**
     * Lấy userId từ JWT token
     */
    private Long getCurrentUserId() {
        String email = JwtUtils.getEmail();
        if (email == null) {
            throw new CatalogException("Không thể xác định user từ token");
        }
        return userRepository.findByEmail(email)
            .map(User::getId)
            .orElseThrow(() -> new CatalogException("User không tồn tại"));
    }
    
    // ========== CRUD Catalog ==========
    
    /**
     * Lấy tất cả catalogs của mình (multi-tenant)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy danh sách catalogs của tôi", 
               description = "Lấy tất cả catalogs của store hiện tại (yêu cầu COOPERATIVE_MANAGER hoặc SELLER)")
    public ResponseEntity<MessageResponse> getMyCatalogs() {
        try {
            Long userId = getCurrentUserId();
            List<CatalogResponse> catalogs = catalogService.getMyCatalogs(userId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách catalogs thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(catalogs);
            return ResponseEntity.ok(response);
        } catch (CatalogException e) {
            log.error("Error getting catalogs: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Lấy catalog theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy catalog theo ID", 
               description = "Lấy thông tin catalog theo ID (yêu cầu COOPERATIVE_MANAGER hoặc SELLER)")
    public ResponseEntity<MessageResponse> getCatalogById(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            CatalogResponse catalog = catalogService.getCatalogById(id, userId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy thông tin catalog thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(catalog);
            return ResponseEntity.ok(response);
        } catch (CatalogException e) {
            log.error("Error getting catalog: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Tạo catalog mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Tạo catalog mới", 
               description = "Tạo catalog mới cho store của bạn (yêu cầu COOPERATIVE_MANAGER hoặc SELLER)")
    public ResponseEntity<MessageResponse> createCatalog(@Valid @RequestBody CreateCatalogRequest request) {
        try {
            Long userId = getCurrentUserId();
            CatalogResponse catalog = catalogService.createCatalog(userId, request);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Tạo catalog thành công");
            response.setStatus(HttpStatus.CREATED.value());
            response.setData(catalog);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (CatalogException e) {
            log.error("Error creating catalog: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Cập nhật catalog
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Cập nhật catalog", 
               description = "Cập nhật thông tin catalog của bạn (yêu cầu COOPERATIVE_MANAGER hoặc SELLER)")
    public ResponseEntity<MessageResponse> updateCatalog(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCatalogRequest request) {
        try {
            Long userId = getCurrentUserId();
            CatalogResponse catalog = catalogService.updateCatalog(id, userId, request);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Cập nhật catalog thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(catalog);
            return ResponseEntity.ok(response);
        } catch (CatalogException e) {
            log.error("Error updating catalog: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Xóa catalog (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Xóa catalog", 
               description = "Xóa catalog của bạn (soft delete - set isActive = false)")
    public ResponseEntity<MessageResponse> deleteCatalog(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            catalogService.deleteCatalog(id, userId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Xóa catalog thành công");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (CatalogException e) {
            log.error("Error deleting catalog: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ========== Quản lý Products trong Catalog ==========
    
    /**
     * Lấy danh sách products trong catalog
     */
    @GetMapping("/{id}/products")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy danh sách products trong catalog", 
               description = "Lấy tất cả products đang active trong catalog")
    public ResponseEntity<MessageResponse> getProductsInCatalog(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            List<CatalogProductResponse> products = catalogService.getProductsInCatalog(id, userId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách products thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(products);
            return ResponseEntity.ok(response);
        } catch (CatalogException e) {
            log.error("Error getting products in catalog: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Thêm product vào catalog
     */
    @PostMapping("/{id}/products")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Thêm product vào catalog", 
               description = "Thêm product vào catalog với validation duplicate")
    public ResponseEntity<MessageResponse> addProductToCatalog(
            @PathVariable Long id,
            @Valid @RequestBody AddProductToCatalogRequest request) {
        try {
            Long userId = getCurrentUserId();
            CatalogProductResponse catalogProduct = catalogService.addProductToCatalog(id, userId, request);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Thêm product vào catalog thành công");
            response.setStatus(HttpStatus.CREATED.value());
            response.setData(catalogProduct);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (CatalogException e) {
            log.error("Error adding product to catalog: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Xóa product khỏi catalog
     */
    @DeleteMapping("/{id}/products/{productId}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Xóa product khỏi catalog", 
               description = "Xóa product khỏi catalog")
    public ResponseEntity<MessageResponse> removeProductFromCatalog(
            @PathVariable Long id,
            @PathVariable Long productId) {
        try {
            Long userId = getCurrentUserId();
            catalogService.removeProductFromCatalog(id, userId, productId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Xóa product khỏi catalog thành công");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (CatalogException e) {
            log.error("Error removing product from catalog: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Kiểm tra product đã có trong catalog chưa
     */
    @GetMapping("/{id}/products/{productId}/check")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Kiểm tra product trong catalog", 
               description = "Kiểm tra product đã có trong catalog chưa (validate duplicate)")
    public ResponseEntity<MessageResponse> checkProductInCatalog(
            @PathVariable Long id,
            @PathVariable Long productId) {
        try {
            boolean exists = catalogService.isProductInCatalog(id, productId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage(exists ? "Product đã có trong catalog" : "Product chưa có trong catalog");
            response.setStatus(HttpStatus.OK.value());
            response.setData(exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking product in catalog: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi kiểm tra: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

