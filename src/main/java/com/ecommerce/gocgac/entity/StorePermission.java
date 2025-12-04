package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_permissions", 
    uniqueConstraints = @UniqueConstraint(name = "unique_store_user", columnNames = {"store_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "role_id", nullable = false)
    private Long roleId;
    
    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt = LocalDateTime.now();
    
    @Column(name = "granted_by")
    private Long grantedBy;
}

