package com.ecommerce.gocgac.dto.promotion;

import com.ecommerce.gocgac.entity.enums.PromotionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreatePromotionRequest {

    @NotBlank(message = "Tên khuyến mãi không được để trống")
    private String promotionName;

    private String description;

    @NotNull(message = "Loại khuyến mãi không được để trống")
    private PromotionType promotionType;

    private BigDecimal discountValue;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    /** Danh sách product áp dụng (tùy chọn, có thể thêm sau). */
    private List<Long> productIds;
}