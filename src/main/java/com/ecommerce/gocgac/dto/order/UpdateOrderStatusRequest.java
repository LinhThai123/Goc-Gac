package com.ecommerce.gocgac.dto.order;

import com.ecommerce.gocgac.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "Trạng thái mới không được để trống")
    private OrderStatus status;

    private String note;
}
