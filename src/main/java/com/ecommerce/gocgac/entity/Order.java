package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.OrderStatus;
import com.ecommerce.gocgac.entity.enums.PaymentMethod;
import com.ecommerce.gocgac.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_user", columnList = "user_id"),
    @Index(name = "idx_order_code", columnList = "order_code"),
    @Index(name = "idx_orders_status", columnList = "order_status"),
    @Index(name = "idx_payment", columnList = "payment_status"),
    @Index(name = "idx_orders_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String orderCode;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "recipient_name", nullable = false)
    private String recipientName;
    
    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;
    
    @Column(name = "recipient_email")
    private String recipientEmail;
    
    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;
    
    @Column(name = "shipping_province", length = 100)
    private String shippingProvince;
    
    @Column(name = "shipping_district", length = 100)
    private String shippingDistrict;
    
    @Column(name = "shipping_ward", length = 100)
    private String shippingWard;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "shipping_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;
    
    @Column(name = "voucher_discount", nullable = false, precision = 15, scale = 2)
    private BigDecimal voucherDiscount = BigDecimal.ZERO;
    
    @Column(name = "loyalty_points_used", nullable = false)
    private Integer loyaltyPointsUsed = 0;
    
    @Column(name = "loyalty_discount", nullable = false, precision = 15, scale = 2)
    private BigDecimal loyaltyDiscount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDING;
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "affiliate_code", length = 50)
    private String affiliateCode;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

