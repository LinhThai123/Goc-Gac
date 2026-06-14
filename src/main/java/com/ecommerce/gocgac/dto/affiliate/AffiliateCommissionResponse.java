package com.ecommerce.gocgac.dto.affiliate;

import com.ecommerce.gocgac.entity.AffiliateCommission;
import com.ecommerce.gocgac.entity.enums.CommissionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AffiliateCommissionResponse {

    private Long id;
    private Long partnerId;
    private Long orderId;
    private BigDecimal orderAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private CommissionStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public static AffiliateCommissionResponse from(AffiliateCommission c) {
        return AffiliateCommissionResponse.builder()
            .id(c.getId())
            .partnerId(c.getPartnerId())
            .orderId(c.getOrderId())
            .orderAmount(c.getOrderAmount())
            .commissionRate(c.getCommissionRate())
            .commissionAmount(c.getCommissionAmount())
            .status(c.getStatus())
            .paidAt(c.getPaidAt())
            .createdAt(c.getCreatedAt())
            .build();
    }
}
