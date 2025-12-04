package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.StockTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions", indexes = {
    @Index(name = "idx_stock_transactions_product", columnList = "product_id"),
    @Index(name = "idx_stock_transactions_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "variant_id")
    private Long variantId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private StockTransactionType transactionType;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "reference_type", length = 50)
    private String referenceType;
    
    @Column(name = "reference_id")
    private Long referenceId;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "created_by")
    private Long createdBy;
}

