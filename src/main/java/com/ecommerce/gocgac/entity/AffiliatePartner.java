package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "affiliate_partners", 
    uniqueConstraints = @UniqueConstraint(name = "unique_user_affiliate", columnNames = "user_id"),
    indexes = @Index(name = "idx_affiliate_partners_code", columnList = "affiliate_code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffiliatePartner {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "affiliate_code", nullable = false, unique = true, length = 50)
    private String affiliateCode;
    
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;
    
    @Column(name = "total_clicks", nullable = false)
    private Integer totalClicks = 0;
    
    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders = 0;
    
    @Column(name = "total_revenue", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    
    @Column(name = "total_commission", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCommission = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private String status = "active"; // active, inactive, suspended
    
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();
}

