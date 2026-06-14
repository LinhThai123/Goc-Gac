package com.ecommerce.gocgac.dto.recommendation;

import com.ecommerce.gocgac.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RecommendedProductResponse {

    private Long productId;
    private String productName;
    private String slug;
    private Long storeId;
    private Long categoryId;
    private BigDecimal ratingAverage;
    private Integer ratingCount;
    private Integer soldCount;

    public static RecommendedProductResponse from(Product p) {
        return RecommendedProductResponse.builder()
            .productId(p.getId())
            .productName(p.getProductName())
            .slug(p.getSlug())
            .storeId(p.getStoreId())
            .categoryId(p.getCategoryId())
            .ratingAverage(p.getRatingAverage())
            .ratingCount(p.getRatingCount())
            .soldCount(p.getSoldCount())
            .build();
    }
}
