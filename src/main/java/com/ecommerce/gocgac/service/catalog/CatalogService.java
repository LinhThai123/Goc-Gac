package com.ecommerce.gocgac.service.catalog;

import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.catalog.AddProductToCatalogRequest;
import com.ecommerce.gocgac.dto.catalog.CatalogProductResponse;
import com.ecommerce.gocgac.dto.catalog.CatalogResponse;
import com.ecommerce.gocgac.dto.catalog.CreateCatalogRequest;
import com.ecommerce.gocgac.dto.catalog.UpdateCatalogRequest;
import com.ecommerce.gocgac.entity.CatalogProduct;
import com.ecommerce.gocgac.entity.Cooperative;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.ShopCatalog;
import com.ecommerce.gocgac.entity.Store;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.enums.UserType;
import com.ecommerce.gocgac.exception.CatalogException;
import com.ecommerce.gocgac.repository.CatalogProductRepository;
import com.ecommerce.gocgac.repository.CooperativeRepository;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.repository.ShopCatalogRepository;
import com.ecommerce.gocgac.repository.StoreRepository;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {
    
    private final ShopCatalogRepository catalogRepository;
    private final CatalogProductRepository catalogProductRepository;
    private final StoreRepository storeRepository;
    private final CooperativeRepository cooperativeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
    /**
     * Lấy storeId từ userId (tương tự ChannelService)
     */
    private Long getStoreIdByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CatalogException("User không tồn tại"));
        
        if (user.getUserType() == UserType.COOPERATIVE_MANAGER) {
            Cooperative cooperative = cooperativeRepository.findByUserId(userId)
                .orElseThrow(() -> new CatalogException("Bạn chưa có HTX"));
            
            Store store = storeRepository.findByCooperativeId(cooperative.getId())
                .orElseThrow(() -> new CatalogException("HTX của bạn chưa có Store"));
            
            return store.getId();
        } else if (user.getUserType() == UserType.SELLER) {
            Store store = storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new CatalogException("Bạn chưa có Store"));
            
            return store.getId();
        } else {
            throw new CatalogException("Chỉ COOPERATIVE_MANAGER hoặc SELLER mới có thể tạo Catalog");
        }
    }
    
    /**
     * Validate ownership - kiểm tra catalog thuộc về store của user
     */
    private void validateOwnership(Long catalogId, Long userId) {
        ShopCatalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new CatalogException("Catalog không tồn tại"));
        
        Long userStoreId = getStoreIdByUserId(userId);
        
        if (!catalog.getStoreId().equals(userStoreId)) {
            throw new CatalogException("Bạn không có quyền truy cập catalog này");
        }
    }
    
    /**
     * Generate catalog code nếu không được cung cấp
     */
    private String generateCatalogCode(Long storeId) {
        String prefix = "CAT-" + storeId + "-";
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + uniquePart;
    }
    
    /**
     * Tạo catalog mới
     */
    @Transactional
    public CatalogResponse createCatalog(Long userId, CreateCatalogRequest request) {
        // Lấy storeId từ userId
        Long storeId = getStoreIdByUserId(userId);
        
        // Lấy cooperativeId nếu có
        Long cooperativeId = null;
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new CatalogException("Store không tồn tại"));
        
        if (store.getCooperativeId() != null) {
            cooperativeId = store.getCooperativeId();
        }
        
        // Generate catalog code nếu không có
        String catalogCode = request.getCatalogCode();
        if (catalogCode == null || catalogCode.trim().isEmpty()) {
            catalogCode = generateCatalogCode(storeId);
        }
        
        // Kiểm tra catalog code đã tồn tại chưa
        if (catalogRepository.existsByCatalogCode(catalogCode)) {
            throw new CatalogException("Catalog code đã tồn tại: " + catalogCode);
        }
        
        // Tạo catalog mới
        ShopCatalog catalog = new ShopCatalog();
        catalog.setStoreId(storeId);
        catalog.setCooperativeId(cooperativeId);
        catalog.setCatalogName(XssSanitizer.sanitize(request.getCatalogName()));
        catalog.setCatalogCode(catalogCode);
        catalog.setShortDescription(XssSanitizer.sanitize(request.getShortDescription()));
        catalog.setDescription(XssSanitizer.sanitizeHtml(request.getDescription()));
        catalog.setImageUrl(XssSanitizer.sanitizeStructuredData(request.getImageUrl()));
        catalog.setBannerUrl(XssSanitizer.sanitizeStructuredData(request.getBannerUrl()));
        catalog.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        catalog.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);
        catalog.setIsActive(true);
        catalog.setCatalogSettings(request.getCatalogSettings()); // JSON string
        catalog.setProductCount(0);
        catalog.setViewCount(0);
        
        catalog = catalogRepository.save(catalog);
        
        log.info("Catalog {} created for Store {}", catalog.getId(), storeId);
        
        return convertToResponse(catalog);
    }
    
    /**
     * Lấy tất cả catalogs của store hiện tại (multi-tenant)
     */
    public List<CatalogResponse> getMyCatalogs(Long userId) {
        Long storeId = getStoreIdByUserId(userId);
        return catalogRepository.findAllByStoreIdAndIsActiveTrue(storeId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy catalog theo ID (với ownership validation)
     */
    public CatalogResponse getCatalogById(Long catalogId, Long userId) {
        validateOwnership(catalogId, userId);
        
        ShopCatalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new CatalogException("Catalog không tồn tại"));
        
        return convertToResponse(catalog);
    }
    
    /**
     * Cập nhật catalog
     */
    @Transactional
    public CatalogResponse updateCatalog(Long catalogId, Long userId, UpdateCatalogRequest request) {
        validateOwnership(catalogId, userId);
        
        ShopCatalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new CatalogException("Catalog không tồn tại"));
        
        // Cập nhật các trường
        if (request.getCatalogName() != null) {
            catalog.setCatalogName(XssSanitizer.sanitize(request.getCatalogName()));
        }
        
        if (request.getCatalogCode() != null) {
            // Kiểm tra catalog code mới đã tồn tại chưa (trừ catalog hiện tại)
            if (!request.getCatalogCode().equals(catalog.getCatalogCode()) &&
                catalogRepository.existsByCatalogCodeAndIdNot(request.getCatalogCode(), catalogId)) {
                throw new CatalogException("Catalog code đã tồn tại: " + request.getCatalogCode());
            }
            catalog.setCatalogCode(request.getCatalogCode());
        }
        
        if (request.getShortDescription() != null) {
            catalog.setShortDescription(XssSanitizer.sanitize(request.getShortDescription()));
        }
        
        if (request.getDescription() != null) {
            catalog.setDescription(XssSanitizer.sanitizeHtml(request.getDescription()));
        }
        
        if (request.getImageUrl() != null) {
            catalog.setImageUrl(XssSanitizer.sanitizeStructuredData(request.getImageUrl()));
        }
        
        if (request.getBannerUrl() != null) {
            catalog.setBannerUrl(XssSanitizer.sanitizeStructuredData(request.getBannerUrl()));
        }
        
        if (request.getDisplayOrder() != null) {
            catalog.setDisplayOrder(request.getDisplayOrder());
        }
        
        if (request.getIsFeatured() != null) {
            catalog.setIsFeatured(request.getIsFeatured());
        }
        
        if (request.getIsActive() != null) {
            catalog.setIsActive(request.getIsActive());
        }
        
        if (request.getCatalogSettings() != null) {
            catalog.setCatalogSettings(request.getCatalogSettings());
        }
        
        catalog = catalogRepository.save(catalog);
        
        log.info("Catalog {} updated by user {}", catalogId, userId);
        
        return convertToResponse(catalog);
    }
    
    /**
     * Xóa catalog (soft delete - set isActive = false)
     */
    @Transactional
    public void deleteCatalog(Long catalogId, Long userId) {
        validateOwnership(catalogId, userId);
        
        ShopCatalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new CatalogException("Catalog không tồn tại"));
        
        catalog.setIsActive(false);
        catalogRepository.save(catalog);
        
        log.info("Catalog {} deactivated by user {}", catalogId, userId);
    }
    
    /**
     * Thêm product vào catalog
     */
    @Transactional
    public CatalogProductResponse addProductToCatalog(Long catalogId, Long userId, AddProductToCatalogRequest request) {
        validateOwnership(catalogId, userId); // Đã validate catalog tồn tại và ownership
        
        // Validate product tồn tại
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new CatalogException("Product không tồn tại"));
        
        // Validate product thuộc về store của user
        Long userStoreId = getStoreIdByUserId(userId);
        if (!product.getStoreId().equals(userStoreId)) {
            throw new CatalogException("Product không thuộc về store của bạn");
        }
        
        // Validate duplicate - kiểm tra product đã có trong catalog chưa
        if (catalogProductRepository.existsByCatalogIdAndProductId(catalogId, request.getProductId())) {
            throw new CatalogException("Product đã có trong catalog này");
        }
        
        // Tạo mapping
        CatalogProduct catalogProduct = new CatalogProduct();
        catalogProduct.setCatalogId(catalogId);
        catalogProduct.setProductId(request.getProductId());
        catalogProduct.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        catalogProduct.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);
        catalogProduct.setIsActive(true);
        
        catalogProduct = catalogProductRepository.save(catalogProduct);
        
        // product_count sẽ được update tự động qua trigger
        
        log.info("Product {} added to catalog {}", request.getProductId(), catalogId);
        
        return convertToCatalogProductResponse(catalogProduct, product);
    }
    
    /**
     * Xóa product khỏi catalog
     */
    @Transactional
    public void removeProductFromCatalog(Long catalogId, Long userId, Long productId) {
        validateOwnership(catalogId, userId);
        
        // Validate product có trong catalog
        CatalogProduct catalogProduct = catalogProductRepository
            .findByCatalogIdAndProductId(catalogId, productId)
            .orElseThrow(() -> new CatalogException("Product không có trong catalog này"));
        
        catalogProductRepository.delete(catalogProduct);
        
        // product_count sẽ được update tự động qua trigger
        
        log.info("Product {} removed from catalog {}", productId, catalogId);
    }
    
    /**
     * Lấy danh sách products trong catalog
     */
    public List<CatalogProductResponse> getProductsInCatalog(Long catalogId, Long userId) {
        validateOwnership(catalogId, userId);
        
        List<CatalogProduct> catalogProducts = catalogProductRepository
            .findAllByCatalogIdAndIsActiveTrue(catalogId);
        
        return catalogProducts.stream()
            .map(cp -> {
                Product product = productRepository.findById(cp.getProductId())
                    .orElse(null);
                return convertToCatalogProductResponse(cp, product);
            })
            .filter(cpr -> cpr.getProductId() != null) // Filter out products that don't exist
            .collect(Collectors.toList());
    }
    
    /**
     * Validate duplicate product trong catalog
     */
    public boolean isProductInCatalog(Long catalogId, Long productId) {
        return catalogProductRepository.existsByCatalogIdAndProductId(catalogId, productId);
    }
    
    /**
     * Convert ShopCatalog entity to CatalogResponse DTO
     */
    private CatalogResponse convertToResponse(ShopCatalog catalog) {
        CatalogResponse response = new CatalogResponse();
        response.setId(catalog.getId());
        response.setStoreId(catalog.getStoreId());
        response.setCooperativeId(catalog.getCooperativeId());
        response.setCatalogName(catalog.getCatalogName());
        response.setCatalogCode(catalog.getCatalogCode());
        response.setShortDescription(catalog.getShortDescription());
        response.setDescription(catalog.getDescription());
        response.setImageUrl(catalog.getImageUrl());
        response.setBannerUrl(catalog.getBannerUrl());
        response.setDisplayOrder(catalog.getDisplayOrder());
        response.setIsFeatured(catalog.getIsFeatured());
        response.setIsActive(catalog.getIsActive());
        response.setCatalogSettings(catalog.getCatalogSettings());
        response.setProductCount(catalog.getProductCount());
        response.setViewCount(catalog.getViewCount());
        response.setCreatedAt(catalog.getCreatedAt());
        response.setUpdatedAt(catalog.getUpdatedAt());
        return response;
    }
    
    /**
     * Convert CatalogProduct entity to CatalogProductResponse DTO
     */
    private CatalogProductResponse convertToCatalogProductResponse(CatalogProduct catalogProduct, Product product) {
        CatalogProductResponse response = new CatalogProductResponse();
        response.setId(catalogProduct.getId());
        response.setCatalogId(catalogProduct.getCatalogId());
        response.setProductId(catalogProduct.getProductId());
        response.setDisplayOrder(catalogProduct.getDisplayOrder());
        response.setIsFeatured(catalogProduct.getIsFeatured());
        response.setIsActive(catalogProduct.getIsActive());
        response.setAddedAt(catalogProduct.getAddedAt());
        
        // Thêm thông tin product nếu có
        if (product != null) {
            response.setProductName(product.getProductName());
            response.setProductSlug(product.getSlug());
            // Có thể thêm product image từ ProductImage entity nếu cần
        }
        
        return response;
    }
}

