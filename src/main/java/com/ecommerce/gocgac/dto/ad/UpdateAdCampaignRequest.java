package com.ecommerce.gocgac.dto.ad;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Cập nhật chiến dịch — chỉ áp dụng khi đang DRAFT/REJECTED. */
@Data
public class UpdateAdCampaignRequest {

    private String campaignName;
    private String adImageUrl;
    private String adPosition;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal budget;
    private BigDecimal costPerClick;
}
