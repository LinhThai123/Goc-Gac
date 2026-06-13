package com.ecommerce.gocgac.dto.memberrank;

import lombok.Data;

import java.math.BigDecimal;

/** Cập nhật hạng — chỉ trường khác null mới được áp dụng. */
@Data
public class UpdateMemberRankRequest {

    private String rankName;
    private BigDecimal minSpending;
    private Integer periodMonths;
    private String description;
    private String benefits;
    private Integer rankOrder;
    private String status; // active | inactive
}
