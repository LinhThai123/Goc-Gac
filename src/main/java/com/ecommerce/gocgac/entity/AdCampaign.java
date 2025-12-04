package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ad_campaigns", indexes = {
    @Index(name = "idx_ad_campaigns_store", columnList = "store_id"),
    @Index(name = "idx_ad_campaigns_status", columnList = "status"),
    @Index(name = "idx_ad_campaigns_dates", columnList = "start_date, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdCampaign {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "campaign_name", nullable = false)
    private String campaignName;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "ad_image_url", length = 500)
    private String adImageUrl;
    
    @Column(name = "ad_position", length = 100)
    private String adPosition;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal budget;
    
    @Column(name = "cost_per_click", precision = 10, scale = 2)
    private BigDecimal costPerClick;
    
    @Column(name = "total_clicks", nullable = false)
    private Integer totalClicks = 0;
    
    @Column(name = "total_conversions", nullable = false)
    private Integer totalConversions = 0;
    
    @Column(name = "total_spent", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.DRAFT;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(nullable = false)
    private String status = "scheduled"; // scheduled, active, paused, completed
    
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

