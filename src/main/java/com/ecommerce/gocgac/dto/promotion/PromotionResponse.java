package com.ecommerce.gocgac.dto.promotion;

import com.ecommerce.gocgac.entity.Promotion;
import com.ecommerce.gocgac.entity.enums.PromotionStatus;
import com.ecommerce.gocgac.entity.enums.PromotionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PromotionResponse {

    private Long id;
    private Long storeId;
    private String promotionName;
    private String description;
    private PromotionType promotionType;
    private BigDecimal discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private PromotionStatus status;
    private List<Long> productIds;

    public static PromotionResponse from(Promotion p, List<Long> productIds) {
        return PromotionResponse.builder()
            .id(p.getId())
            .storeId(p.getStoreId())
            .promotionName(p.getPromotionName())
            .description(p.getDescription())
            .promotionType(p.getPromotionType())
            .discountValue(p.getDiscountValue())
            .startDate(p.getStartDate())
            .endDate(p.getEndDate())
            .status(p.getStatus())
            .productIds(productIds)
            .build();
    }
}