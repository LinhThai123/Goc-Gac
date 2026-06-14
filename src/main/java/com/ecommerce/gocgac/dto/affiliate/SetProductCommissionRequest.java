package com.ecommerce.gocgac.dto.affiliate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SetProductCommissionRequest {

    @NotNull(message = "productId không được để trống")
    private Long productId;

    @NotNull(message = "commissionRate không được để trống")
    @DecimalMin(value = "0.0", message = "Hoa hồng không âm")
    @DecimalMax(value = "100.0", message = "Hoa hồng tối đa 100%")
    private BigDecimal commissionRate; // phần trăm

    private Boolean isActive;
}
