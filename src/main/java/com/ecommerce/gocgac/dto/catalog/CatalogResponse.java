package com.ecommerce.gocgac.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogResponse {
    
    private Long id;
    
    private Long storeId;
    
    private Long cooperativeId;
    
    private String catalogName;
    
    private String catalogCode;
    
    private String shortDescription;
    
    private String description;
    
    private String imageUrl;
    
    private String bannerUrl;
    
    private Integer displayOrder;
    
    private Boolean isFeatured;
    
    private Boolean isActive;
    
    private String catalogSettings;
    
    private Integer productCount;
    
    private Integer viewCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

