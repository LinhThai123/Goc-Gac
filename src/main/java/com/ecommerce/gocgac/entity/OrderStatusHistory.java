package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history", indexes = @Index(name = "idx_order_status_history_order", columnList = "order_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "from_status", length = 50)
    private String fromStatus;
    
    @Column(name = "to_status", nullable = false, length = 50)
    private String toStatus;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "created_by")
    private Long createdBy;
}

