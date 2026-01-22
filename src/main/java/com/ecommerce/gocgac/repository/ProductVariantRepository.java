package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    
    /**
     * Tìm tất cả variants của product
     */
    List<ProductVariant> findAllByProductId(Long productId);
    
    /**
     * Tìm variant theo SKU
     */
    Optional<ProductVariant> findBySku(String sku);
    
    /**
     * Kiểm tra SKU đã tồn tại chưa
     */
    boolean existsBySku(String sku);
    
    /**
     * Đếm số lượng variants của product
     */
    long countByProductId(Long productId);
    
    /**
     * Tìm variants active của product
     */
    List<ProductVariant> findAllByProductIdAndStatus(Long productId, String status);
    
    /**
     * Tìm variant đầu tiên (SKU mặc định) của product
     */
    Optional<ProductVariant> findFirstByProductIdOrderByDisplayOrderAscCreatedAtAsc(Long productId);
    
    /**
     * Xóa tất cả variants của product
     */
    void deleteAllByProductId(Long productId);
}

