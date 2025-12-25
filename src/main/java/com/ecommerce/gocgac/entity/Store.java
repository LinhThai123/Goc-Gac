package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.StoreStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stores", indexes = {
    @Index(name = "idx_stores_seller", columnList = "seller_id"),
    @Index(name = "idx_stores_cooperative", columnList = "cooperative_id"),
    @Index(name = "idx_status_stores", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "seller_id")
    private Long sellerId; // Nullable - cho Seller độc lập
    
    @Column(name = "cooperative_id")
    private Long cooperativeId; // Nullable - cho HTX
    
    @Column(name = "store_name", nullable = false)
    private String storeName;
    
    @Column(name = "store_code", unique = true, length = 50)
    private String storeCode;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "logo_url", length = 500)
    private String logoUrl;
    
    @Column(name = "banner_url", length = 500)
    private String bannerUrl;
    
    @Column(name = "video_url", length = 500)
    private String videoUrl;
    
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    @Column(name = "contact_email")
    private String contactEmail;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Column(name = "is_branded", nullable = false)
    private Boolean isBranded = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreStatus status = StoreStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

