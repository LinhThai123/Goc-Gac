package com.ecommerce.gocgac.dto.ad;

import com.ecommerce.gocgac.entity.AdCampaign;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdCampaignResponse {

    private Long id;
    private Long storeId;
    private String campaignName;
    private Long productId;
    private String adImageUrl;
    private String adPosition;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal budget;
    private BigDecimal costPerClick;
    private Integer totalClicks;
    private Integer totalConversions;
    private BigDecimal totalSpent;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private String status;

    public static AdCampaignResponse from(AdCampaign a) {
        return AdCampaignResponse.builder()
            .id(a.getId())
            .storeId(a.getStoreId())
            .campaignName(a.getCampaignName())
            .productId(a.getProductId())
            .adImageUrl(a.getAdImageUrl())
            .adPosition(a.getAdPosition())
            .startDate(a.getStartDate())
            .endDate(a.getEndDate())
            .budget(a.getBudget())
            .costPerClick(a.getCostPerClick())
            .totalClicks(a.getTotalClicks())
            .totalConversions(a.getTotalConversions())
            .totalSpent(a.getTotalSpent())
            .approvalStatus(a.getApprovalStatus())
            .rejectionReason(a.getRejectionReason())
            .status(a.getStatus())
            .build();
    }
}
