package com.ecommerce.gocgac.dto.promotion;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AddPromotionProductsRequest {

    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    private List<Long> productIds;
}