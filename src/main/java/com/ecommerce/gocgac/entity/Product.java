package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_store", columnList = "store_id"),
    @Index(name = "idx_category_products", columnList = "category_id"),
    @Index(name = "idx_status_products", columnList = "status"),
    @Index(name = "idx_approval", columnList = "approval_status"),
    @Index(name = "idx_slug_products", columnList = "slug")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "store_category_id")
    private Long storeCategoryId;
    
    @Column(name = "product_code", unique = true, length = 100)
    private String productCode;
    
    @Column(name = "product_name", nullable = false, length = 500)
    private String productName;
    
    @Column(nullable = false, length = 500)
    private String slug;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
    
    @Column(name = "compare_price", precision = 15, scale = 2)
    private BigDecimal comparePrice;
    
    @Column(name = "cost_price", precision = 15, scale = 2)
    private BigDecimal costPrice;
    
    @Column(name = "is_combo", nullable = false)
    private Boolean isCombo = false;
    
    @Column(name = "ocop_certified", nullable = false)
    private Boolean ocopCertified = false;
    
    @Column(name = "ocop_level", length = 10)
    private String ocopLevel;
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Column(name = "has_origin_tracking", nullable = false)
    private Boolean hasOriginTracking = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.DRAFT;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;
    
    @Column(name = "sold_count", nullable = false)
    private Integer soldCount = 0;
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @Column(name = "rating_average", precision = 3, scale = 2, nullable = false)
    private BigDecimal ratingAverage = BigDecimal.ZERO;
    
    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

