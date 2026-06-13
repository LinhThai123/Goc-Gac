package com.ecommerce.gocgac.dto.loyalty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoyaltyInfoResponse {

    private Integer points;
    private Long memberRankId;
    private String memberRankName;
}
