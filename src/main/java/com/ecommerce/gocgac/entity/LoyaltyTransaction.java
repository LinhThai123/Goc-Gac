package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_transactions", 
    indexes = {
        @Index(name = "idx_user_transaction", columnList = "user_id, created_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Column(nullable = false)
    private Integer points;
    
    @Column(name = "event_type", length = 100)
    private String eventType;
    
    @Column(name = "reference_id")
    private Long referenceId;
    
    @Column(name = "reference_type", length = 50)
    private String referenceType;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

