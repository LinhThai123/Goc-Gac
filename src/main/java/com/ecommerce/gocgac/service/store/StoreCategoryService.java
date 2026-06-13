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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreCategoryService {

    private final StoreCategoryRepository storeCategoryRepository;
    private final StoreRepository storeRepository;
    private final CooperativeRepository cooperativeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

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

    private void requireStoreExists(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new StoreCategoryException("Store không tồn tại");
        }
    }

    private Integer calculateLevel(Long storeId, Long parentId) {
        if (parentId == null) {
            return 1;
        }

        StoreCategory parent = storeCategoryRepository.findByIdAndStoreId(parentId, storeId)
            .orElseThrow(() -> new StoreCategoryException("Parent category không tồn tại hoặc không thuộc về store của bạn"));

        return parent.getLevel() + 1;
    }

    private void validateParentCategory(Long storeId, Long categoryId, Long parentId) {
        if (parentId == null) {
            return;
        }

        if (categoryId != null && categoryId.equals(parentId)) {
            throw new StoreCategoryException("Category không thể là parent của chính nó");
        }

        StoreCategory parent = storeCategoryRepository.findByIdAndStoreId(parentId, storeId)
            .orElseThrow(() -> new StoreCategoryException("Parent category không tồn tại hoặc không thuộc về store của bạn"));

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

    private void validateOwnership(Long categoryId, Long userId) {
        Long storeId = getStoreIdByUserId(userId);

        if (!storeCategoryRepository.existsByIdAndStoreId(categoryId, storeId)) {
            throw new StoreCategoryException("Store category không tồn tại hoặc không thuộc về store của bạn");
        }
    }

    private void requireActiveParent(Long storeId, Long parentId) {
        storeCategoryRepository.findByIdAndStoreIdAndIsActiveTrue(parentId, storeId)
            .orElseThrow(() -> new StoreCategoryException("Parent category không tồn tại hoặc đã bị vô hiệu hóa"));
    }

    @Transactional
    public StoreCategoryResponse createStoreCategory(Long userId, CreateStoreCategoryRequest request) {
        Long storeId = getStoreIdByUserId(userId);

        validateParentCategory(storeId, null, request.getParentId());
        Integer level = calculateLevel(storeId, request.getParentId());

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

    public List<StoreCategoryResponse> getMyStoreCategories(Long userId) {
        Long storeId = getStoreIdByUserId(userId);
        return storeCategoryRepository.findAllByStoreIdOrderByDisplayOrderAsc(storeId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public Page<StoreCategoryResponse> getMyStoreCategories(Long userId, Pageable pageable) {
        Long storeId = getStoreIdByUserId(userId);
        return storeCategoryRepository.findAllByStoreId(storeId, pageable)
            .map(this::convertToResponse);
    }

    public List<StoreCategoryResponse> getMyActiveStoreCategories(Long userId) {
        Long storeId = getStoreIdByUserId(userId);
        return storeCategoryRepository.findAllByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(storeId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public Page<StoreCategoryResponse> getMyActiveStoreCategories(Long userId, Pageable pageable) {
        Long storeId = getStoreIdByUserId(userId);
        return storeCategoryRepository.findAllByStoreIdAndIsActiveTrue(storeId, pageable)
            .map(this::convertToResponse);
    }

    public List<StoreCategoryResponse> getStoreCategoryTree(Long userId, boolean activeOnly) {
        Long storeId = getStoreIdByUserId(userId);
        List<StoreCategory> categories = activeOnly
            ? storeCategoryRepository.findAllByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(storeId)
            : storeCategoryRepository.findAllByStoreIdOrderByDisplayOrderAsc(storeId);
        return buildCategoryTree(categories);
    }

    public StoreCategoryResponse getStoreCategoryById(Long categoryId, Long userId) {
        validateOwnership(categoryId, userId);

        Long storeId = getStoreIdByUserId(userId);
        StoreCategory category = storeCategoryRepository.findByIdAndStoreId(categoryId, storeId)
            .orElseThrow(() -> new StoreCategoryException("Store category không tồn tại"));

        return convertToResponse(category);
    }

    public List<StoreCategoryResponse> getRootStoreCategories(Long userId, boolean activeOnly) {
        Long storeId = getStoreIdByUserId(userId);
        List<StoreCategory> roots = activeOnly
            ? storeCategoryRepository.findAllByStoreIdAndParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc(storeId)
            : storeCategoryRepository.findAllByStoreIdAndParentIdIsNullOrderByDisplayOrderAsc(storeId);
        return roots.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public List<StoreCategoryResponse> getChildStoreCategories(Long parentId, Long userId, boolean activeOnly) {
        validateOwnership(parentId, userId);

        Long storeId = getStoreIdByUserId(userId);
        List<StoreCategory> children = activeOnly
            ? storeCategoryRepository.findAllByStoreIdAndParentIdAndIsActiveTrueOrderByDisplayOrderAsc(storeId, parentId)
            : storeCategoryRepository.findAllByStoreIdAndParentIdOrderByDisplayOrderAsc(storeId, parentId);
        return children.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    // ========== Public (storefront) ==========

    public List<StoreCategoryResponse> getPublicActiveCategories(Long storeId) {
        requireStoreExists(storeId);
        return storeCategoryRepository.findAllByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(storeId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public List<StoreCategoryResponse> getPublicCategoryTree(Long storeId) {
        requireStoreExists(storeId);
        List<StoreCategory> categories = storeCategoryRepository
            .findAllByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(storeId);
        return buildCategoryTree(categories);
    }

    public List<StoreCategoryResponse> getPublicRootCategories(Long storeId) {
        requireStoreExists(storeId);
        return storeCategoryRepository
            .findAllByStoreIdAndParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc(storeId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public List<StoreCategoryResponse> getPublicChildCategories(Long storeId, Long parentId) {
        requireStoreExists(storeId);
        requireActiveParent(storeId, parentId);
        return storeCategoryRepository
            .findAllByStoreIdAndParentIdAndIsActiveTrueOrderByDisplayOrderAsc(storeId, parentId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public StoreCategoryResponse updateStoreCategory(Long categoryId, Long userId, UpdateStoreCategoryRequest request) {
        validateOwnership(categoryId, userId);

        Long storeId = getStoreIdByUserId(userId);
        StoreCategory category = storeCategoryRepository.findByIdAndStoreId(categoryId, storeId)
            .orElseThrow(() -> new StoreCategoryException("Store category không tồn tại"));

        boolean parentChanged = false;

        if (Boolean.TRUE.equals(request.getMakeRoot())) {
            if (category.getParentId() != null) {
                validateParentCategory(storeId, categoryId, null);
                category.setParentId(null);
                category.setLevel(1);
                parentChanged = true;
            }
        } else if (request.getParentId() != null && !request.getParentId().equals(category.getParentId())) {
            validateParentCategory(storeId, categoryId, request.getParentId());
            category.setParentId(request.getParentId());
            category.setLevel(calculateLevel(storeId, request.getParentId()));
            parentChanged = true;
        }

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

        if (parentChanged) {
            updateDescendantLevels(storeId, category.getId(), category.getLevel());
        }

        log.info("StoreCategory {} updated for Store {}", categoryId, storeId);

        return convertToResponse(category);
    }

    @Transactional
    public void deleteStoreCategory(Long categoryId, Long userId) {
        validateOwnership(categoryId, userId);

        Long storeId = getStoreIdByUserId(userId);
        StoreCategory category = storeCategoryRepository.findByIdAndStoreId(categoryId, storeId)
            .orElseThrow(() -> new StoreCategoryException("Store category không tồn tại"));

        long childCount = storeCategoryRepository.countByStoreIdAndParentIdAndIsActiveTrue(storeId, categoryId);
        if (childCount > 0) {
            throw new StoreCategoryException("Không thể xóa category vì còn " + childCount + " category con đang active");
        }

        long productCount = productRepository.countByStoreIdAndStoreCategoryId(storeId, categoryId);
        if (productCount > 0) {
            throw new StoreCategoryException(
                "Không thể xóa category vì còn " + productCount + " sản phẩm đang sử dụng category này");
        }

        category.setIsActive(false);
        storeCategoryRepository.save(category);

        log.info("StoreCategory {} deactivated for Store {}", categoryId, storeId);
    }

    private void updateDescendantLevels(Long storeId, Long parentCategoryId, int parentLevel) {
        List<StoreCategory> children = storeCategoryRepository
            .findAllByStoreIdAndParentIdOrderByDisplayOrderAsc(storeId, parentCategoryId);
        for (StoreCategory child : children) {
            int newLevel = parentLevel + 1;
            if (child.getLevel() == null || child.getLevel() != newLevel) {
                child.setLevel(newLevel);
                storeCategoryRepository.save(child);
            }
            updateDescendantLevels(storeId, child.getId(), newLevel);
        }
    }

    private List<StoreCategoryResponse> buildCategoryTree(List<StoreCategory> categories) {
        Map<Long, List<StoreCategory>> childrenByParentId = new HashMap<>();
        List<StoreCategory> roots = new ArrayList<>();

        for (StoreCategory category : categories) {
            if (category.getParentId() == null) {
                roots.add(category);
            } else {
                childrenByParentId
                    .computeIfAbsent(category.getParentId(), key -> new ArrayList<>())
                    .add(category);
            }
        }

        roots.sort(Comparator.comparing(StoreCategory::getDisplayOrder));
        return roots.stream()
            .map(root -> toTreeNode(root, childrenByParentId))
            .collect(Collectors.toList());
    }

    private StoreCategoryResponse toTreeNode(StoreCategory category, Map<Long, List<StoreCategory>> childrenByParentId) {
        StoreCategoryResponse response = convertToResponse(category);

        List<StoreCategory> children = childrenByParentId.getOrDefault(category.getId(), List.of());
        if (!children.isEmpty()) {
            children.sort(Comparator.comparing(StoreCategory::getDisplayOrder));
            response.setChildren(children.stream()
                .map(child -> toTreeNode(child, childrenByParentId))
                .collect(Collectors.toList()));
        }

        return response;
    }

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
