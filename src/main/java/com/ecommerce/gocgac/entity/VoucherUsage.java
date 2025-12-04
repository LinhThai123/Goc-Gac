package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_usage", indexes = {
    @Index(name = "idx_voucher", columnList = "voucher_id"),
    @Index(name = "idx_voucher_usage_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherUsage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "used_at", nullable = false, updatable = false)
    private LocalDateTime usedAt = LocalDateTime.now();
}

