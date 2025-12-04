package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_origin_tracking", 
    uniqueConstraints = @UniqueConstraint(name = "unique_product_tracking", columnNames = "product_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductOriginTracking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;
    
    @Column(name = "qr_code", unique = true)
    private String qrCode;
    
    @Column(name = "raw_material_info", columnDefinition = "TEXT")
    private String rawMaterialInfo;
    
    @Column(name = "production_process", columnDefinition = "TEXT")
    private String productionProcess;
    
    @Column(name = "packaging_info", columnDefinition = "TEXT")
    private String packagingInfo;
    
    @Column(name = "labeling_info", columnDefinition = "TEXT")
    private String labelingInfo;
    
    @Column(name = "supplier_info", columnDefinition = "TEXT")
    private String supplierInfo;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

