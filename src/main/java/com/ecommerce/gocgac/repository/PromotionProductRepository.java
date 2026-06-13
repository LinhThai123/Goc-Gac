package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.PromotionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long> {

    List<PromotionProduct> findByPromotionId(Long promotionId);

    boolean existsByPromotionIdAndProductId(Long promotionId, Long productId);

    void deleteByPromotionIdAndProductId(Long promotionId, Long productId);

    List<PromotionProduct> findByProductId(Long productId);
}
