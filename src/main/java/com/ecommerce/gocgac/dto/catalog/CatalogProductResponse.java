package com.ecommerce.gocgac.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogProductResponse {
    
    private Long id;
    
    private Long catalogId;
    
    private Long productId;
    
    private Integer displayOrder;
    
    private Boolean isFeatured;
    
    private Boolean isActive;
    
    private LocalDateTime addedAt;
    
    // Product info (có thể thêm sau khi join với Product entity)
    private String productName;
    
    private String productSlug;
    
    private String productImage;
}

