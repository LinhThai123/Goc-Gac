package com.ecommerce.gocgac.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuResponse {
    
    private Long id;
    
    private Long productId;
    
    private String variantName;
    
    private String sku;
    
    private String size;
    
    private String color;
    
    private String material;
    
    private String weightVariant;
    
    private String barcode;
    
    private BigDecimal price;
    
    private BigDecimal comparePrice;
    
    private BigDecimal costPrice;
    
    private Integer stockQuantity;
    
    private Integer reservedQuantity;
    
    private Integer availableQuantity;
    
    private String imageUrl;
    
    private BigDecimal weightKg;
    
    private BigDecimal lengthCm;
    
    private BigDecimal widthCm;
    
    private BigDecimal heightCm;
    
    private String status;
    
    private Integer displayOrder;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

