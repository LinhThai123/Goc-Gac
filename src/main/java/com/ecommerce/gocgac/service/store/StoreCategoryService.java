package com.ecommerce.gocgac.service.store;

import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.store.CreateStoreCategoryRequest;
import com.ecommerce.gocgac.dto.store.StoreCategoryResponse;
import com.ecommerce.gocgac.dto.store.UpdateStoreCategoryRequest;
import com.ecommerce.gocgac.entity.Cooperative;
import com.ecommerce.gocgac.entity.Store;
import com.ecommerce.gocgac.entity.StoreCategory;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.enums.UserType;
import com.ecommerce.gocgac.exception.StoreCategoryException;
import com.ecommerce.gocgac.repository.CooperativeRepository;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.repository.StoreCategoryRepository;
import com.ecommerce.gocgac.repository.StoreRepository;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service cho Store Category Management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreCategoryService {
    
    private final StoreCategoryRepository storeCategoryRepository;
    private final StoreRepository storeRepository;
    private final CooperativeRepository cooperativeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
    /**
     * Lấy storeId từ userId
     */
    private Long getStoreIdByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new StoreCategoryException("User không tồn tại"));
        
        if (user.getUserType() == UserType.COOPERATIVE_MANAGER) {
            Cooperative cooperative = cooperativeRepository.findByUserId(userId)
                .orElseThrow(() -> new StoreCategoryException("Bạn chưa có HTX"));
            
            Store store = storeRepository.findByCooperativeId(cooperative.getId())
                .orElseThrow(() -> new StoreCategoryException("HTX của bạn chưa có Store"));
            
            return store.getId();
        } else if (user.getUserType() == UserType.SELLER) {
            Store store = storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new StoreCategoryException("Bạn chưa có Store"));
            
            return store.getId();
        } else {
            throw new StoreCategoryException("Chỉ COOPERATIVE_MANAGER hoặc SELLER mới có thể quản lý store category");
        }
    }
    
    /**
     * Tính level của store category dựa trên parent
     */
    private Integer calculateLevel(Long storeId, Long parentId) {
        if (parentId == null) {
            return 1; // Root category
        }
        
        StoreCategory parent = storeCategoryRepository.findByIdAndStoreId(parentId, storeId)
            .orElseThrow(() -> new StoreCategoryException("Parent category không tồn tại hoặc không thuộc về store của bạn"));
        
        return parent.getLevel() + 1;
    }
    
    /**
     * Validate parent category (không được là chính nó, không được là con của nó)
     */
    private void validateParentCategory(Long storeId, Long categoryId, Long parentId) {
        if (parentId == null) {
            return; // Root category, không cần validate
        }
        
        if (categoryId != null && categoryId.equals(parentId)) {
            throw new StoreCategoryException("Category không thể là parent của chính nó");
        }
        
        // Kiểm tra parent có tồn tại và thuộc về store không
        StoreCategory parent = storeCategoryRepository.findByIdAndStoreId(parentId, storeId)
            .orElseThrow(() -> new StoreCategoryException("Parent category không tồn tại hoặc không thuộc về store của bạn"));
        
        // Kiểm tra circular reference (parent không được là con của category hiện tại)
        if (categoryId != null) {
            Long checkParentId = parent.getParentId();
            while (checkParentId != null) {
                if (checkParentId.equals(categoryId)) {
                    throw new StoreCategoryException("Không thể tạo circular reference giữa các categories");
                }
                StoreCategory checkParent = storeCategoryRepository.findByIdAndStoreId(checkParentId, storeId)
                    .orElseThrow(() -> new StoreCategoryException("Parent category không tồn tại"));
                checkParentId = checkParent.getParentId();
            }
        }
    }
    
    /**
     * Validate ownership - kiểm tra category thuộc về store của user
     */
    private void validateOwnership(Long categoryId, Long userId) {
        Long storeId = getStoreIdByUserId(userId);
        
        if (!storeCategoryRepository.existsByIdAndStoreId(categoryId, storeId)) {
            throw new StoreCategoryException("Store category không tồn tại hoặc không thuộc về store của bạn");
        }
    }
    
    /**
     * Tạo store category mới
     */
    @Transactional
    public StoreCategoryResponse createStoreCategory(Long userId, CreateStoreCategoryRequest request) {
        Long storeId = getStoreIdByUserId(userId);
        
        // Validate parent category nếu có
        validateParentCategory(storeId, null, request.getParentId());
        
        // Tính level
        Integer level = calculateLevel(storeId, request.getParentId());
        
        // Tạo store category mới
        StoreCategory category = new StoreCategory();
        category.setStoreId(storeId);
        category.setParentId(request.getParentId());
        category.setCategoryName(XssSanitizer.sanitize(request.getCategoryName()));
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        category.setLevel(level);
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        category = storeCategoryRepository.save(category);
        
        log.info("StoreCategory {} created for Store {}", category.getId(), storeId);
        
        return convertToResponse(category);
    }
    
    /**
     * Lấy tất cả store categories của store hiện tại
     */
    public List<StoreCategoryResponse> getMyStoreCategories(Long userId) {
        Long storeId = getStoreIdByUserId(userId);
        
        return storeCategoryRepository.findAllByStoreId(storeId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy tất cả store categories active của store hiện tại
     */
    public List<StoreCategoryResponse> getMyActiveStoreCategories(Long userId) {
        Long storeId = getStoreIdByUserId(userId);
        
        return storeCategoryRepository.findAllByStoreIdAndIsActiveTrue(storeId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy store category theo ID (với ownership validation)
     */
    public StoreCategoryResponse getStoreCategoryById(Long categoryId, Long userId) {
        validateOwnership(categoryId, userId);
        
        Long storeId = getStoreIdByUserId(userId);
        StoreCategory category = storeCategoryRepository.findByIdAndStoreId(categoryId, storeId)
            .orElseThrow(() -> new StoreCategoryException("Store category không tồn tại"));
        
        return convertToResponse(category);
    }
    
    /**
     * Lấy tất cả root categories (parent_id = null) của store hiện tại
     */
    public List<StoreCategoryResponse> getRootStoreCategories(Long userId) {
        Long storeId = getStoreIdByUserId(userId);
        
        return storeCategoryRepository.findAllByStoreIdAndIsActiveTrue(storeId).stream()
            .filter(cat -> cat.getParentId() == null)
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy tất cả child categories của một category
     */
    public List<StoreCategoryResponse> getChildStoreCategories(Long parentId, Long userId) {
        validateOwnership(parentId, userId);
        
        Long storeId = getStoreIdByUserId(userId);
        
        return storeCategoryRepository.findAllByStoreIdAndIsActiveTrue(storeId).stream()
            .filter(cat -> parentId.equals(cat.getParentId()))
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Cập nhật store category
     */
    @Transactional
    public StoreCategoryResponse updateStoreCategory(Long categoryId, Long userId, UpdateStoreCategoryRequest request) {
        validateOwnership(categoryId, userId);
        
        Long storeId = getStoreIdByUserId(userId);
        StoreCategory category = storeCategoryRepository.findByIdAndStoreId(categoryId, storeId)
            .orElseThrow(() -> new StoreCategoryException("Store category không tồn tại"));
        
        // Validate parent category nếu có thay đổi
        if (request.getParentId() != null && !request.getParentId().equals(category.getParentId())) {
            validateParentCategory(storeId, categoryId, request.getParentId());
            
            // Tính lại level
            Integer newLevel = calculateLevel(storeId, request.getParentId());
            category.setLevel(newLevel);
            category.setParentId(request.getParentId());
        }
        
        // Cập nhật các trường khác
        if (request.getCategoryName() != null) {
            category.setCategoryName(XssSanitizer.sanitize(request.getCategoryName()));
        }
        
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        
        category = storeCategoryRepository.save(category);
        
        log.info("StoreCategory {} updated for Store {}", categoryId, storeId);
        
        return convertToResponse(category);
    }
    
    /**
     * Xóa store category (soft delete - set isActive = false)
     */
    @Transactional
    public void deleteStoreCategory(Long categoryId, Long userId) {
        validateOwnership(categoryId, userId);
        
        Long storeId = getStoreIdByUserId(userId);
        StoreCategory category = storeCategoryRepository.findByIdAndStoreId(categoryId, storeId)
            .orElseThrow(() -> new StoreCategoryException("Store category không tồn tại"));
        
        // Kiểm tra xem category có con không
        long childCount = storeCategoryRepository.findAllByStoreIdAndIsActiveTrue(storeId).stream()
            .filter(cat -> categoryId.equals(cat.getParentId()))
            .count();
        
        if (childCount > 0) {
            throw new StoreCategoryException("Không thể xóa category vì còn " + childCount + " category con đang active");
        }
        
        // Kiểm tra xem có sản phẩm nào đang sử dụng category này không
        long productCount = productRepository.countByStoreIdAndStoreCategoryId(storeId, categoryId);
        if (productCount > 0) {
            throw new StoreCategoryException("Không thể xóa category vì còn " + productCount + " sản phẩm đang sử dụng category này");
        }
        
        // Soft delete
        category.setIsActive(false);
        storeCategoryRepository.save(category);
        
        log.info("StoreCategory {} deactivated for Store {}", categoryId, storeId);
    }
    
    /**
     * Convert StoreCategory entity to StoreCategoryResponse DTO
     */
    private StoreCategoryResponse convertToResponse(StoreCategory category) {
        StoreCategoryResponse response = new StoreCategoryResponse();
        response.setId(category.getId());
        response.setStoreId(category.getStoreId());
        response.setParentId(category.getParentId());
        response.setCategoryName(category.getCategoryName());
        response.setDisplayOrder(category.getDisplayOrder());
        response.setLevel(category.getLevel());
        response.setIsActive(category.getIsActive());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}

