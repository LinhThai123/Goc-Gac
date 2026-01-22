package com.ecommerce.gocgac.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO cho search response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    
    /**
     * Danh sách sản phẩm tìm được
     */
    private List<ProductResponse> products;
    
    /**
     * Tổng số sản phẩm
     */
    private long totalElements;
    
    /**
     * Tổng số trang
     */
    private int totalPages;
    
    /**
     * Trang hiện tại (0-based)
     */
    private int currentPage;
    
    /**
     * Kích thước trang
     */
    private int pageSize;
    
    /**
     * Keyword đã search
     */
    private String keyword;
    
    /**
     * Thời gian search (ms)
     */
    private long searchTime;
    
    /**
     * Highlight results (nếu có)
     * Key: productId, Value: Map<field, highlightedText>
     */
    private Map<Long, Map<String, String>> highlights;
    
    /**
     * Aggregations (facets) cho filter
     */
    private SearchFacets facets;
    
    /**
     * Inner class cho facets
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchFacets {
        /**
         * Danh sách categories với số lượng sản phẩm
         */
        private Map<Long, Long> categories;
        
        /**
         * Danh sách stores với số lượng sản phẩm
         */
        private Map<Long, Long> stores;
        
        /**
         * Danh sách sizes với số lượng sản phẩm
         */
        private Map<String, Long> sizes;
        
        /**
         * Danh sách colors với số lượng sản phẩm
         */
        private Map<String, Long> colors;
        
        /**
         * Danh sách materials với số lượng sản phẩm
         */
        private Map<String, Long> materials;
        
        /**
         * Price range (min, max)
         */
        private PriceRange priceRange;
        
        /**
         * Inner class cho price range
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PriceRange {
            private Double min;
            private Double max;
        }
    }
}

