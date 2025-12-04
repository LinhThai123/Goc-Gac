package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "banners", indexes = {
    @Index(name = "idx_banners_position", columnList = "banner_position"),
    @Index(name = "idx_banners_active", columnList = "is_active"),
    @Index(name = "idx_banners_dates", columnList = "start_date, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Banner {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "banner_name", nullable = false)
    private String bannerName;
    
    @Column(name = "banner_position", nullable = false, length = 100)
    private String bannerPosition;
    
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
    
    @Column(name = "mobile_image_url", length = 500)
    private String mobileImageUrl;
    
    @Column(name = "link_url", length = 500)
    private String linkUrl;
    
    @Column(name = "target_type", nullable = false)
    private String targetType = "_self"; // _self, _blank
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

