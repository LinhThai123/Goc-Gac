package com.ecommerce.gocgac.dto.memberrank;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMemberRankRequest {

    @NotBlank(message = "Tên hạng không được để trống")
    private String rankName;

    @NotNull(message = "Ngưỡng chi tiêu không được để trống")
    @PositiveOrZero(message = "Ngưỡng chi tiêu không được âm")
    private BigDecimal minSpending;

    /** Kỳ tính chi tiêu (tháng), mặc định 12. */
    private Integer periodMonths;

    private String description;
    private String benefits;

    @NotNull(message = "Thứ tự hạng không được để trống")
    private Integer rankOrder;
}
