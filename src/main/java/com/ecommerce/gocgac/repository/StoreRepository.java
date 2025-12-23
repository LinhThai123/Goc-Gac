package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    Optional<Store> findBySellerId(Long sellerId);
    
    Optional<Store> findByStoreCode(String storeCode);
    
    boolean existsByStoreCode(String storeCode);
}

