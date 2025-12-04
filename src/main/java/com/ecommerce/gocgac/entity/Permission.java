package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_name", columnList = "name"),
    @Index(name = "idx_permission_code", columnList = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String code; // Format: resource:action (e.g., "product:create", "order:view")
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "resource", length = 50)
    private String resource; // product, order, user, etc.
    
    @Column(name = "action", length = 50)
    private String action; // create, read, update, delete, approve, etc.

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
