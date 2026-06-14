package com.ecommerce.gocgac.dto.affiliate;

import com.ecommerce.gocgac.entity.AffiliatePartner;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AffiliatePartnerResponse {

    private Long id;
    private Long userId;
    private String affiliateCode;
    private BigDecimal commissionRate;
    private Integer totalClicks;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalCommission;
    private String status;

    public static AffiliatePartnerResponse from(AffiliatePartner p) {
        return AffiliatePartnerResponse.builder()
            .id(p.getId())
            .userId(p.getUserId())
            .affiliateCode(p.getAffiliateCode())
            .commissionRate(p.getCommissionRate())
            .totalClicks(p.getTotalClicks())
            .totalOrders(p.getTotalOrders())
            .totalRevenue(p.getTotalRevenue())
            .totalCommission(p.getTotalCommission())
            .status(p.getStatus())
            .build();
    }
}
