package com.ecommerce.gocgac.dto.ad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateAdCampaignRequest {

    @NotBlank(message = "Tên chiến dịch không được để trống")
    private String campaignName;

    @NotNull(message = "productId không được để trống")
    private Long productId;

    private String adImageUrl;

    @NotBlank(message = "Vị trí quảng cáo không được để trống")
    private String adPosition;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    @NotNull(message = "Ngân sách không được để trống")
    @PositiveOrZero(message = "Ngân sách không âm")
    private BigDecimal budget;

    @NotNull(message = "Giá mỗi click không được để trống")
    @PositiveOrZero(message = "Giá mỗi click không âm")
    private BigDecimal costPerClick;
}
