package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_vouchers", 
    uniqueConstraints = @UniqueConstraint(name = "unique_user_voucher", columnNames = {"user_id", "voucher_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;
    
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;
    
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt = LocalDateTime.now();
}

