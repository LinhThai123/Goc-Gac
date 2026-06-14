package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.ProductCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCommissionRepository extends JpaRepository<ProductCommission, Long> {

    Optional<ProductCommission> findByProductId(Long productId);

    List<ProductCommission> findByStoreId(Long storeId);
}
