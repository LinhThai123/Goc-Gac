package com.ecommerce.gocgac.dto.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {

    private Long id;            // id của cart_item
    private Long storeId;       // gian hàng (dùng để tách đơn ở bước checkout)
    private Long productId;
    private String productName;
    private String slug;
    private Long variantId;
    private String sku;
    private String variantName;
    private String imageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
    private Integer availableQuantity;
    private Boolean inStock;     // còn đủ tồn cho số lượng đang chọn không
}
