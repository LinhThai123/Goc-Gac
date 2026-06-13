package com.ecommerce.gocgac.dto.memberrank;

import com.ecommerce.gocgac.entity.MemberRank;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MemberRankResponse {

    private Long id;
    private String rankName;
    private BigDecimal minSpending;
    private Integer periodMonths;
    private String description;
    private String benefits;
    private Integer rankOrder;
    private String status;

    public static MemberRankResponse from(MemberRank r) {
        return MemberRankResponse.builder()
            .id(r.getId())
            .rankName(r.getRankName())
            .minSpending(r.getMinSpending())
            .periodMonths(r.getPeriodMonths())
            .description(r.getDescription())
            .benefits(r.getBenefits())
            .rankOrder(r.getRankOrder())
            .status(r.getStatus())
            .build();
    }
}
