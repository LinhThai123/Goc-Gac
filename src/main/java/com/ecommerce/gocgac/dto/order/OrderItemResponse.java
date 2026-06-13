package com.ecommerce.gocgac.dto.order;

import com.ecommerce.gocgac.entity.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {

    private Long id;
    private Long storeId;
    private Long productId;
    private Long variantId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal subtotal;

    public static OrderItemResponse from(OrderItem i) {
        return OrderItemResponse.builder()
            .id(i.getId())
            .storeId(i.getStoreId())
            .productId(i.getProductId())
            .variantId(i.getVariantId())
            .productName(i.getProductName())
            .productImage(i.getProductImage())
            .quantity(i.getQuantity())
            .unitPrice(i.getUnitPrice())
            .discountAmount(i.getDiscountAmount())
            .subtotal(i.getSubtotal())
            .build();
    }
}
