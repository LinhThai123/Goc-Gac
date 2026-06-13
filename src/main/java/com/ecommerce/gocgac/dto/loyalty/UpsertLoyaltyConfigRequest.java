package com.ecommerce.gocgac.dto.loyalty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpsertLoyaltyConfigRequest {

    @NotBlank(message = "eventType không được để trống")
    private String eventType;

    @NotBlank(message = "Tên sự kiện không được để trống")
    private String eventName;

    @NotNull(message = "Số điểm/giá trị không được để trống")
    private Integer pointsAwarded;

    private Boolean isActive;

    private String description;
}
