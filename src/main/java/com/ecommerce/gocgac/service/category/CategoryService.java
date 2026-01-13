package com.ecommerce.gocgac.service.category;

import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.category.CategoryResponse;
import com.ecommerce.gocgac.dto.category.CreateCategoryRequest;
import com.ecommerce.gocgac.dto.category.UpdateCategoryRequest;
import com.ecommerce.gocgac.entity.Category;
import com.ecommerce.gocgac.exception.CategoryException;
import com.ecommerce.gocgac.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    /**
     * Generate slug từ category name (Vietnamese to slug)
     */
    private String generateSlug(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new CategoryException("Category name không được để trống");
        }
        
        // Normalize Vietnamese characters
        String normalized = Normalizer.normalize(categoryName, Normalizer.Form.NFD);
        
        // Remove diacritics (dấu)
        String withoutDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        
        // Convert to lowercase
        String lowercased = withoutDiacritics.toLowerCase();
        
        // Replace spaces and special characters with hyphens
        String slug = lowercased
            .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters except spaces and hyphens
            .replaceAll("\\s+", "-") // Replace spaces with hyphens
            .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
            .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
        
        // Ensure slug is not empty
        if (slug.isEmpty()) {
            slug = "category-" + System.currentTimeMillis();
        }
        
        return slug;
    }
    
    /**
     * Tạo slug unique bằng cách thêm số nếu slug đã tồn tại
     */
    private String generateUniqueSlug(String baseSlug) {
        String slug = baseSlug;
        int counter = 1;
        
        while (categoryRepository.existsByCategorySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }
    
    /**
     * Tính level của category dựa trên parent
     */
    private Integer calculateLevel(Long parentId) {
        if (parentId == null) {
            return 1; // Root category
        }
        
        Category parent = categoryRepository.findById(parentId)
            .orElseThrow(() -> new CategoryException("Parent category không tồn tại"));
        
        return parent.getLevel() + 1;
    }
    
    /**
     * Validate parent category (không được là chính nó, không được là con của nó)
     */
    private void validateParentCategory(Long categoryId, Long parentId) {
        if (parentId == null) {
            return; // Root category, không cần validate
        }
        
        if (categoryId != null && categoryId.equals(parentId)) {
            throw new CategoryException("Category không thể là parent của chính nó");
        }
        
        // Kiểm tra parent có tồn tại không
        Category parent = categoryRepository.findById(parentId)
            .orElseThrow(() -> new CategoryException("Parent category không tồn tại"));
        
        // Kiểm tra circular reference (parent không được là con của category hiện tại)
        if (categoryId != null) {
            Long checkParentId = parent.getParentId();
            while (checkParentId != null) {
                if (checkParentId.equals(categoryId)) {
                    throw new CategoryException("Không thể tạo circular reference giữa các categories");
                }
                Category checkParent = categoryRepository.findById(checkParentId)
                    .orElseThrow(() -> new CategoryException("Parent category không tồn tại"));
                checkParentId = checkParent.getParentId();
            }
        }
    }
    
    /**
     * Tạo category mới
     */
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        // Generate slug nếu không được cung cấp
        String categorySlug = request.getCategorySlug();
        if (categorySlug == null || categorySlug.trim().isEmpty()) {
            categorySlug = generateSlug(request.getCategoryName());
        }
        
        // Validate slug format
        if (!categorySlug.matches("^[a-z0-9-]+$")) {
            throw new CategoryException("Category slug không hợp lệ. Chỉ chứa chữ thường, số và dấu gạch ngang");
        }
        
        // Kiểm tra slug đã tồn tại chưa
        if (categoryRepository.existsByCategorySlug(categorySlug)) {
            categorySlug = generateUniqueSlug(categorySlug);
        }
        
        // Validate parent category
        validateParentCategory(null, request.getParentId());
        
        // Tính level
        Integer level = calculateLevel(request.getParentId());
        
        // Tạo category mới
        Category category = new Category();
        category.setParentId(request.getParentId());
        category.setCategoryName(XssSanitizer.sanitize(request.getCategoryName()));
        category.setCategorySlug(categorySlug);
        category.setDescription(XssSanitizer.sanitizeHtml(request.getDescription()));
        category.setImageUrl(XssSanitizer.sanitizeStructuredData(request.getImageUrl()));
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        category.setLevel(level);
        category.setIsActive(true);
        
        category = categoryRepository.save(category);
        
        log.info("Category {} created with slug {}", category.getId(), categorySlug);
        
        return convertToResponse(category);
    }
    
    /**
     * Lấy tất cả categories
     */
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy tất cả categories active
     */
    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findAllByIsActiveTrue().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy category theo ID
     */
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new CategoryException("Category không tồn tại"));
        
        return convertToResponse(category);
    }
    
    /**
     * Lấy category theo slug
     */
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findByCategorySlug(slug)
            .orElseThrow(() -> new CategoryException("Category không tồn tại"));
        
        return convertToResponse(category);
    }
    
    /**
     * Lấy tất cả categories con của một category
     */
    public List<CategoryResponse> getChildCategories(Long parentId) {
        return categoryRepository.findAllByParentIdAndIsActiveTrue(parentId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy tất cả root categories (parent_id = null)
     */
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findAllByParentIdIsNullAndIsActiveTrue().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Cập nhật category
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new CategoryException("Category không tồn tại"));
        
        // Validate parent category nếu có thay đổi
        if (request.getParentId() != null && !request.getParentId().equals(category.getParentId())) {
            validateParentCategory(id, request.getParentId());
            
            // Tính lại level
            Integer newLevel = calculateLevel(request.getParentId());
            category.setLevel(newLevel);
            category.setParentId(request.getParentId());
        }
        
        // Cập nhật slug nếu có
        if (request.getCategorySlug() != null && !request.getCategorySlug().trim().isEmpty()) {
            String newSlug = request.getCategorySlug();
            
            // Validate slug format
            if (!newSlug.matches("^[a-z0-9-]+$")) {
                throw new CategoryException("Category slug không hợp lệ. Chỉ chứa chữ thường, số và dấu gạch ngang");
            }
            
            // Kiểm tra slug đã tồn tại cho category khác chưa
            if (!newSlug.equals(category.getCategorySlug()) && 
                categoryRepository.existsByCategorySlugAndIdNot(newSlug, id)) {
                throw new CategoryException("Category slug đã tồn tại: " + newSlug);
            }
            
            category.setCategorySlug(newSlug);
        }
        
        // Cập nhật các trường khác
        if (request.getCategoryName() != null) {
            category.setCategoryName(XssSanitizer.sanitize(request.getCategoryName()));
        }
        
        if (request.getDescription() != null) {
            category.setDescription(XssSanitizer.sanitizeHtml(request.getDescription()));
        }
        
        if (request.getImageUrl() != null) {
            category.setImageUrl(XssSanitizer.sanitizeStructuredData(request.getImageUrl()));
        }
        
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        
        category = categoryRepository.save(category);
        
        log.info("Category {} updated", id);
        
        return convertToResponse(category);
    }
    
    /**
     * Xóa category (soft delete - set isActive = false)
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new CategoryException("Category không tồn tại"));
        
        // Kiểm tra xem category có con không
        long childCount = categoryRepository.countByParentIdAndIsActiveTrue(id);
        if (childCount > 0) {
            throw new CategoryException("Không thể xóa category vì còn " + childCount + " category con đang active");
        }
        
        category.setIsActive(false);
        categoryRepository.save(category);
        
        log.info("Category {} deactivated", id);
    }
    
    /**
     * Convert Category entity to CategoryResponse DTO
     */
    private CategoryResponse convertToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setParentId(category.getParentId());
        response.setCategoryName(category.getCategoryName());
        response.setCategorySlug(category.getCategorySlug());
        response.setDescription(category.getDescription());
        response.setImageUrl(category.getImageUrl());
        response.setDisplayOrder(category.getDisplayOrder());
        response.setLevel(category.getLevel());
        response.setIsActive(category.getIsActive());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}

