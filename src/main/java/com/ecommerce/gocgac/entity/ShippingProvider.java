package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingProvider {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "provider_name", nullable = false, length = 100)
    private String providerName;
    
    @Column(name = "provider_code", nullable = false, unique = true, length = 50)
    private String providerCode;
    
    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;
    
    @Column(name = "api_key")
    private String apiKey;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

