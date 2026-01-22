package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.ProductStatus;
import com.ecommerce.gocgac.entity.enums.ProductType;
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
    @Index(name = "idx_slug_products", columnList = "slug"),
    @Index(name = "idx_product_type", columnList = "product_type")
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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType = ProductType.PHYSICAL;
    
    // Lưu ý: Vì mỗi Product đều bắt buộc có ít nhất 1 SKU,
    // nên các thông tin về giá, tồn kho, kích thước, trọng lượng
    // sẽ được quản lý ở cấp SKU (ProductVariant)
    // Product chỉ lưu thông tin chung về sản phẩm
    
    // Các trường cho sản phẩm phi vật lý (khóa học, dịch vụ...)
    @Column(name = "duration_hours")
    private Integer durationHours; // Thời lượng (giờ) - cho khóa học
    
    @Column(name = "access_period_days")
    private Integer accessPeriodDays; // Thời gian truy cập (ngày)
    
    @Column(name = "is_unlimited_access")
    private Boolean isUnlimitedAccess = false; // Truy cập không giới hạn
    
    @Column(name = "delivery_method", length = 50)
    private String deliveryMethod; // Phương thức giao hàng: "download", "email", "online_access", etc.
    
    // Cho biết sản phẩm có nhiều biến thể (SKU) hay không
    @Column(name = "has_variants", nullable = false)
    private Boolean hasVariants = false;
    
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
    
    // Lưu ý: Tồn kho được quản lý ở cấp SKU (ProductVariant)
    // Product không lưu stock_quantity vì mỗi Product đều có ít nhất 1 SKU
    
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
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // Thời gian xóa mềm
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

