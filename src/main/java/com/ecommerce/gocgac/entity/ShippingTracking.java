package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_tracking", indexes = {
    @Index(name = "idx_shipping_tracking_order", columnList = "order_id"),
    @Index(name = "idx_shipping_tracking_code", columnList = "tracking_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingTracking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "provider_id")
    private Long providerId;
    
    @Column(name = "tracking_code", length = 100)
    private String trackingCode;
    
    @Column(name = "current_status", length = 100)
    private String currentStatus;
    
    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;
    
    @Column(name = "actual_delivery")
    private LocalDateTime actualDelivery;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

