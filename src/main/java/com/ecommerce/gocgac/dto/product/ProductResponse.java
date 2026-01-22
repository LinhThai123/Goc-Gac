package com.ecommerce.gocgac.dto.product;

import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.ProductStatus;
import com.ecommerce.gocgac.entity.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private Long id;
    
    private Long storeId;
    
    private Long categoryId;
    
    private Long storeCategoryId;
    
    private String productCode;
    
    private String productName;
    
    private String slug;
    
    private String description;
    
    private String shortDescription;
    
    private ProductType productType;
    
    // Các trường cho sản phẩm phi vật lý
    private Integer durationHours;
    
    private Integer accessPeriodDays;
    
    private Boolean isUnlimitedAccess;
    
    private String deliveryMethod;
    
    private Boolean hasVariants;
    
    private Boolean isCombo;
    
    private Boolean ocopCertified;
    
    private String ocopLevel;
    
    private Boolean isVerified;
    
    private Boolean hasOriginTracking;
    
    private ApprovalStatus approvalStatus;
    
    private String rejectionReason;
    
    private Integer soldCount;
    
    private Integer viewCount;
    
    private BigDecimal ratingAverage;
    
    private Integer ratingCount;
    
    private ProductStatus status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime approvedAt;
    
    private Long approvedBy;
    
    private LocalDateTime deletedAt; // Thời gian xóa mềm
    
    // Danh sách SKUs
    private List<SkuResponse> skus;
}

