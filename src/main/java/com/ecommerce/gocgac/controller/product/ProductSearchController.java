package com.ecommerce.gocgac.controller.product;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.product.SearchRequest;
import com.ecommerce.gocgac.dto.product.SearchResponse;
import com.ecommerce.gocgac.exception.ProductException;
import com.ecommerce.gocgac.service.product.ProductSearchService;
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

/**
 * Controller cho Product Search với Elasticsearch
 */
@Slf4j
@RestController
@RequestMapping("/api/products/search")
@RequiredArgsConstructor
@Tag(name = "Product Search", description = "API tìm kiếm sản phẩm với Elasticsearch")
public class ProductSearchController {
    
    private final ProductSearchService productSearchService;
    
    /**
     * Search sản phẩm với Elasticsearch
     * Public endpoint - không cần authentication
     */
    @PostMapping
    @Operation(summary = "Tìm kiếm sản phẩm", 
               description = "Tìm kiếm sản phẩm với full-text search, filters, và sorting. " +
                           "Chỉ trả về sản phẩm đã APPROVED và ACTIVE.")
    public ResponseEntity<MessageResponse> searchProducts(@Valid @RequestBody SearchRequest request) {
        try {
            SearchResponse response = productSearchService.searchProducts(request);
            
            MessageResponse messageResponse = new MessageResponse();
            messageResponse.setMessage("Tìm kiếm thành công");
            messageResponse.setStatus(HttpStatus.OK.value());
            messageResponse.setData(response);
            return ResponseEntity.ok(messageResponse);
        } catch (ProductException e) {
            log.error("Error searching products: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Search sản phẩm với query parameters (GET method)
     * Public endpoint - không cần authentication
     */
    @GetMapping
    @Operation(summary = "Tìm kiếm sản phẩm (GET)", 
               description = "Tìm kiếm sản phẩm với query parameters. " +
                           "Chỉ trả về sản phẩm đã APPROVED và ACTIVE.")
    public ResponseEntity<MessageResponse> searchProductsGet(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long storeCategoryId,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) java.util.List<String> sizes,
            @RequestParam(required = false) java.util.List<String> colors,
            @RequestParam(required = false) java.util.List<String> materials,
            @RequestParam(required = false) Boolean ocopCertified,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) Boolean hasOriginTracking,
            @RequestParam(required = false) Boolean hasVariants,
            @RequestParam(required = false) Boolean isCombo,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") Boolean highlight) {
        try {
            SearchRequest request = new SearchRequest();
            request.setKeyword(keyword);
            request.setStoreId(storeId);
            request.setCategoryId(categoryId);
            request.setStoreCategoryId(storeCategoryId);
            if (productType != null) {
                request.setProductType(com.ecommerce.gocgac.entity.enums.ProductType.valueOf(productType));
            }
            if (status != null) {
                request.setStatus(com.ecommerce.gocgac.entity.enums.ProductStatus.valueOf(status));
            }
            request.setMinPrice(minPrice);
            request.setMaxPrice(maxPrice);
            request.setSizes(sizes);
            request.setColors(colors);
            request.setMaterials(materials);
            request.setOcopCertified(ocopCertified);
            request.setIsVerified(isVerified);
            request.setHasOriginTracking(hasOriginTracking);
            request.setHasVariants(hasVariants);
            request.setIsCombo(isCombo);
            request.setSortBy(sortBy);
            request.setSortDir(sortDir);
            request.setPage(page);
            request.setSize(size);
            request.setHighlight(highlight);
            
            SearchResponse response = productSearchService.searchProducts(request);
            
            MessageResponse messageResponse = new MessageResponse();
            messageResponse.setMessage("Tìm kiếm thành công");
            messageResponse.setStatus(HttpStatus.OK.value());
            messageResponse.setData(response);
            return ResponseEntity.ok(messageResponse);
        } catch (ProductException e) {
            log.error("Error searching products: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Re-index tất cả sản phẩm vào Elasticsearch
     * Protected endpoint - chỉ admin mới có thể thực hiện
     */
    @PostMapping("/reindex")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @Operation(summary = "Re-index tất cả sản phẩm", 
               description = "Re-index tất cả sản phẩm vào Elasticsearch. " +
                           "Chỉ SUPER_ADMIN mới có thể thực hiện.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MessageResponse> reIndexAllProducts() {
        try {
            productSearchService.reIndexAllProducts();
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Re-index tất cả sản phẩm thành công");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (ProductException e) {
            log.error("Error re-indexing products: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Index một sản phẩm cụ thể vào Elasticsearch
     * Protected endpoint - chỉ admin/store owner mới có thể thực hiện
     */
    @PostMapping("/index/{productId}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Index sản phẩm", 
               description = "Index một sản phẩm cụ thể vào Elasticsearch. " +
                           "Chỉ COOPERATIVE_MANAGER, SELLER, hoặc SUPER_ADMIN mới có thể thực hiện.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MessageResponse> indexProduct(@PathVariable Long productId) {
        try {
            productSearchService.indexProduct(productId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Index sản phẩm thành công");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (ProductException e) {
            log.error("Error indexing product: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

