package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.StoreCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreCategoryRepository extends JpaRepository<StoreCategory, Long> {

    Optional<StoreCategory> findByIdAndStoreId(Long id, Long storeId);

    Optional<StoreCategory> findByIdAndStoreIdAndIsActiveTrue(Long id, Long storeId);

    boolean existsByIdAndStoreId(Long id, Long storeId);

    List<StoreCategory> findAllByStoreIdOrderByDisplayOrderAsc(Long storeId);

    List<StoreCategory> findAllByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(Long storeId);

    Page<StoreCategory> findAllByStoreId(Long storeId, Pageable pageable);

    Page<StoreCategory> findAllByStoreIdAndIsActiveTrue(Long storeId, Pageable pageable);

    List<StoreCategory> findAllByStoreIdAndParentIdIsNullOrderByDisplayOrderAsc(Long storeId);

    List<StoreCategory> findAllByStoreIdAndParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc(Long storeId);

    List<StoreCategory> findAllByStoreIdAndParentIdOrderByDisplayOrderAsc(Long storeId, Long parentId);

    List<StoreCategory> findAllByStoreIdAndParentIdAndIsActiveTrueOrderByDisplayOrderAsc(Long storeId, Long parentId);

    long countByStoreIdAndParentIdAndIsActiveTrue(Long storeId, Long parentId);
}
