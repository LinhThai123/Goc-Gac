package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Tìm tất cả products của store
     */
    List<Product> findAllByStoreId(Long storeId);
    
    /**
     * Tìm product theo store_id và id
     */
    Optional<Product> findByIdAndStoreId(Long id, Long storeId);
    
    /**
     * Tìm product theo slug
     */
    Optional<Product> findBySlug(String slug);
    
    /**
     * Tìm product theo product_code
     */
    Optional<Product> findByProductCode(String productCode);
    
    /**
     * Kiểm tra product_code đã tồn tại chưa
     */
    boolean existsByProductCode(String productCode);
    
    /**
     * Kiểm tra slug đã tồn tại chưa
     */
    boolean existsBySlug(String slug);
}

