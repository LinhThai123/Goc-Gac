package com.ecommerce.gocgac.dto.voucher;

import com.ecommerce.gocgac.entity.enums.DiscountType;
import com.ecommerce.gocgac.entity.enums.VoucherType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateVoucherRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 50)
    private String voucherCode;

    @NotBlank(message = "Tên voucher không được để trống")
    private String voucherName;

    @NotNull(message = "Loại voucher không được để trống")
    private VoucherType voucherType;

    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @Positive(message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    /** Mức giảm tối đa (cho voucher %). */
    private BigDecimal maxDiscountAmount;

    /** Giá trị đơn tối thiểu để áp dụng. */
    private BigDecimal minOrderValue;

    /** Tổng lượt dùng tối đa (null = không giới hạn). */
    @Min(0)
    private Integer usageLimit;

    /** Số lần mỗi người dùng được dùng (mặc định 1). */
    @Min(1)
    private Integer userUsageLimit;

    /** Giới hạn theo hạng thành viên (null = mọi hạng). */
    private Long memberRankId;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;
}