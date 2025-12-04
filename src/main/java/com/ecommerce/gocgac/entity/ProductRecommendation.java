package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.RecommendationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_recommendations", indexes = {
    @Index(name = "idx_source", columnList = "source_product_id"),
    @Index(name = "idx_product_recommendations_type", columnList = "recommendation_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "source_product_id", nullable = false)
    private Long sourceProductId;
    
    @Column(name = "recommended_product_id", nullable = false)
    private Long recommendedProductId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_type", nullable = false)
    private RecommendationType recommendationType;
    
    @Column(precision = 5, scale = 4, nullable = false)
    private BigDecimal score = BigDecimal.ZERO;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

