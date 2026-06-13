package com.ecommerce.gocgac.dto.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {

    private Long cartId;
    private List<CartItemResponse> items;
    private int totalItems;          // số dòng sản phẩm khác nhau
    private int totalQuantity;       // tổng số lượng
    private BigDecimal subtotal;     // tổng tạm tính (chưa gồm phí ship / giảm giá)
}
