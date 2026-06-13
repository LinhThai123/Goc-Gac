package com.ecommerce.gocgac.dto.promotion;

import com.ecommerce.gocgac.entity.enums.PromotionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Cập nhật khuyến mãi — chỉ trường khác null mới được áp dụng. */
@Data
public class UpdatePromotionRequest {

    private String promotionName;
    private String description;
    private BigDecimal discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private PromotionStatus status;
}