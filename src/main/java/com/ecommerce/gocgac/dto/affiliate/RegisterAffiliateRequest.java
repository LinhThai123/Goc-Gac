package com.ecommerce.gocgac.dto.affiliate;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterAffiliateRequest {

    @NotBlank(message = "Lý do/kênh quảng bá không được để trống")
    private String applicationReason;
}
