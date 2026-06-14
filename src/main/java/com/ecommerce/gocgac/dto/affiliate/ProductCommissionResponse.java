package com.ecommerce.gocgac.dto.affiliate;

import com.ecommerce.gocgac.entity.ProductCommission;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductCommissionResponse {

    private Long id;
    private Long productId;
    private Long storeId;
    private BigDecimal commissionRate;
    private Boolean isActive;

    public static ProductCommissionResponse from(ProductCommission p) {
        return ProductCommissionResponse.builder()
            .id(p.getId())
            .productId(p.getProductId())
            .storeId(p.getStoreId())
            .commissionRate(p.getCommissionRate())
            .isActive(p.getIsActive())
            .build();
    }
}
