package com.ecommerce.gocgac.service.product;

import com.ecommerce.gocgac.dto.product.ProductResponse;
import com.ecommerce.gocgac.dto.product.SearchRequest;
import com.ecommerce.gocgac.dto.product.SearchResponse;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.ProductVariant;
import com.ecommerce.gocgac.entity.elasticsearch.ProductDocument;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.ProductStatus;
import com.ecommerce.gocgac.exception.ProductException;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.repository.ProductVariantRepository;
import com.ecommerce.gocgac.repository.elasticsearch.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service cho Elasticsearch Product Search
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {
    
    private final ProductSearchRepository productSearchRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    
    /**
     * Index sản phẩm vào Elasticsearch
     */
    @Transactional
    public void indexProduct(Long productId) {
        try {
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException("Sản phẩm không tồn tại: " + productId));
            
            // Lấy tất cả SKUs của sản phẩm
            List<ProductVariant> variants = productVariantRepository.findAllByProductId(productId);
            
            // Convert Product sang ProductDocument
            ProductDocument document = convertToProductDocument(product, variants);
            
            // Lưu vào Elasticsearch
            productSearchRepository.save(document);
            
            log.info("Indexed product {} to Elasticsearch", productId);
        } catch (Exception e) {
            log.error("Error indexing product {} to Elasticsearch: {}", productId, e.getMessage(), e);
            throw new ProductException("Lỗi khi index sản phẩm vào Elasticsearch: " + e.getMessage());
        }
    }
    
    /**
     * Xóa sản phẩm khỏi Elasticsearch
     */
    @Transactional
    public void deleteProductFromIndex(Long productId) {
        try {
            if (productSearchRepository.existsById(productId)) {
                productSearchRepository.deleteById(productId);
                log.info("Deleted product {} from Elasticsearch", productId);
            }
        } catch (Exception e) {
            log.error("Error deleting product {} from Elasticsearch: {}", productId, e.getMessage(), e);
            // Không throw exception để không ảnh hưởng đến flow chính
        }
    }
    
    /**
     * Re-index tất cả sản phẩm
     */
    @Transactional
    public void reIndexAllProducts() {
        try {
            log.info("Starting re-index all products...");
            
            // Lấy tất cả sản phẩm đã APPROVED và ACTIVE
            List<Product> products = productRepository.findAll();
            
            int indexed = 0;
            int failed = 0;
            
            for (Product product : products) {
                try {
                    indexProduct(product.getId());
                    indexed++;
                    
                    if (indexed % 100 == 0) {
                        log.info("Indexed {} products...", indexed);
                    }
                } catch (Exception e) {
                    failed++;
                    log.error("Failed to index product {}: {}", product.getId(), e.getMessage());
                }
            }
            
            log.info("Re-index completed. Indexed: {}, Failed: {}", indexed, failed);
        } catch (Exception e) {
            log.error("Error re-indexing all products: {}", e.getMessage(), e);
            throw new ProductException("Lỗi khi re-index tất cả sản phẩm: " + e.getMessage());
        }
    }
    
    /**
     * Search sản phẩm với full-text search và filters
     */
    public SearchResponse searchProducts(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Tạo Criteria query
            Criteria criteria = buildSearchCriteria(request);
            
            // Tạo Query
            Query query = new CriteriaQuery(criteria);
            
            // Pagination
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            query.setPageable(pageable);
            
            // Sort
            Sort sort = buildSort(request.getSortBy(), request.getSortDir());
            query.addSort(sort);
            
            // Highlight (nếu có keyword) — in đậm từ khóa trong tên & mô tả
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()
                    && Boolean.TRUE.equals(request.getHighlight())) {
                Highlight highlight = new Highlight(List.of(
                    new HighlightField("productName"),
                    new HighlightField("description"),
                    new HighlightField("shortDescription")));
                query.setHighlightQuery(new HighlightQuery(highlight, ProductDocument.class));
            }
            
            // Execute search
            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);
            
            // Convert to ProductResponse
            List<ProductResponse> products = searchHits.getSearchHits().stream()
                .map(hit -> {
                    ProductDocument doc = hit.getContent();
                    return convertToProductResponse(doc);
                })
                .collect(Collectors.toList());
            
            // Build highlights map
            Map<Long, Map<String, String>> highlights = new HashMap<>();
            if (request.getHighlight() && request.getKeyword() != null) {
                for (SearchHit<ProductDocument> hit : searchHits.getSearchHits()) {
                    Map<String, List<String>> highlightFields = hit.getHighlightFields();
                    if (highlightFields != null && !highlightFields.isEmpty()) {
                        Map<String, String> productHighlights = new HashMap<>();
                        highlightFields.forEach((field, values) -> {
                            if (!values.isEmpty()) {
                                productHighlights.put(field, values.get(0));
                            }
                        });
                        highlights.put(hit.getContent().getId(), productHighlights);
                    }
                }
            }
            
            // Build response
            SearchResponse response = new SearchResponse();
            response.setProducts(products);
            response.setTotalElements(searchHits.getTotalHits());
            response.setTotalPages((int) Math.ceil((double) searchHits.getTotalHits() / request.getSize()));
            response.setCurrentPage(request.getPage());
            response.setPageSize(request.getSize());
            response.setKeyword(request.getKeyword());
            response.setSearchTime(System.currentTimeMillis() - startTime);
            response.setHighlights(highlights);
            
            // Build facets (aggregations) trên tập kết quả cơ sở (approved + active + keyword)
            response.setFacets(buildFacets(request));

            return response;
        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage(), e);
            throw new ProductException("Lỗi khi tìm kiếm sản phẩm: " + e.getMessage());
        }
    }
    
    /**
     * Build Criteria từ SearchRequest
     */
    private Criteria buildSearchCriteria(SearchRequest request) {
        Criteria criteria = new Criteria();
        
        // Chỉ lấy sản phẩm đã APPROVED và ACTIVE (cho public search)
        criteria.and(Criteria.where("approvalStatus").is(ApprovalStatus.APPROVED.name()));
        criteria.and(Criteria.where("status").is(ProductStatus.ACTIVE.name()));
        
        // Keyword search (full-text)
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            String keyword = request.getKeyword().trim();
            Criteria keywordCriteria = new Criteria()
                .or(Criteria.where("productName").contains(keyword))
                .or(Criteria.where("description").contains(keyword))
                .or(Criteria.where("shortDescription").contains(keyword))
                .or(Criteria.where("productCode").is(keyword))
                .or(Criteria.where("skuCodes").contains(keyword));
            criteria.and(keywordCriteria);
        }
        
        // Filters
        if (request.getStoreId() != null) {
            criteria.and(Criteria.where("storeId").is(request.getStoreId()));
        }
        
        if (request.getCategoryId() != null) {
            criteria.and(Criteria.where("categoryId").is(request.getCategoryId()));
        }
        
        if (request.getStoreCategoryId() != null) {
            criteria.and(Criteria.where("storeCategoryId").is(request.getStoreCategoryId()));
        }
        
        if (request.getProductType() != null) {
            criteria.and(Criteria.where("productType").is(request.getProductType().name()));
        }
        
        if (request.getStatus() != null) {
            criteria.and(Criteria.where("status").is(request.getStatus().name()));
        }
        
        // Price range
        if (request.getMinPrice() != null) {
            criteria.and(Criteria.where("minPrice").greaterThanEqual(request.getMinPrice().doubleValue()));
        }
        
        if (request.getMaxPrice() != null) {
            criteria.and(Criteria.where("maxPrice").lessThanEqual(request.getMaxPrice().doubleValue()));
        }
        
        // Sizes
        if (request.getSizes() != null && !request.getSizes().isEmpty()) {
            Criteria sizeCriteria = new Criteria();
            for (String size : request.getSizes()) {
                sizeCriteria.or(Criteria.where("sizes").contains(size));
            }
            criteria.and(sizeCriteria);
        }
        
        // Colors
        if (request.getColors() != null && !request.getColors().isEmpty()) {
            Criteria colorCriteria = new Criteria();
            for (String color : request.getColors()) {
                colorCriteria.or(Criteria.where("colors").contains(color));
            }
            criteria.and(colorCriteria);
        }
        
        // Materials
        if (request.getMaterials() != null && !request.getMaterials().isEmpty()) {
            Criteria materialCriteria = new Criteria();
            for (String material : request.getMaterials()) {
                materialCriteria.or(Criteria.where("materials").contains(material));
            }
            criteria.and(materialCriteria);
        }
        
        // Boolean filters
        if (request.getOcopCertified() != null) {
            criteria.and(Criteria.where("ocopCertified").is(request.getOcopCertified()));
        }
        
        if (request.getIsVerified() != null) {
            criteria.and(Criteria.where("isVerified").is(request.getIsVerified()));
        }
        
        if (request.getHasOriginTracking() != null) {
            criteria.and(Criteria.where("hasOriginTracking").is(request.getHasOriginTracking()));
        }
        
        if (request.getHasVariants() != null) {
            criteria.and(Criteria.where("hasVariants").is(request.getHasVariants()));
        }
        
        if (request.getIsCombo() != null) {
            criteria.and(Criteria.where("isCombo").is(request.getIsCombo()));
        }
        
        // Không lấy sản phẩm đã bị xóa (chỉ lấy deletedAt = null)
        // Note: Elasticsearch Criteria không có isNull(), nên ta filter bằng cách khác
        // Hoặc có thể bỏ qua vì khi index, ta không index sản phẩm đã bị xóa
        
        return criteria;
    }
    
    /**
     * Build Sort từ sortBy và sortDir
     */
    private Sort buildSort(String sortBy, String sortDir) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        switch (sortBy != null ? sortBy.toLowerCase() : "relevance") {
            case "price_asc":
                return Sort.by(direction, "minPrice");
            case "price_desc":
                return Sort.by(Sort.Direction.DESC, "minPrice");
            case "rating":
            case "rating_desc":
                return Sort.by(Sort.Direction.DESC, "ratingAverage");
            case "sold":
            case "sold_count":
            case "sold_count_desc":
                return Sort.by(Sort.Direction.DESC, "soldCount");
            case "created_at":
            case "created_at_desc":
                return Sort.by(Sort.Direction.DESC, "createdAt");
            case "relevance":
            default:
                // Relevance sort sẽ được xử lý bởi Elasticsearch score
                return Sort.by(Sort.Direction.DESC, "_score");
        }
    }
    
    /**
     * Convert Product + ProductVariant list sang ProductDocument
     */
    private ProductDocument convertToProductDocument(Product product, List<ProductVariant> variants) {
        // Tính toán giá min, max từ SKUs
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        BigDecimal comparePrice = null;
        int totalStock = 0;
        
        List<String> skuCodes = new ArrayList<>();
        Set<String> sizes = new HashSet<>();
        Set<String> colors = new HashSet<>();
        Set<String> materials = new HashSet<>();
        
        for (ProductVariant variant : variants) {
            if (variant.getStatus().equals("active")) {
                if (variant.getPrice() != null) {
                    if (minPrice == null || variant.getPrice().compareTo(minPrice) < 0) {
                        minPrice = variant.getPrice();
                    }
                    if (maxPrice == null || variant.getPrice().compareTo(maxPrice) > 0) {
                        maxPrice = variant.getPrice();
                    }
                }
                
                if (variant.getComparePrice() != null && comparePrice == null) {
                    comparePrice = variant.getComparePrice();
                }
                
                totalStock += variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
                
                if (variant.getSku() != null) {
                    skuCodes.add(variant.getSku());
                }
                
                if (variant.getSize() != null) {
                    sizes.add(variant.getSize());
                }
                
                if (variant.getColor() != null) {
                    colors.add(variant.getColor());
                }
                
                if (variant.getMaterial() != null) {
                    materials.add(variant.getMaterial());
                }
            }
        }
        
        // Tính relevance score (có thể tùy chỉnh)
        double relevanceScore = calculateRelevanceScore(product);
        
        return ProductDocument.builder()
            .id(product.getId())
            .storeId(product.getStoreId())
            .categoryId(product.getCategoryId())
            .storeCategoryId(product.getStoreCategoryId())
            .productCode(product.getProductCode())
            .productName(product.getProductName())
            .slug(product.getSlug())
            .description(product.getDescription())
            .shortDescription(product.getShortDescription())
            .productType(product.getProductType())
            .durationHours(product.getDurationHours())
            .accessPeriodDays(product.getAccessPeriodDays())
            .isUnlimitedAccess(product.getIsUnlimitedAccess())
            .deliveryMethod(product.getDeliveryMethod())
            .hasVariants(product.getHasVariants())
            .isCombo(product.getIsCombo())
            .ocopCertified(product.getOcopCertified())
            .ocopLevel(product.getOcopLevel())
            .isVerified(product.getIsVerified())
            .hasOriginTracking(product.getHasOriginTracking())
            .approvalStatus(product.getApprovalStatus())
            .soldCount(product.getSoldCount())
            .viewCount(product.getViewCount())
            .ratingAverage(product.getRatingAverage())
            .ratingCount(product.getRatingCount())
            .status(product.getStatus())
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .comparePrice(comparePrice)
            .totalStockQuantity(totalStock)
            .skuCodes(new ArrayList<>(skuCodes))
            .sizes(new ArrayList<>(sizes))
            .colors(new ArrayList<>(colors))
            .materials(new ArrayList<>(materials))
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .deletedAt(product.getDeletedAt())
            .relevanceScore(relevanceScore)
            .build();
    }
    
    /**
     * Tính relevance score cho sản phẩm
     */
    private double calculateRelevanceScore(Product product) {
        double score = 0.0;
        
        // Rating (0-5) * 2
        if (product.getRatingAverage() != null) {
            score += product.getRatingAverage().doubleValue() * 2;
        }
        
        // Sold count / 100 (normalize)
        if (product.getSoldCount() != null) {
            score += Math.min(product.getSoldCount() / 100.0, 10.0);
        }
        
        // View count / 1000 (normalize)
        if (product.getViewCount() != null) {
            score += Math.min(product.getViewCount() / 1000.0, 5.0);
        }
        
        // Bonus cho verified, ocop certified
        if (Boolean.TRUE.equals(product.getIsVerified())) {
            score += 1.0;
        }
        
        if (Boolean.TRUE.equals(product.getOcopCertified())) {
            score += 1.0;
        }
        
        return score;
    }
    
    /**
     * Convert ProductDocument sang ProductResponse
     */
    private ProductResponse convertToProductResponse(ProductDocument doc) {
        ProductResponse response = new ProductResponse();
        response.setId(doc.getId());
        response.setStoreId(doc.getStoreId());
        response.setCategoryId(doc.getCategoryId());
        response.setStoreCategoryId(doc.getStoreCategoryId());
        response.setProductCode(doc.getProductCode());
        response.setProductName(doc.getProductName());
        response.setSlug(doc.getSlug());
        response.setDescription(doc.getDescription());
        response.setShortDescription(doc.getShortDescription());
        response.setProductType(doc.getProductType());
        response.setDurationHours(doc.getDurationHours());
        response.setAccessPeriodDays(doc.getAccessPeriodDays());
        response.setIsUnlimitedAccess(doc.getIsUnlimitedAccess());
        response.setDeliveryMethod(doc.getDeliveryMethod());
        response.setHasVariants(doc.getHasVariants());
        response.setIsCombo(doc.getIsCombo());
        response.setOcopCertified(doc.getOcopCertified());
        response.setOcopLevel(doc.getOcopLevel());
        response.setIsVerified(doc.getIsVerified());
        response.setHasOriginTracking(doc.getHasOriginTracking());
        response.setApprovalStatus(doc.getApprovalStatus());
        response.setSoldCount(doc.getSoldCount());
        response.setViewCount(doc.getViewCount());
        response.setRatingAverage(doc.getRatingAverage());
        response.setRatingCount(doc.getRatingCount());
        response.setStatus(doc.getStatus());
        response.setCreatedAt(doc.getCreatedAt());
        response.setUpdatedAt(doc.getUpdatedAt());
        response.setDeletedAt(doc.getDeletedAt());
        
        // Note: SKUs không được lấy từ Elasticsearch, cần lấy từ database nếu cần
        // response.setSkus(...);

        return response;
    }

    /**
     * Tính facets (aggregations) cho bộ lọc: số sản phẩm theo category/store/size/color/material
     * và khoảng giá. Tính trên tập cơ sở (APPROVED + ACTIVE + keyword).
     */
    private SearchResponse.SearchFacets buildFacets(SearchRequest request) {
        SearchResponse.SearchFacets facets = new SearchResponse.SearchFacets();
        try {
            boolean hasKeyword = request.getKeyword() != null && !request.getKeyword().trim().isEmpty();
            String keyword = hasKeyword ? request.getKeyword().trim() : null;

            co.elastic.clients.elasticsearch._types.query_dsl.Query esQuery =
                co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q.bool(b -> {
                    b.filter(f -> f.term(t -> t.field("approvalStatus").value(ApprovalStatus.APPROVED.name())));
                    b.filter(f -> f.term(t -> t.field("status").value(ProductStatus.ACTIVE.name())));
                    if (hasKeyword) {
                        b.must(m -> m.multiMatch(mm -> mm.query(keyword)
                            .fields("productName", "description", "shortDescription")));
                    }
                    return b;
                }));

            NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(esQuery)
                .withMaxResults(0)
                .withAggregation("categories", Aggregation.of(a -> a.terms(t -> t.field("categoryId").size(50))))
                .withAggregation("stores", Aggregation.of(a -> a.terms(t -> t.field("storeId").size(50))))
                .withAggregation("sizes", Aggregation.of(a -> a.terms(t -> t.field("sizes").size(50))))
                .withAggregation("colors", Aggregation.of(a -> a.terms(t -> t.field("colors").size(50))))
                .withAggregation("materials", Aggregation.of(a -> a.terms(t -> t.field("materials").size(50))))
                .withAggregation("priceStats", Aggregation.of(a -> a.stats(s -> s.field("minPrice"))))
                .build();

            SearchHits<ProductDocument> hits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);
            if (hits.getAggregations() instanceof ElasticsearchAggregations aggs) {
                Map<String, ElasticsearchAggregation> map = aggs.aggregationsAsMap();
                facets.setCategories(longTermFacet(map.get("categories")));
                facets.setStores(longTermFacet(map.get("stores")));
                facets.setSizes(stringTermFacet(map.get("sizes")));
                facets.setColors(stringTermFacet(map.get("colors")));
                facets.setMaterials(stringTermFacet(map.get("materials")));
                facets.setPriceRange(priceRangeFacet(map.get("priceStats")));
            }
        } catch (Exception e) {
            log.warn("Không thể tính facets: {}", e.getMessage());
        }
        return facets;
    }

    private Map<Long, Long> longTermFacet(ElasticsearchAggregation agg) {
        Map<Long, Long> result = new LinkedHashMap<>();
        if (agg == null) return result;
        Aggregate aggregate = agg.aggregation().getAggregate();
        if (aggregate.isLterms()) {
            aggregate.lterms().buckets().array()
                .forEach(b -> result.put(b.key(), b.docCount()));
        }
        return result;
    }

    private Map<String, Long> stringTermFacet(ElasticsearchAggregation agg) {
        Map<String, Long> result = new LinkedHashMap<>();
        if (agg == null) return result;
        Aggregate aggregate = agg.aggregation().getAggregate();
        if (aggregate.isSterms()) {
            aggregate.sterms().buckets().array()
                .forEach(b -> result.put(b.key().stringValue(), b.docCount()));
        }
        return result;
    }

    private SearchResponse.SearchFacets.PriceRange priceRangeFacet(ElasticsearchAggregation agg) {
        if (agg == null) return null;
        Aggregate aggregate = agg.aggregation().getAggregate();
        if (aggregate.isStats()) {
            var stats = aggregate.stats();
            return new SearchResponse.SearchFacets.PriceRange(stats.min(), stats.max());
        }
        return null;
    }
}

