package com.ecommerce.gocgac.dto.recommendation;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecordViewRequest {

    @NotNull(message = "productId không được để trống")
    private Long productId;

    /** Thời gian xem (giây) — tùy chọn. */
    private Integer viewDuration;
}
