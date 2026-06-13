package com.ecommerce.gocgac.dto.order;

import com.ecommerce.gocgac.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    private String recipientName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    @Size(max = 20)
    private String recipientPhone;

    private String recipientEmail;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;

    @Size(max = 100)
    private String shippingProvince;

    @Size(max = 100)
    private String shippingDistrict;

    @Size(max = 100)
    private String shippingWard;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    /** Mã voucher (tùy chọn) — áp dụng cho đơn của gian hàng tương ứng. */
    private String voucherCode;

    /** Số điểm thưởng muốn dùng để giảm giá (tùy chọn). */
    private Integer loyaltyPointsToUse;

    private String notes;
}
