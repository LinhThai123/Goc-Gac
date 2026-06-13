package com.ecommerce.gocgac.controller.admin;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.dto.product.ChangeStatusProduct;
import com.ecommerce.gocgac.dto.product.ProductResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Admin Product Management", description = "API kiểm duyệt sản phẩm dành cho admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {
    
    private final ProductService productService;
    private final UserRepository userRepository;
    
    private Long getCurrentAdminId() {
        String email = JwtUtils.getEmail();
        if (email == null) {
            throw new ProductException("Không thể xác định admin từ token");
        }
        return userRepository.findByEmail(email)
            .map(User::getId)
            .orElseThrow(() -> new ProductException("Admin không tồn tại"));
    }
    
    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm để kiểm duyệt",
               description = "Admin lọc sản phẩm theo store, trạng thái duyệt, trạng thái hoạt động, loại sản phẩm và từ khóa.")
    public ResponseEntity<MessageResponse> getProductsForReview(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false, defaultValue = "PENDING") ApprovalStatus approvalStatus,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean hasVariants,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(defaultValue = "false") boolean includeSkus) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponse> products = productService.getProductsForAdmin(
            storeId,
            status,
            approvalStatus,
            productType,
            categoryId,
            hasVariants,
            keyword,
            includeSkus,
            pageable
        );
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Lấy danh sách sản phẩm kiểm duyệt thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(products);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết sản phẩm để kiểm duyệt")
    public ResponseEntity<MessageResponse> getProductForReview(@PathVariable Long id) {
        ProductResponse product = productService.getProductByIdForAdmin(id);
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Lấy thông tin sản phẩm thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(product);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/approve")
    @Operation(summary = "Duyệt sản phẩm")
    public ResponseEntity<MessageResponse> approveProduct(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusProduct request) {
        
        if (request.getStatus() != ApprovalStatus.APPROVED) {
            throw new ProductException("Trạng thái phải là APPROVED");
        }
        
        ProductResponse product = productService.approveProduct(getCurrentAdminId(), id);
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Duyệt sản phẩm thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(product);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/reject")
    @Operation(summary = "Từ chối sản phẩm")
    public ResponseEntity<MessageResponse> rejectProduct(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusProduct request) {
        
        if (request.getStatus() != ApprovalStatus.REJECTED) {
            throw new ProductException("Trạng thái phải là REJECTED");
        }
        
        ProductResponse product = productService.rejectProduct(
            getCurrentAdminId(),
            id,
            request.getRejectionReason()
        );
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Từ chối sản phẩm thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(product);
        return ResponseEntity.ok(response);
    }
}
