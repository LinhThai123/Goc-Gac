package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences", 
    uniqueConstraints = @UniqueConstraint(name = "unique_user_preference", columnNames = {"user_id", "preference_key"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "preference_key", nullable = false, length = 100)
    private String preferenceKey;
    
    @Column(name = "preference_value", columnDefinition = "TEXT")
    private String preferenceValue;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

