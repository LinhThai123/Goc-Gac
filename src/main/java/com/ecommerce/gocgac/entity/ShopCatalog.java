package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity cho ShopCatalog (Catalog riêng cho từng shop)
 * Mỗi shop có catalog riêng để quản lý tập hợp sản phẩm của họ
 * Multi-tenant: Shop chỉ thấy và chỉnh sửa catalog của mình
 */
@Entity
@Table(name = "shop_catalogs", indexes = {
    @Index(name = "idx_shop_catalogs_store", columnList = "store_id"),
    @Index(name = "idx_shop_catalogs_cooperative", columnList = "cooperative_id"),
    @Index(name = "idx_shop_catalogs_code", columnList = "catalog_code"),
    @Index(name = "idx_shop_catalogs_active", columnList = "is_active"),
    @Index(name = "idx_shop_catalogs_featured", columnList = "is_featured")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopCatalog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId; // Owner của catalog
    
    @Column(name = "cooperative_id")
    private Long cooperativeId; // Reference đến cooperative (nếu store thuộc cooperative)
    
    // ========== Thông tin catalog ==========
    @Column(name = "catalog_name", nullable = false, length = 255)
    private String catalogName;
    
    @Column(name = "catalog_code", unique = true, length = 100)
    private String catalogCode;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "short_description", length = 500)
    private String shortDescription;
    
    // ========== Display settings ==========
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // ========== Metadata ==========
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "banner_url", length = 500)
    private String bannerUrl;
    
    // ========== Settings ==========
    @Column(name = "catalog_settings", columnDefinition = "TEXT")
    private String catalogSettings; // JSON: {"defaultSort": "price_asc", "showFilters": true}
    
    // ========== Statistics ==========
    @Column(name = "product_count", nullable = false)
    private Integer productCount = 0; // Được update tự động qua trigger
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

