package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.DiscountType;
import com.ecommerce.gocgac.entity.enums.VoucherStatus;
import com.ecommerce.gocgac.entity.enums.VoucherType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers", indexes = {
    @Index(name = "idx_vouchers_code", columnList = "voucher_code"),
    @Index(name = "idx_vouchers_store", columnList = "store_id"),
    @Index(name = "idx_vouchers_dates", columnList = "start_date, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "store_id")
    private Long storeId;
    
    @Column(name = "voucher_code", nullable = false, unique = true, length = 50)
    private String voucherCode;
    
    @Column(name = "voucher_name", nullable = false)
    private String voucherName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "voucher_type", nullable = false)
    private VoucherType voucherType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;
    
    @Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "max_discount_amount", precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;
    
    @Column(name = "min_order_value", precision = 15, scale = 2)
    private BigDecimal minOrderValue;
    
    @Column(name = "usage_limit")
    private Integer usageLimit;
    
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;
    
    @Column(name = "user_usage_limit", nullable = false)
    private Integer userUsageLimit = 1;
    
    @Column(name = "member_rank_id")
    private Long memberRankId;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherStatus status = VoucherStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

