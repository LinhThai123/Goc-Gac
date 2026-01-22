package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.StoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreCategoryRepository extends JpaRepository<StoreCategory, Long> {
    
    /**
     * Tìm store category theo ID và storeId
     */
    Optional<StoreCategory> findByIdAndStoreId(Long id, Long storeId);
    
    /**
     * Kiểm tra store category có tồn tại và thuộc về store không
     */
    boolean existsByIdAndStoreId(Long id, Long storeId);
    
    /**
     * Tìm tất cả store categories của store
     */
    List<StoreCategory> findAllByStoreId(Long storeId);
    
    /**
     * Tìm store categories active của store
     */
    List<StoreCategory> findAllByStoreIdAndIsActiveTrue(Long storeId);
}

