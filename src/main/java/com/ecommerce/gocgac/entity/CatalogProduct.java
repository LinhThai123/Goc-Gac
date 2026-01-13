package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity cho CatalogProduct (Mapping products vào catalog)
 * Many-to-Many relationship giữa ShopCatalog và Product
 */
@Entity
@Table(name = "catalog_products",
       uniqueConstraints = @UniqueConstraint(columnNames = {"catalog_id", "product_id"}),
       indexes = {
           @Index(name = "idx_catalog_products_catalog", columnList = "catalog_id"),
           @Index(name = "idx_catalog_products_product", columnList = "product_id"),
           @Index(name = "idx_catalog_products_active", columnList = "is_active"),
           @Index(name = "idx_catalog_products_featured", columnList = "is_featured")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatalogProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "catalog_id", nullable = false)
    private Long catalogId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    // ========== Display settings trong catalog ==========
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt = LocalDateTime.now();
}

