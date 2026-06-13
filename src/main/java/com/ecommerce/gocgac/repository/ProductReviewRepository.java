package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.ProductReview;
import com.ecommerce.gocgac.entity.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    Page<ProductReview> findByProductIdAndStatusOrderByCreatedAtDesc(Long productId, ReviewStatus status, Pageable pageable);

    Page<ProductReview> findByStatusOrderByCreatedAtDesc(ReviewStatus status, Pageable pageable);

    long countByProductIdAndStatus(Long productId, ReviewStatus status);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM ProductReview r " +
           "WHERE r.productId = :productId AND r.status = :status")
    Double averageRating(@Param("productId") Long productId, @Param("status") ReviewStatus status);
}
