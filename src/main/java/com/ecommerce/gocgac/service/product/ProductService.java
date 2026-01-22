package com.ecommerce.gocgac.service.product;

import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.product.CreateProductRequest;
import com.ecommerce.gocgac.dto.product.ProductResponse;
import com.ecommerce.gocgac.dto.product.SkuRequest;
import com.ecommerce.gocgac.dto.product.SkuResponse;
import com.ecommerce.gocgac.dto.product.UpdateProductRequest;
import com.ecommerce.gocgac.entity.CatalogProduct;
import com.ecommerce.gocgac.entity.Cooperative;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.ProductVariant;
import com.ecommerce.gocgac.entity.ShopCatalog;
import com.ecommerce.gocgac.entity.Store;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.ProductStatus;
import com.ecommerce.gocgac.entity.enums.ProductType;
import com.ecommerce.gocgac.entity.enums.UserType;
import com.ecommerce.gocgac.exception.ProductException;
import com.ecommerce.gocgac.repository.CatalogProductRepository;
import com.ecommerce.gocgac.repository.CooperativeRepository;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.repository.ProductVariantRepository;
import com.ecommerce.gocgac.repository.ShopCatalogRepository;
import com.ecommerce.gocgac.repository.StoreRepository;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;
    private final CooperativeRepository cooperativeRepository;
    private final UserRepository userRepository;
    private final CatalogProductRepository catalogProductRepository;
    private final ShopCatalogRepository shopCatalogRepository;
    private final ProductSearchService productSearchService;
    
    /**
     * Lấy storeId từ userId
     */
    private Long getStoreIdByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ProductException("User không tồn tại"));
        
        if (user.getUserType() == UserType.COOPERATIVE_MANAGER) {
            Cooperative cooperative = cooperativeRepository.findByUserId(userId)
                .orElseThrow(() -> new ProductException("Bạn chưa có HTX"));
            
            Store store = storeRepository.findByCooperativeId(cooperative.getId())
                .orElseThrow(() -> new ProductException("HTX của bạn chưa có Store"));
            
            return store.getId();
        } else if (user.getUserType() == UserType.SELLER) {
            Store store = storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new ProductException("Bạn chưa có Store"));
            
            return store.getId();
        } else {
            throw new ProductException("Chỉ COOPERATIVE_MANAGER hoặc SELLER mới có thể tạo sản phẩm");
        }
    }
    
    /**
     * Generate product code nếu không được cung cấp
     */
    private String generateProductCode(Long storeId) {
        String prefix = "PROD-" + storeId + "-";
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + uniquePart;
    }
    
    /**
     * Generate slug từ product name
     */
    private String generateSlug(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return UUID.randomUUID().toString().substring(0, 8);
        }
        
        // Normalize và chuyển thành slug
        String normalized = Normalizer.normalize(productName, Normalizer.Form.NFD);
        String slug = normalized
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "") // Xóa dấu
            .toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "") // Chỉ giữ chữ, số, khoảng trắng, dấu gạch ngang
            .replaceAll("\\s+", "-") // Thay khoảng trắng bằng dấu gạch ngang
            .replaceAll("-+", "-") // Xóa dấu gạch ngang trùng lặp
            .replaceAll("^-|-$", ""); // Xóa dấu gạch ngang ở đầu và cuối
        
        // Nếu slug quá dài, cắt bớt
        if (slug.length() > 500) {
            slug = slug.substring(0, 500);
        }
        
        // Nếu slug trống, dùng UUID
        if (slug.isEmpty()) {
            slug = UUID.randomUUID().toString().substring(0, 8);
        }
        
        // Kiểm tra slug đã tồn tại chưa, nếu có thì thêm số
        String finalSlug = slug;
        int counter = 1;
        while (productRepository.existsBySlug(finalSlug)) {
            finalSlug = slug + "-" + counter;
            counter++;
        }
        
        return finalSlug;
    }
    
    /**
     * Tạo sản phẩm mới
     */
    @Transactional
    public ProductResponse createProduct(Long userId, CreateProductRequest request) {
        // Lấy storeId từ userId
        Long storeId = getStoreIdByUserId(userId);
        
        // Validate SKU không trùng
        for (SkuRequest skuRequest : request.getSkus()) {
            if (productVariantRepository.existsBySku(skuRequest.getSku())) {
                throw new ProductException("SKU đã tồn tại: " + skuRequest.getSku());
            }
        }
        
        // Generate product code nếu không có
        String productCode = request.getProductCode();
        if (productCode == null || productCode.trim().isEmpty()) {
            productCode = generateProductCode(storeId);
        } else {
            // Kiểm tra product code đã tồn tại chưa
            if (productRepository.existsByProductCode(productCode)) {
                throw new ProductException("Mã sản phẩm đã tồn tại: " + productCode);
            }
        }
        
        // Generate slug nếu không có
        String slug = request.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(request.getProductName());
        } else {
            // Kiểm tra slug đã tồn tại chưa
            if (productRepository.existsBySlug(slug)) {
                throw new ProductException("Slug đã tồn tại: " + slug);
            }
        }
        
        // Xác định hasVariants
        boolean hasVariants = request.getSkus().size() > 1;
        
        // Tạo Product
        Product product = new Product();
        product.setStoreId(storeId);
        product.setCategoryId(request.getCategoryId());
        product.setStoreCategoryId(request.getStoreCategoryId());
        product.setProductCode(productCode);
        product.setProductName(XssSanitizer.sanitize(request.getProductName()));
        product.setSlug(slug);
        product.setDescription(XssSanitizer.sanitizeHtml(request.getDescription()));
        product.setShortDescription(XssSanitizer.sanitize(request.getShortDescription()));
        product.setProductType(request.getProductType());
        product.setDurationHours(request.getDurationHours());
        product.setAccessPeriodDays(request.getAccessPeriodDays());
        product.setIsUnlimitedAccess(request.getIsUnlimitedAccess() != null ? request.getIsUnlimitedAccess() : false);
        product.setDeliveryMethod(request.getDeliveryMethod());
        product.setHasVariants(hasVariants);
        product.setIsCombo(request.getIsCombo() != null ? request.getIsCombo() : false);
        product.setOcopCertified(request.getOcopCertified() != null ? request.getOcopCertified() : false);
        product.setOcopLevel(request.getOcopLevel());
        product.setHasOriginTracking(request.getHasOriginTracking() != null ? request.getHasOriginTracking() : false);
        
        Product savedProduct = productRepository.save(product);
        
        log.info("Product {} created for Store {}", savedProduct.getId(), storeId);
        
        // Tạo các SKU
        List<ProductVariant> variants = request.getSkus().stream()
            .map(skuRequest -> {
                ProductVariant variant = new ProductVariant();
                variant.setProductId(savedProduct.getId());
                variant.setSku(skuRequest.getSku());
                variant.setVariantName(XssSanitizer.sanitize(skuRequest.getVariantName()));
                variant.setSize(skuRequest.getSize());
                variant.setColor(skuRequest.getColor());
                variant.setMaterial(skuRequest.getMaterial());
                variant.setWeightVariant(skuRequest.getWeightVariant());
                variant.setBarcode(skuRequest.getBarcode());
                variant.setPrice(skuRequest.getPrice());
                variant.setComparePrice(skuRequest.getComparePrice());
                variant.setCostPrice(skuRequest.getCostPrice());
                variant.setStockQuantity(skuRequest.getStockQuantity() != null ? skuRequest.getStockQuantity() : 0);
                variant.setReservedQuantity(0);
                variant.setImageUrl(XssSanitizer.sanitizeStructuredData(skuRequest.getImageUrl()));
                variant.setWeightKg(skuRequest.getWeightKg());
                variant.setLengthCm(skuRequest.getLengthCm());
                variant.setWidthCm(skuRequest.getWidthCm());
                variant.setHeightCm(skuRequest.getHeightCm());
                variant.setStatus(skuRequest.getStatus() != null ? skuRequest.getStatus() : "active");
                variant.setDisplayOrder(skuRequest.getDisplayOrder() != null ? skuRequest.getDisplayOrder() : 0);
                
                return variant;
            })
            .collect(Collectors.toList());
        
        variants = productVariantRepository.saveAll(variants);
        
        log.info("Created {} SKUs for Product {}", variants.size(), savedProduct.getId());
        
        // Index vào Elasticsearch (async - không block)
        try {
            productSearchService.indexProduct(savedProduct.getId());
        } catch (Exception e) {
            log.error("Failed to index product {} to Elasticsearch: {}", savedProduct.getId(), e.getMessage());
            // Không throw exception để không ảnh hưởng đến flow chính
        }
        
        // Convert to response
        return convertToProductResponse(savedProduct, variants);
    }
    
    /**
     * Cập nhật thông tin sản phẩm
     */
    @Transactional
    public ProductResponse updateProduct(Long userId, Long productId, UpdateProductRequest request) {
        // Lấy storeId từ userId
        Long storeId = getStoreIdByUserId(userId);
        
        // Tìm product và kiểm tra quyền
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductException("Sản phẩm không tồn tại"));
        
        if (!product.getStoreId().equals(storeId)) {
            throw new ProductException("Bạn không có quyền cập nhật sản phẩm này");
        }
        
        // Lấy danh sách SKU hiện tại
        List<ProductVariant> existingVariants = productVariantRepository.findAllByProductId(productId);
        
        // Xử lý SKUs nếu có trong request
        if (request.getSkus() != null && !request.getSkus().isEmpty()) {
            // Validate SKU không trùng (trừ SKU hiện tại của sản phẩm này)
            for (SkuRequest skuRequest : request.getSkus()) {
                if (skuRequest.getId() == null) {
                    // SKU mới - kiểm tra SKU code không trùng
                    if (productVariantRepository.existsBySku(skuRequest.getSku())) {
                        throw new ProductException("SKU đã tồn tại: " + skuRequest.getSku());
                    }
                } else {
                    // SKU cũ - kiểm tra SKU code không trùng với SKU khác (trừ chính nó)
                    ProductVariant existingVariant = productVariantRepository.findById(skuRequest.getId())
                        .orElseThrow(() -> new ProductException("SKU không tồn tại với ID: " + skuRequest.getId()));
                    
                    if (!existingVariant.getProductId().equals(productId)) {
                        throw new ProductException("SKU không thuộc về sản phẩm này");
                    }
                    
                    // Nếu SKU code thay đổi, kiểm tra không trùng với SKU khác
                    if (!existingVariant.getSku().equals(skuRequest.getSku())) {
                        if (productVariantRepository.existsBySku(skuRequest.getSku())) {
                            throw new ProductException("SKU đã tồn tại: " + skuRequest.getSku());
                        }
                    }
                }
            }
            
            // Xác định SKUs cần xóa (SKUs hiện tại không có trong request)
            List<Long> requestSkuIds = request.getSkus().stream()
                .map(SkuRequest::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
            
            List<ProductVariant> variantsToDelete = existingVariants.stream()
                .filter(v -> !requestSkuIds.contains(v.getId()))
                .collect(Collectors.toList());
            
            // Đảm bảo sau khi xóa vẫn còn ít nhất 1 SKU
            int remainingSkusCount = existingVariants.size() - variantsToDelete.size() 
                + request.getSkus().stream().filter(s -> s.getId() == null).mapToInt(s -> 1).sum();
            
            if (remainingSkusCount < 1) {
                throw new ProductException("Sản phẩm phải có ít nhất 1 SKU");
            }
            
            // Xóa các SKU không có trong request
            if (!variantsToDelete.isEmpty()) {
                productVariantRepository.deleteAll(variantsToDelete);
                log.info("Deleted {} SKUs for Product {}", variantsToDelete.size(), productId);
            }
            
            // Cập nhật và tạo mới SKUs
            List<ProductVariant> updatedVariants = new java.util.ArrayList<>();
            
            for (SkuRequest skuRequest : request.getSkus()) {
                ProductVariant variant;
                
                if (skuRequest.getId() != null) {
                    // Update existing SKU
                    variant = productVariantRepository.findById(skuRequest.getId())
                        .orElseThrow(() -> new ProductException("SKU không tồn tại với ID: " + skuRequest.getId()));
                } else {
                    // Create new SKU
                    variant = new ProductVariant();
                    variant.setProductId(productId);
                    variant.setReservedQuantity(0);
                }
                
                // Cập nhật thông tin SKU
                variant.setSku(skuRequest.getSku());
                variant.setVariantName(XssSanitizer.sanitize(skuRequest.getVariantName()));
                variant.setSize(skuRequest.getSize());
                variant.setColor(skuRequest.getColor());
                variant.setMaterial(skuRequest.getMaterial());
                variant.setWeightVariant(skuRequest.getWeightVariant());
                variant.setBarcode(skuRequest.getBarcode());
                variant.setPrice(skuRequest.getPrice());
                variant.setComparePrice(skuRequest.getComparePrice());
                variant.setCostPrice(skuRequest.getCostPrice());
                variant.setStockQuantity(skuRequest.getStockQuantity() != null ? skuRequest.getStockQuantity() : 0);
                variant.setImageUrl(XssSanitizer.sanitizeStructuredData(skuRequest.getImageUrl()));
                variant.setWeightKg(skuRequest.getWeightKg());
                variant.setLengthCm(skuRequest.getLengthCm());
                variant.setWidthCm(skuRequest.getWidthCm());
                variant.setHeightCm(skuRequest.getHeightCm());
                variant.setStatus(skuRequest.getStatus() != null ? skuRequest.getStatus() : "active");
                variant.setDisplayOrder(skuRequest.getDisplayOrder() != null ? skuRequest.getDisplayOrder() : 0);
                
                updatedVariants.add(variant);
            }
            
            updatedVariants = productVariantRepository.saveAll(updatedVariants);
            log.info("Updated {} SKUs for Product {}", updatedVariants.size(), productId);
            
            // Cập nhật hasVariants
            boolean hasVariants = updatedVariants.size() > 1;
            product.setHasVariants(hasVariants);
        } else {
            // Không có SKUs trong request, giữ nguyên SKUs hiện tại
            // Chỉ cập nhật hasVariants nếu được chỉ định
            if (request.getHasVariants() != null) {
                product.setHasVariants(request.getHasVariants());
            }
        }
        
        // Cập nhật thông tin Product (chỉ những field không null)
        if (request.getProductName() != null) {
            product.setProductName(XssSanitizer.sanitize(request.getProductName()));
        }
        
        if (request.getProductCode() != null) {
            // Kiểm tra product code không trùng (trừ chính nó)
            if (!product.getProductCode().equals(request.getProductCode())) {
                if (productRepository.existsByProductCode(request.getProductCode())) {
                    throw new ProductException("Mã sản phẩm đã tồn tại: " + request.getProductCode());
                }
            }
            product.setProductCode(request.getProductCode());
        }
        
        if (request.getSlug() != null) {
            // Kiểm tra slug không trùng (trừ chính nó)
            if (!product.getSlug().equals(request.getSlug())) {
                if (productRepository.existsBySlug(request.getSlug())) {
                    throw new ProductException("Slug đã tồn tại: " + request.getSlug());
                }
            }
            product.setSlug(request.getSlug());
        }
        
        if (request.getDescription() != null) {
            product.setDescription(XssSanitizer.sanitizeHtml(request.getDescription()));
        }
        
        if (request.getShortDescription() != null) {
            product.setShortDescription(XssSanitizer.sanitize(request.getShortDescription()));
        }
        
        if (request.getProductType() != null) {
            product.setProductType(request.getProductType());
        }
        
        if (request.getCategoryId() != null) {
            product.setCategoryId(request.getCategoryId());
        }
        
        if (request.getStoreCategoryId() != null) {
            product.setStoreCategoryId(request.getStoreCategoryId());
        }
        
        if (request.getDurationHours() != null) {
            product.setDurationHours(request.getDurationHours());
        }
        
        if (request.getAccessPeriodDays() != null) {
            product.setAccessPeriodDays(request.getAccessPeriodDays());
        }
        
        if (request.getIsUnlimitedAccess() != null) {
            product.setIsUnlimitedAccess(request.getIsUnlimitedAccess());
        }
        
        if (request.getDeliveryMethod() != null) {
            product.setDeliveryMethod(request.getDeliveryMethod());
        }
        
        if (request.getIsCombo() != null) {
            product.setIsCombo(request.getIsCombo());
        }
        
        if (request.getOcopCertified() != null) {
            product.setOcopCertified(request.getOcopCertified());
        }
        
        if (request.getOcopLevel() != null) {
            product.setOcopLevel(request.getOcopLevel());
        }
        
        if (request.getHasOriginTracking() != null) {
            product.setHasOriginTracking(request.getHasOriginTracking());
        }
        
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        
        // Nếu sản phẩm đã APPROVED và có thay đổi quan trọng, chuyển về DRAFT để admin review lại
        if (product.getApprovalStatus() == ApprovalStatus.APPROVED) {
            boolean hasImportantChanges = request.getProductName() != null 
                || request.getDescription() != null 
                || request.getProductType() != null
                || (request.getSkus() != null && !request.getSkus().isEmpty());
            
            if (hasImportantChanges) {
                product.setApprovalStatus(ApprovalStatus.DRAFT);
                product.setRejectionReason(null);
                log.info("Product {} status changed to DRAFT due to important updates", productId);
            }
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product {} updated for Store {}", updatedProduct.getId(), storeId);
        
        // Index lại vào Elasticsearch (async - không block)
        try {
            productSearchService.indexProduct(updatedProduct.getId());
        } catch (Exception e) {
            log.error("Failed to re-index product {} to Elasticsearch: {}", updatedProduct.getId(), e.getMessage());
            // Không throw exception để không ảnh hưởng đến flow chính
        }
        
        // Lấy lại danh sách SKUs sau khi cập nhật
        List<ProductVariant> finalVariants = productVariantRepository.findAllByProductId(productId);
        
        // Convert to response
        return convertToProductResponse(updatedProduct, finalVariants);
    }
    
    /**
     * Xóa sản phẩm
     * - Nếu sản phẩm ở trạng thái ACTIVE: Xóa mềm (soft delete) - chuyển sang DELETED
     * - Nếu sản phẩm ở trạng thái không hoạt động (INACTIVE, OUT_OF_STOCK, DELETED): Xóa cứng (hard delete) - xóa khỏi database
     */
    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        // Lấy storeId từ userId
        Long storeId = getStoreIdByUserId(userId);
        
        // Tìm product và kiểm tra quyền
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductException("Sản phẩm không tồn tại"));
        
        if (!product.getStoreId().equals(storeId)) {
            throw new ProductException("Bạn không có quyền xóa sản phẩm này");
        }
        
        // Kiểm tra trạng thái sản phẩm
        if (product.getStatus() == ProductStatus.ACTIVE) {
            // Soft delete: Chuyển sang trạng thái DELETED
            product.setStatus(ProductStatus.DELETED);
            product.setDeletedAt(LocalDateTime.now());
            productRepository.save(product);
            log.info("Product {} soft deleted (status changed to DELETED)", productId);
            
            // Xóa khỏi Elasticsearch
            try {
                productSearchService.deleteProductFromIndex(productId);
            } catch (Exception e) {
                log.error("Failed to delete product {} from Elasticsearch: {}", productId, e.getMessage());
            }
        } else {
            // Hard delete: Xóa khỏi database
            // Xóa các bản ghi liên quan trong catalog_products trước (nếu có CASCADE thì không cần)
            catalogProductRepository.deleteAllByProductId(productId);
            
            // Xóa các SKUs
            productVariantRepository.deleteAllByProductId(productId);
            
            // Xóa sản phẩm (các bảng khác sẽ được xóa tự động nhờ CASCADE)
            productRepository.delete(product);
            log.info("Product {} hard deleted from database", productId);
            
            // Xóa khỏi Elasticsearch
            try {
                productSearchService.deleteProductFromIndex(productId);
            } catch (Exception e) {
                log.error("Failed to delete product {} from Elasticsearch: {}", productId, e.getMessage());
            }
        }
    }
    
    /**
     * Convert Product entity to ProductResponse
     */
    private ProductResponse convertToProductResponse(Product product, List<ProductVariant> variants) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setStoreId(product.getStoreId());
        response.setCategoryId(product.getCategoryId());
        response.setStoreCategoryId(product.getStoreCategoryId());
        response.setProductCode(product.getProductCode());
        response.setProductName(product.getProductName());
        response.setSlug(product.getSlug());
        response.setDescription(product.getDescription());
        response.setShortDescription(product.getShortDescription());
        response.setProductType(product.getProductType());
        response.setDurationHours(product.getDurationHours());
        response.setAccessPeriodDays(product.getAccessPeriodDays());
        response.setIsUnlimitedAccess(product.getIsUnlimitedAccess());
        response.setDeliveryMethod(product.getDeliveryMethod());
        response.setHasVariants(product.getHasVariants());
        response.setIsCombo(product.getIsCombo());
        response.setOcopCertified(product.getOcopCertified());
        response.setOcopLevel(product.getOcopLevel());
        response.setIsVerified(product.getIsVerified());
        response.setHasOriginTracking(product.getHasOriginTracking());
        response.setApprovalStatus(product.getApprovalStatus());
        response.setRejectionReason(product.getRejectionReason());
        response.setSoldCount(product.getSoldCount());
        response.setViewCount(product.getViewCount());
        response.setRatingAverage(product.getRatingAverage());
        response.setRatingCount(product.getRatingCount());
        response.setStatus(product.getStatus());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setApprovedAt(product.getApprovedAt());
        response.setApprovedBy(product.getApprovedBy());
        response.setDeletedAt(product.getDeletedAt());
        
        // Convert variants
        List<SkuResponse> skuResponses = variants.stream()
            .map(this::convertToSkuResponse)
            .collect(Collectors.toList());
        response.setSkus(skuResponses);
        
        return response;
    }
    
    /**
     * Convert ProductVariant entity to SkuResponse
     */
    private SkuResponse convertToSkuResponse(ProductVariant variant) {
        SkuResponse response = new SkuResponse();
        response.setId(variant.getId());
        response.setProductId(variant.getProductId());
        response.setVariantName(variant.getVariantName());
        response.setSku(variant.getSku());
        response.setSize(variant.getSize());
        response.setColor(variant.getColor());
        response.setMaterial(variant.getMaterial());
        response.setWeightVariant(variant.getWeightVariant());
        response.setBarcode(variant.getBarcode());
        response.setPrice(variant.getPrice());
        response.setComparePrice(variant.getComparePrice());
        response.setCostPrice(variant.getCostPrice());
        response.setStockQuantity(variant.getStockQuantity());
        response.setReservedQuantity(variant.getReservedQuantity());
        response.setAvailableQuantity(variant.getAvailableQuantity());
        response.setImageUrl(variant.getImageUrl());
        response.setWeightKg(variant.getWeightKg());
        response.setLengthCm(variant.getLengthCm());
        response.setWidthCm(variant.getWidthCm());
        response.setHeightCm(variant.getHeightCm());
        response.setStatus(variant.getStatus());
        response.setDisplayOrder(variant.getDisplayOrder());
        response.setCreatedAt(variant.getCreatedAt());
        response.setUpdatedAt(variant.getUpdatedAt());
        return response;
    }
    
    /**
     * Lấy danh sách sản phẩm của store hiện tại với pagination và filtering
     */
    public Page<ProductResponse> getProducts(
            Long userId,
            ProductStatus status,
            ApprovalStatus approvalStatus,
            ProductType productType,
            Long categoryId,
            Boolean hasVariants,
            String keyword,
            Pageable pageable) {
        
        // Lấy storeId từ userId
        Long storeId = getStoreIdByUserId(userId);
        
        // Lấy danh sách products với filters
        Page<Product> products = productRepository.findAllByStoreIdWithFilters(
            storeId,
            status,
            approvalStatus,
            productType,
            categoryId,
            hasVariants,
            keyword,
            pageable
        );
        
        // Convert to response (có thể lazy load SKUs nếu cần)
        List<ProductResponse> productResponses = products.getContent().stream()
            .map(product -> {
                // Lấy SKUs của product
                List<ProductVariant> variants = productVariantRepository.findAllByProductId(product.getId());
                return convertToProductResponse(product, variants);
            })
            .collect(Collectors.toList());
        
        return new PageImpl<>(productResponses, pageable, products.getTotalElements());
    }
    
    /**
     * Lấy thông tin sản phẩm theo ID (với ownership validation)
     */
    public ProductResponse getProductById(Long productId, Long userId) {
        Long storeId = getStoreIdByUserId(userId);
        
        Product product = productRepository.findByIdAndStoreId(productId, storeId)
            .orElseThrow(() -> new ProductException("Sản phẩm không tồn tại hoặc không thuộc về store của bạn"));
        
        // Lấy SKUs của product
        List<ProductVariant> variants = productVariantRepository.findAllByProductId(productId);
        
        return convertToProductResponse(product, variants);
    }
    
    /**
     * Lấy danh sách sản phẩm đơn giản (không có SKUs) để hiển thị trong list
     */
    public Page<ProductResponse> getProductsSimple(
            Long userId,
            ProductStatus status,
            ApprovalStatus approvalStatus,
            ProductType productType,
            Long categoryId,
            Boolean hasVariants,
            String keyword,
            Pageable pageable) {
        
        Long storeId = getStoreIdByUserId(userId);
        
        Page<Product> products = productRepository.findAllByStoreIdWithFilters(
            storeId,
            status,
            approvalStatus,
            productType,
            categoryId,
            hasVariants,
            keyword,
            pageable
        );
        
        // Convert to response không có SKUs (lazy load)
        List<ProductResponse> productResponses = products.getContent().stream()
            .map(product -> {
                ProductResponse response = convertToProductResponse(product, List.of());
                // Có thể set null hoặc empty list cho SKUs để tiết kiệm query
                response.setSkus(null);
                return response;
            })
            .collect(Collectors.toList());
        
        return new PageImpl<>(productResponses, pageable, products.getTotalElements());
    }
    
    // ========== Public Methods (cho người dùng chưa đăng nhập và người mua hàng) ==========
    
    /**
     * Lấy danh sách sản phẩm public (đã APPROVED và ACTIVE) - không cần authentication
     */
    public Page<ProductResponse> getPublicProducts(
            Long storeId,
            ProductType productType,
            Long categoryId,
            Boolean hasVariants,
            String keyword,
            Pageable pageable) {
        
        // Chỉ lấy sản phẩm đã APPROVED và ACTIVE
        Page<Product> products = productRepository.findPublicProducts(
            storeId,
            productType,
            categoryId,
            hasVariants,
            keyword,
            pageable
        );
        
        // Convert to response với SKUs (chỉ SKU active)
        List<ProductResponse> productResponses = products.getContent().stream()
            .map(product -> {
                // Lấy chỉ SKUs active
                List<ProductVariant> variants = productVariantRepository.findAllByProductIdAndStatus(
                    product.getId(), "active"
                );
                return convertToProductResponse(product, variants);
            })
            .collect(Collectors.toList());
        
        return new PageImpl<>(productResponses, pageable, products.getTotalElements());
    }
    
    /**
     * Lấy thông tin chi tiết sản phẩm public theo slug
     */
    public ProductResponse getPublicProductBySlug(String slug) {
        Product product = productRepository.findPublicProductBySlug(slug)
            .orElseThrow(() -> new ProductException("Sản phẩm không tồn tại hoặc chưa được duyệt"));
        
        // Tăng view count
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
        
        // Lấy chỉ SKUs active
        List<ProductVariant> variants = productVariantRepository.findAllByProductIdAndStatus(
            product.getId(), "active"
        );
        
        return convertToProductResponse(product, variants);
    }
    
    /**
     * Lấy thông tin chi tiết sản phẩm public theo ID
     */
    public ProductResponse getPublicProductById(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductException("Sản phẩm không tồn tại"));
        
        // Kiểm tra sản phẩm đã được APPROVED và ACTIVE chưa
        if (product.getApprovalStatus() != ApprovalStatus.APPROVED || 
            product.getStatus() != ProductStatus.ACTIVE) {
            throw new ProductException("Sản phẩm chưa được duyệt hoặc không còn hoạt động");
        }
        
        // Tăng view count
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
        
        // Lấy chỉ SKUs active
        List<ProductVariant> variants = productVariantRepository.findAllByProductIdAndStatus(
            productId, "active"
        );
        
        return convertToProductResponse(product, variants);
    }
    
    /**
     * Lấy danh sách sản phẩm public của store
     */
    public Page<ProductResponse> getPublicProductsByStoreId(Long storeId, Pageable pageable) {
        Page<Product> products = productRepository.findPublicProductsByStoreId(storeId, pageable);
        
        List<ProductResponse> productResponses = products.getContent().stream()
            .map(product -> {
                List<ProductVariant> variants = productVariantRepository.findAllByProductIdAndStatus(
                    product.getId(), "active"
                );
                return convertToProductResponse(product, variants);
            })
            .collect(Collectors.toList());
        
        return new PageImpl<>(productResponses, pageable, products.getTotalElements());
    }
    
    /**
     * Lấy danh sách sản phẩm public theo category
     */
    public Page<ProductResponse> getPublicProductsByCategoryId(Long categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findPublicProductsByCategoryId(categoryId, pageable);
        
        List<ProductResponse> productResponses = products.getContent().stream()
            .map(product -> {
                List<ProductVariant> variants = productVariantRepository.findAllByProductIdAndStatus(
                    product.getId(), "active"
                );
                return convertToProductResponse(product, variants);
            })
            .collect(Collectors.toList());
        
        return new PageImpl<>(productResponses, pageable, products.getTotalElements());
    }
    
    /**
     * Lấy danh sách sản phẩm public theo catalog
     */
    public Page<ProductResponse> getPublicProductsByCatalogId(Long catalogId, Pageable pageable) {
        // Kiểm tra catalog tồn tại và active
        ShopCatalog catalog = shopCatalogRepository.findById(catalogId)
            .orElseThrow(() -> new ProductException("Catalog không tồn tại"));
        
        if (!catalog.getIsActive()) {
            throw new ProductException("Catalog không còn hoạt động");
        }
        
        // Lấy danh sách products trong catalog (chỉ active)
        List<CatalogProduct> catalogProducts = catalogProductRepository
            .findAllByCatalogIdAndIsActiveTrue(catalogId);
        
        // Lấy productIds
        List<Long> productIds = catalogProducts.stream()
            .map(CatalogProduct::getProductId)
            .collect(Collectors.toList());
        
        if (productIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        
        // Lấy products public (đã APPROVED và ACTIVE) từ danh sách productIds với pagination
        Page<Product> products = productRepository.findPublicProductsByIds(productIds, pageable);
        
        // Convert to response
        List<ProductResponse> productResponses = products.getContent().stream()
            .map(product -> {
                List<ProductVariant> variants = productVariantRepository.findAllByProductIdAndStatus(
                    product.getId(), "active"
                );
                return convertToProductResponse(product, variants);
            })
            .collect(Collectors.toList());
        
        return new PageImpl<>(productResponses, pageable, products.getTotalElements());
    }
}

