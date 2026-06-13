package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * Lấy SKU với khóa ghi bi quan (PESSIMISTIC_WRITE) — chống oversell khi
     * nhiều người cùng đặt một SKU đồng thời. Phải gọi trong một transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
    Optional<ProductVariant> findByIdForUpdate(@Param("id") Long id);
    
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

