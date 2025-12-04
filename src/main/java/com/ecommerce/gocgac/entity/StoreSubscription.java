package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_subscriptions", 
    indexes = @Index(name = "idx_store_status", columnList = "store_id, status"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreSubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "package_id", nullable = false)
    private Long packageId;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "payment_amount", precision = 15, scale = 2)
    private BigDecimal paymentAmount;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(nullable = false)
    private String status = "active"; // active, expired, cancelled
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

