package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Tìm category theo slug
     */
    Optional<Category> findByCategorySlug(String categorySlug);

    Optional<Category> findByCategorySlugAndIsActiveTrue(String categorySlug);

    Optional<Category> findByIdAndIsActiveTrue(Long id);
    
    /**
     * Kiểm tra category slug đã tồn tại chưa
     */
    boolean existsByCategorySlug(String categorySlug);
    
    /**
     * Kiểm tra category slug đã tồn tại cho category khác chưa (trừ category hiện tại)
     */
    boolean existsByCategorySlugAndIdNot(String categorySlug, Long id);
    
    /**
     * Tìm tất cả categories theo parent_id
     */
    List<Category> findAllByParentId(Long parentId);
    
    /**
     * Tìm tất cả categories active
     */
    List<Category> findAllByIsActiveTrue();

    List<Category> findAllByIsActiveTrueOrderByDisplayOrderAsc();
    
    /**
     * Tìm tất cả categories active theo parent_id
     */
    List<Category> findAllByParentIdAndIsActiveTrue(Long parentId);

    List<Category> findAllByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(Long parentId);

    List<Category> findAllByParentIdOrderByDisplayOrderAsc(Long parentId);
    
    /**
     * Tìm tất cả categories theo level
     */
    List<Category> findAllByLevel(Integer level);
    
    /**
     * Tìm tất cả categories active theo level
     */
    List<Category> findAllByLevelAndIsActiveTrue(Integer level);
    
    /**
     * Tìm categories root (parent_id = null)
     */
    List<Category> findAllByParentIdIsNull();
    
    /**
     * Tìm categories root active
     */
    List<Category> findAllByParentIdIsNullAndIsActiveTrue();

    List<Category> findAllByParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

    List<Category> findAllByParentIdIsNullOrderByDisplayOrderAsc();
    
    /**
     * Đếm số lượng categories con của một category
     */
    long countByParentId(Long parentId);
    
    /**
     * Đếm số lượng categories con active của một category
     */
    long countByParentIdAndIsActiveTrue(Long parentId);
}

