package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_config", 
    uniqueConstraints = @UniqueConstraint(name = "unique_event_type", columnNames = "event_type"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_type", nullable = false, unique = true, length = 100)
    private String eventType;
    
    @Column(name = "event_name", nullable = false)
    private String eventName;
    
    @Column(name = "points_awarded", nullable = false)
    private Integer pointsAwarded;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

