package com.ecommerce.gocgac.controller.product;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.dto.product.CreateProductRequest;
import com.ecommerce.gocgac.dto.product.ProductResponse;
import com.ecommerce.gocgac.dto.product.UpdateProductRequest;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.ProductStatus;
import com.ecommerce.gocgac.entity.enums.ProductType;
import com.ecommerce.gocgac.exception.ProductException;
import com.ecommerce.gocgac.repository.UserRepository;
import com.ecommerce.gocgac.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho Product Management
 * - Protected endpoints: CRUD sản phẩm của store mình
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "API quản lý sản phẩm")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {
    
    private final ProductService productService;
    private final UserRepository userRepository;
    
    /**
     * Lấy userId từ JWT token
     */
    private Long getCurrentUserId() {
        String email = JwtUtils.getEmail();
        if (email == null) {
            throw new ProductException("Không thể xác định user từ token");
        }
        return userRepository.findByEmail(email)
            .map(User::getId)
            .orElseThrow(() -> new ProductException("User không tồn tại"));
    }
    
    /**
     * Tạo sản phẩm mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Tạo sản phẩm mới", 
               description = "Tạo sản phẩm mới với ít nhất 1 SKU. Mỗi sản phẩm bắt buộc phải có ít nhất 1 SKU.")
    public ResponseEntity<MessageResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        try {
            Long userId = getCurrentUserId();
            ProductResponse product = productService.createProduct(userId, request);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Tạo sản phẩm thành công");
            response.setStatus(HttpStatus.CREATED.value());
            response.setData(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ProductException e) {
            log.error("Error creating product: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Lấy danh sách sản phẩm với pagination và filtering
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy danh sách sản phẩm", 
               description = "Lấy danh sách sản phẩm của store với pagination và các filter")
    public ResponseEntity<MessageResponse> getProducts(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) ApprovalStatus approvalStatus,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean hasVariants,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(defaultValue = "false") boolean includeSkus) {
        try {
            Long userId = getCurrentUserId();
            
            // Tạo Pageable
            Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Lấy danh sách products
            Page<ProductResponse> products;
            if (includeSkus) {
                products = productService.getProducts(
                    userId, status, approvalStatus, productType, 
                    categoryId, hasVariants, keyword, pageable
                );
            } else {
                products = productService.getProductsSimple(
                    userId, status, approvalStatus, productType, 
                    categoryId, hasVariants, keyword, pageable
                );
            }
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách sản phẩm thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(products);
            return ResponseEntity.ok(response);
        } catch (ProductException e) {
            log.error("Error getting products: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Lấy thông tin chi tiết sản phẩm theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy thông tin chi tiết sản phẩm", 
               description = "Lấy thông tin chi tiết sản phẩm theo ID (bao gồm tất cả SKUs)")
    public ResponseEntity<MessageResponse> getProductById(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            ProductResponse product = productService.getProductById(id, userId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy thông tin sản phẩm thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(product);
            return ResponseEntity.ok(response);
        } catch (ProductException e) {
            log.error("Error getting product: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * Cập nhật thông tin sản phẩm
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Cập nhật thông tin sản phẩm", 
               description = "Cập nhật thông tin sản phẩm. Tất cả các field đều optional - chỉ cập nhật những field được gửi lên. " +
                           "Để cập nhật SKUs: SKU có id là update, SKU không có id là tạo mới, SKU không có trong list sẽ bị xóa. " +
                           "Sản phẩm phải có ít nhất 1 SKU sau khi cập nhật.")
    public ResponseEntity<MessageResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        try {
            Long userId = getCurrentUserId();
            ProductResponse product = productService.updateProduct(userId, id, request);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Cập nhật sản phẩm thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(product);
            return ResponseEntity.ok(response);
        } catch (ProductException e) {
            log.error("Error updating product: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Xóa sản phẩm
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Xóa sản phẩm", 
               description = "Xóa sản phẩm. " +
                           "Nếu sản phẩm ở trạng thái ACTIVE: Xóa mềm (soft delete) - chuyển sang DELETED. " +
                           "Nếu sản phẩm ở trạng thái không hoạt động (INACTIVE, OUT_OF_STOCK, DELETED): Xóa cứng (hard delete) - xóa khỏi database.")
    public ResponseEntity<MessageResponse> deleteProduct(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            productService.deleteProduct(userId, id);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Xóa sản phẩm thành công");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (ProductException e) {
            log.error("Error deleting product: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ========== Public Endpoints (không cần authentication) ==========
    
    /**
     * Lấy danh sách sản phẩm public - cho người dùng chưa đăng nhập và người mua hàng
     */
    @GetMapping("/public")
    @Operation(summary = "Lấy danh sách sản phẩm public", 
               description = "Lấy danh sách sản phẩm đã được duyệt và đang hoạt động (public endpoint, không cần authentication)")
    public ResponseEntity<MessageResponse> getPublicProducts(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean hasVariants,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        try {
            // Tạo Pageable
            Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Lấy danh sách products public
            Page<ProductResponse> products = productService.getPublicProducts(
                storeId, productType, categoryId, hasVariants, keyword, pageable
            );
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách sản phẩm thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting public products: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Lấy thông tin chi tiết sản phẩm public theo slug
     */
    @GetMapping("/public/slug/{slug}")
    @Operation(summary = "Lấy sản phẩm theo slug (public)", 
               description = "Lấy thông tin chi tiết sản phẩm theo slug (public endpoint, không cần authentication)")
    public ResponseEntity<MessageResponse> getPublicProductBySlug(@PathVariable String slug) {
        try {
            ProductResponse product = productService.getPublicProductBySlug(slug);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy thông tin sản phẩm thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(product);
            return ResponseEntity.ok(response);
        } catch (ProductException e) {
            log.error("Error getting product by slug: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error getting product by slug: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi lấy thông tin sản phẩm: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Lấy thông tin chi tiết sản phẩm public theo ID
     */
    @GetMapping("/public/{id}")
    @Operation(summary = "Lấy sản phẩm theo ID (public)", 
               description = "Lấy thông tin chi tiết sản phẩm theo ID (public endpoint, không cần authentication)")
    public ResponseEntity<MessageResponse> getPublicProductById(@PathVariable Long id) {
        try {
            ProductResponse product = productService.getPublicProductById(id);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy thông tin sản phẩm thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(product);
            return ResponseEntity.ok(response);
        } catch (ProductException e) {
            log.error("Error getting product by id: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error getting product by id: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi lấy thông tin sản phẩm: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Lấy danh sách sản phẩm public của store
     */
    @GetMapping("/public/store/{storeId}")
    @Operation(summary = "Lấy sản phẩm của store (public)", 
               description = "Lấy danh sách sản phẩm public của một store cụ thể (public endpoint)")
    public ResponseEntity<MessageResponse> getPublicProductsByStore(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductResponse> products = productService.getPublicProductsByStoreId(storeId, pageable);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách sản phẩm của store thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting products by store: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Lấy danh sách sản phẩm public theo category
     */
    @GetMapping("/public/category/{categoryId}")
    @Operation(summary = "Lấy sản phẩm theo category (public)", 
               description = "Lấy danh sách sản phẩm public của một category cụ thể (public endpoint)")
    public ResponseEntity<MessageResponse> getPublicProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductResponse> products = productService.getPublicProductsByCategoryId(categoryId, pageable);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách sản phẩm theo category thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting products by category: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Lấy danh sách sản phẩm public theo catalog
     */
    @GetMapping("/public/catalog/{catalogId}")
    @Operation(summary = "Lấy sản phẩm theo catalog (public)", 
               description = "Lấy danh sách sản phẩm public trong một catalog cụ thể (public endpoint)")
    public ResponseEntity<MessageResponse> getPublicProductsByCatalog(
            @PathVariable Long catalogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductResponse> products = productService.getPublicProductsByCatalogId(catalogId, pageable);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Lấy danh sách sản phẩm theo catalog thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(products);
            return ResponseEntity.ok(response);
        } catch (ProductException e) {
            log.error("Error getting products by catalog: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error getting products by catalog: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

