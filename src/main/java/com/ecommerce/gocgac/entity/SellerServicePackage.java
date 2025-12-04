package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.PackageLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "seller_service_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellerServicePackage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "package_name", nullable = false, length = 100)
    private String packageName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "package_level")
    private PackageLevel packageLevel;
    
    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
    
    @Column(columnDefinition = "TEXT")
    private String benefits;
    
    @Column(name = "max_products")
    private Integer maxProducts;
    
    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;
    
    @Column(name = "priority_support", nullable = false)
    private Boolean prioritySupport = false;
    
    @Column(nullable = false)
    private String status = "active"; // active, inactive
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

