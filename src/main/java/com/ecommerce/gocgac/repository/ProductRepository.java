package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.ProductStatus;
import com.ecommerce.gocgac.entity.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * Tìm products của store với pagination
     */
    Page<Product> findAllByStoreId(Long storeId, Pageable pageable);
    
    /**
     * Tìm products của store với filter theo status
     */
    Page<Product> findAllByStoreIdAndStatus(Long storeId, ProductStatus status, Pageable pageable);
    
    /**
     * Tìm products của store với filter theo approval status
     */
    Page<Product> findAllByStoreIdAndApprovalStatus(Long storeId, ApprovalStatus approvalStatus, Pageable pageable);
    
    /**
     * Tìm products của store với filter theo product type
     */
    Page<Product> findAllByStoreIdAndProductType(Long storeId, ProductType productType, Pageable pageable);
    
    /**
     * Tìm products của store với filter theo category
     */
    Page<Product> findAllByStoreIdAndCategoryId(Long storeId, Long categoryId, Pageable pageable);
    
    /**
     * Tìm products của store với filter theo hasVariants
     */
    Page<Product> findAllByStoreIdAndHasVariants(Long storeId, Boolean hasVariants, Pageable pageable);
    
    /**
     * Tìm products của store với search theo tên sản phẩm
     */
    @Query("SELECT p FROM Product p WHERE p.storeId = :storeId AND " +
           "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> findAllByStoreIdAndProductNameContaining(
        @Param("storeId") Long storeId, 
        @Param("keyword") String keyword, 
        Pageable pageable
    );
    
    /**
     * Tìm products với nhiều filter kết hợp
     */
    @Query("SELECT p FROM Product p WHERE p.storeId = :storeId " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:approvalStatus IS NULL OR p.approvalStatus = :approvalStatus) " +
           "AND (:productType IS NULL OR p.productType = :productType) " +
           "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
           "AND (:hasVariants IS NULL OR p.hasVariants = :hasVariants) " +
           "AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> findAllByStoreIdWithFilters(
        @Param("storeId") Long storeId,
        @Param("status") ProductStatus status,
        @Param("approvalStatus") ApprovalStatus approvalStatus,
        @Param("productType") ProductType productType,
        @Param("categoryId") Long categoryId,
        @Param("hasVariants") Boolean hasVariants,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    
    /**
     * Tìm products cho admin với nhiều filter kết hợp
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:storeId IS NULL OR p.storeId = :storeId) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:approvalStatus IS NULL OR p.approvalStatus = :approvalStatus) " +
           "AND (:productType IS NULL OR p.productType = :productType) " +
           "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
           "AND (:hasVariants IS NULL OR p.hasVariants = :hasVariants) " +
           "AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> findAllWithFilters(
        @Param("storeId") Long storeId,
        @Param("status") ProductStatus status,
        @Param("approvalStatus") ApprovalStatus approvalStatus,
        @Param("productType") ProductType productType,
        @Param("categoryId") Long categoryId,
        @Param("hasVariants") Boolean hasVariants,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    
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
    
    // ========== Public Queries (chỉ lấy sản phẩm đã APPROVED và ACTIVE) ==========
    
    /**
     * Tìm products public (đã APPROVED và ACTIVE) với filters
     */
    @Query("SELECT p FROM Product p WHERE p.approvalStatus = 'APPROVED' AND p.status = 'ACTIVE' " +
           "AND (:storeId IS NULL OR p.storeId = :storeId) " +
           "AND (:productType IS NULL OR p.productType = :productType) " +
           "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
           "AND (:hasVariants IS NULL OR p.hasVariants = :hasVariants) " +
           "AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> findPublicProducts(
        @Param("storeId") Long storeId,
        @Param("productType") ProductType productType,
        @Param("categoryId") Long categoryId,
        @Param("hasVariants") Boolean hasVariants,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    
    /**
     * Tìm product public theo slug
     */
    @Query("SELECT p FROM Product p WHERE p.slug = :slug AND p.approvalStatus = 'APPROVED' AND p.status = 'ACTIVE'")
    Optional<Product> findPublicProductBySlug(@Param("slug") String slug);
    
    /**
     * Tìm products public của store
     */
    @Query("SELECT p FROM Product p WHERE p.storeId = :storeId AND p.approvalStatus = 'APPROVED' AND p.status = 'ACTIVE'")
    Page<Product> findPublicProductsByStoreId(@Param("storeId") Long storeId, Pageable pageable);
    
    /**
     * Tìm products public theo category
     */
    @Query("SELECT p FROM Product p WHERE p.categoryId = :categoryId AND p.approvalStatus = 'APPROVED' AND p.status = 'ACTIVE'")
    Page<Product> findPublicProductsByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * Tìm products public theo danh sách productIds
     */
    @Query("SELECT p FROM Product p WHERE p.id IN :productIds AND p.approvalStatus = 'APPROVED' AND p.status = 'ACTIVE'")
    Page<Product> findPublicProductsByIds(@Param("productIds") List<Long> productIds, Pageable pageable);
    
    /**
     * Tìm products public theo danh sách productIds (không pagination)
     */
    @Query("SELECT p FROM Product p WHERE p.id IN :productIds AND p.approvalStatus = 'APPROVED' AND p.status = 'ACTIVE'")
    List<Product> findPublicProductsByIdsList(@Param("productIds") List<Long> productIds);
    
    /**
     * Đếm số lượng products của store theo storeCategoryId
     */
    long countByStoreIdAndStoreCategoryId(Long storeId, Long storeCategoryId);

    long countByCategoryId(Long categoryId);
}

