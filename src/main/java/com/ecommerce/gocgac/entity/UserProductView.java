package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_product_views", indexes = {
    @Index(name = "idx_user_product_views_user", columnList = "user_id"),
    @Index(name = "idx_user_product_views_product", columnList = "product_id"),
    @Index(name = "idx_viewed", columnList = "viewed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProductView {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "view_duration")
    private Integer viewDuration;
    
    @Column(name = "viewed_at", nullable = false, updatable = false)
    private LocalDateTime viewedAt = LocalDateTime.now();
}

