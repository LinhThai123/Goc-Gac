package com.ecommerce.gocgac.repository.elasticsearch;

import com.ecommerce.gocgac.entity.elasticsearch.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Elasticsearch Product Search
 */
@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    
    /**
     * Tìm sản phẩm theo ID
     */
    Optional<ProductDocument> findById(Long id);
    
    /**
     * Tìm sản phẩm theo storeId
     */
    Page<ProductDocument> findByStoreId(Long storeId, Pageable pageable);
    
    /**
     * Tìm sản phẩm theo categoryId
     */
    Page<ProductDocument> findByCategoryId(Long categoryId, Pageable pageable);
    
    /**
     * Tìm sản phẩm theo productType
     */
    Page<ProductDocument> findByProductType(String productType, Pageable pageable);
    
    /**
     * Tìm sản phẩm theo status
     */
    Page<ProductDocument> findByStatus(String status, Pageable pageable);
    
    /**
     * Tìm sản phẩm theo approvalStatus
     */
    Page<ProductDocument> findByApprovalStatus(String approvalStatus, Pageable pageable);
    
    /**
     * Tìm sản phẩm theo SKU code
     */
    List<ProductDocument> findBySkuCodesContaining(String skuCode);
    
    /**
     * Tìm sản phẩm theo size
     */
    Page<ProductDocument> findBySizesContaining(String size, Pageable pageable);
    
    /**
     * Tìm sản phẩm theo color
     */
    Page<ProductDocument> findByColorsContaining(String color, Pageable pageable);
    
    /**
     * Xóa sản phẩm theo ID
     */
    void deleteById(Long id);
    
    /**
     * Kiểm tra sản phẩm có tồn tại không
     */
    boolean existsById(Long id);
}

