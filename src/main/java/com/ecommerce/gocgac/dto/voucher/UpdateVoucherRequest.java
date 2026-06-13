package com.ecommerce.gocgac.dto.voucher;

import com.ecommerce.gocgac.entity.enums.VoucherStatus;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cập nhật voucher — chỉ trường khác null mới được áp dụng.
 * Không cho đổi mã voucher (voucher_code) để tránh phá vỡ liên kết đã phát.
 */
@Data
public class UpdateVoucherRequest {

    private String voucherName;

    @Positive(message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private Integer usageLimit;
    private Integer userUsageLimit;
    private Long memberRankId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private VoucherStatus status;
}