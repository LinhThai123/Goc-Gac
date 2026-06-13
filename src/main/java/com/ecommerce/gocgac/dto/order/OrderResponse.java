package com.ecommerce.gocgac.dto.order;

import com.ecommerce.gocgac.entity.Order;
import com.ecommerce.gocgac.entity.enums.OrderStatus;
import com.ecommerce.gocgac.entity.enums.PaymentMethod;
import com.ecommerce.gocgac.entity.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private Long id;
    private String orderCode;
    private Long storeId;          // đơn được tách theo gian hàng → 1 store / đơn
    private Long userId;

    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private String shippingAddress;
    private String shippingProvince;
    private String shippingDistrict;
    private String shippingWard;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;

    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal voucherDiscount;
    private BigDecimal loyaltyDiscount;
    private BigDecimal totalAmount;

    private String notes;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    private List<OrderItemResponse> items;

    public static OrderResponse from(Order o, List<OrderItemResponse> items, Long storeId) {
        return OrderResponse.builder()
            .id(o.getId())
            .orderCode(o.getOrderCode())
            .storeId(storeId)
            .userId(o.getUserId())
            .recipientName(o.getRecipientName())
            .recipientPhone(o.getRecipientPhone())
            .recipientEmail(o.getRecipientEmail())
            .shippingAddress(o.getShippingAddress())
            .shippingProvince(o.getShippingProvince())
            .shippingDistrict(o.getShippingDistrict())
            .shippingWard(o.getShippingWard())
            .paymentMethod(o.getPaymentMethod())
            .paymentStatus(o.getPaymentStatus())
            .orderStatus(o.getOrderStatus())
            .subtotal(o.getSubtotal())
            .shippingFee(o.getShippingFee())
            .voucherDiscount(o.getVoucherDiscount())
            .loyaltyDiscount(o.getLoyaltyDiscount())
            .totalAmount(o.getTotalAmount())
            .notes(o.getNotes())
            .cancellationReason(o.getCancellationReason())
            .createdAt(o.getCreatedAt())
            .confirmedAt(o.getConfirmedAt())
            .shippedAt(o.getShippedAt())
            .deliveredAt(o.getDeliveredAt())
            .cancelledAt(o.getCancelledAt())
            .items(items)
            .build();
    }
}
