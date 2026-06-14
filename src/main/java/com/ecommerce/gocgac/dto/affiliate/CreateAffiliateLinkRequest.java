package com.ecommerce.gocgac.dto.affiliate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAffiliateLinkRequest {

    @NotNull(message = "productId không được để trống")
    private Long productId;
}
