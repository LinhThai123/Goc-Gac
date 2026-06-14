package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.ProductRecommendation;
import com.ecommerce.gocgac.entity.enums.RecommendationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRecommendationRepository extends JpaRepository<ProductRecommendation, Long> {

    List<ProductRecommendation> findBySourceProductIdOrderByScoreDesc(Long sourceProductId);

    List<ProductRecommendation> findBySourceProductIdInOrderByScoreDesc(List<Long> sourceProductIds);

    @Modifying
    @Query("DELETE FROM ProductRecommendation r WHERE r.recommendationType = :type")
    void deleteByRecommendationType(@Param("type") RecommendationType type);
}
