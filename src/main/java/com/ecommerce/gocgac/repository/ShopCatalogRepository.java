package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.ShopCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopCatalogRepository extends JpaRepository<ShopCatalog, Long> {
    
    /**
     * Tìm catalog theo store_id (owner)
     */
    List<ShopCatalog> findAllByStoreId(Long storeId);
    
    /**
     * Tìm catalog theo store_id và active
     */
    List<ShopCatalog> findAllByStoreIdAndIsActiveTrue(Long storeId);
    
    /**
     * Tìm catalog theo cooperative_id
     */
    List<ShopCatalog> findAllByCooperativeId(Long cooperativeId);
    
    /**
     * Tìm catalog theo catalog_code
     */
    Optional<ShopCatalog> findByCatalogCode(String catalogCode);
    
    /**
     * Tìm catalog theo id và store_id (để đảm bảo multi-tenant)
     */
    Optional<ShopCatalog> findByIdAndStoreId(Long id, Long storeId);
    
    /**
     * Kiểm tra catalog_code đã tồn tại chưa
     */
    boolean existsByCatalogCode(String catalogCode);
    
    /**
     * Kiểm tra catalog_code đã tồn tại cho store khác chưa (trừ catalog hiện tại)
     */
    boolean existsByCatalogCodeAndIdNot(String catalogCode, Long id);
    
    /**
     * Tìm tất cả catalogs featured
     */
    List<ShopCatalog> findAllByIsFeaturedTrueAndIsActiveTrue();
    
    /**
     * Đếm số lượng catalogs của store
     */
    long countByStoreId(Long storeId);
}

