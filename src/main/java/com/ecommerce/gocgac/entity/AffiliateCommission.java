package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.CommissionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "affiliate_commissions", indexes = {
    @Index(name = "idx_affiliate_commissions_partner", columnList = "partner_id"),
    @Index(name = "idx_affiliate_commissions_order", columnList = "order_id"),
    @Index(name = "idx_affiliate_commissions_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateCommission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "partner_id", nullable = false)
    private Long partnerId;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "order_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal orderAmount;
    
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;
    
    @Column(name = "commission_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal commissionAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionStatus status = CommissionStatus.PENDING;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

