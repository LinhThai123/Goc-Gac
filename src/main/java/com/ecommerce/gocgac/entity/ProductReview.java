package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_reviews", indexes = {
    @Index(name = "idx_product_reviews_product", columnList = "product_id"),
    @Index(name = "idx_product_reviews_user", columnList = "user_id"),
    @Index(name = "idx_rating", columnList = "rating"),
    @Index(name = "idx_product_reviews_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(nullable = false)
    @jakarta.validation.constraints.Min(1)
    @jakarta.validation.constraints.Max(5)
    private Integer rating;
    
    @Column(name = "review_title")
    private String reviewTitle;
    
    @Column(name = "review_content", columnDefinition = "TEXT")
    private String reviewContent;
    
    @Column(columnDefinition = "TEXT")
    private String images;
    
    @Column(name = "helpful_count", nullable = false)
    private Integer helpfulCount = 0;
    
    @Column(name = "is_verified_purchase", nullable = false)
    private Boolean isVerifiedPurchase = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

