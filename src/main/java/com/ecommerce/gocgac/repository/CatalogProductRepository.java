package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.CatalogProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogProductRepository extends JpaRepository<CatalogProduct, Long> {
    
    /**
     * Tìm tất cả products trong catalog
     */
    List<CatalogProduct> findAllByCatalogId(Long catalogId);
    
    /**
     * Tìm tất cả products active trong catalog
     */
    List<CatalogProduct> findAllByCatalogIdAndIsActiveTrue(Long catalogId);
    
    /**
     * Tìm tất cả products featured trong catalog
     */
    List<CatalogProduct> findAllByCatalogIdAndIsFeaturedTrueAndIsActiveTrue(Long catalogId);
    
    /**
     * Tìm mapping theo catalog_id và product_id
     */
    Optional<CatalogProduct> findByCatalogIdAndProductId(Long catalogId, Long productId);
    
    /**
     * Kiểm tra product đã có trong catalog chưa
     */
    boolean existsByCatalogIdAndProductId(Long catalogId, Long productId);
    
    /**
     * Tìm tất cả catalogs chứa product
     */
    List<CatalogProduct> findAllByProductId(Long productId);
    
    /**
     * Đếm số lượng products trong catalog
     */
    long countByCatalogId(Long catalogId);
    
    /**
     * Đếm số lượng products active trong catalog
     */
    long countByCatalogIdAndIsActiveTrue(Long catalogId);
    
    /**
     * Xóa product khỏi catalog
     */
    void deleteByCatalogIdAndProductId(Long catalogId, Long productId);
    
    /**
     * Xóa tất cả products khỏi catalog
     */
    void deleteAllByCatalogId(Long catalogId);
    
    /**
     * Xóa tất cả catalogs chứa product (xóa product khỏi tất cả catalogs)
     */
    void deleteAllByProductId(Long productId);
    
    /**
     * Tìm products trong catalog với pagination (sử dụng native query)
     */
    @Query(value = "SELECT * FROM catalog_products WHERE catalog_id = :catalogId AND is_active = true ORDER BY display_order ASC, added_at DESC", nativeQuery = true)
    List<CatalogProduct> findActiveProductsByCatalogId(@Param("catalogId") Long catalogId);
}

