package com.ecommerce.gocgac.dto.product;

import com.ecommerce.gocgac.entity.enums.ProductStatus;
import com.ecommerce.gocgac.entity.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho search request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    
    /**
     * Keyword để tìm kiếm (tìm trong productName, description, shortDescription)
     */
    private String keyword;
    
    /**
     * Filter theo storeId
     */
    private Long storeId;
    
    /**
     * Filter theo categoryId
     */
    private Long categoryId;
    
    /**
     * Filter theo storeCategoryId
     */
    private Long storeCategoryId;
    
    /**
     * Filter theo productType
     */
    private ProductType productType;
    
    /**
     * Filter theo status (chỉ lấy ACTIVE cho public search)
     */
    private ProductStatus status;
    
    /**
     * Filter theo giá (min)
     */
    private BigDecimal minPrice;
    
    /**
     * Filter theo giá (max)
     */
    private BigDecimal maxPrice;
    
    /**
     * Filter theo sizes
     */
    private List<String> sizes;
    
    /**
     * Filter theo colors
     */
    private List<String> colors;
    
    /**
     * Filter theo materials
     */
    private List<String> materials;
    
    /**
     * Filter theo ocopCertified
     */
    private Boolean ocopCertified;
    
    /**
     * Filter theo isVerified
     */
    private Boolean isVerified;
    
    /**
     * Filter theo hasOriginTracking
     */
    private Boolean hasOriginTracking;
    
    /**
     * Filter theo hasVariants
     */
    private Boolean hasVariants;
    
    /**
     * Filter theo isCombo
     */
    private Boolean isCombo;
    
    /**
     * Sort field (default: relevance)
     * Options: relevance, price_asc, price_desc, rating_desc, sold_count_desc, created_at_desc
     */
    private String sortBy = "relevance";
    
    /**
     * Sort direction (ASC/DESC)
     */
    private String sortDir = "DESC";
    
    /**
     * Page number (0-based)
     */
    private int page = 0;
    
    /**
     * Page size
     */
    private int size = 20;
    
    /**
     * Có highlight kết quả search không
     */
    private Boolean highlight = true;
}

