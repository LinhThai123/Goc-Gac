package com.ecommerce.gocgac.dto.voucher;

import com.ecommerce.gocgac.entity.Voucher;
import com.ecommerce.gocgac.entity.enums.DiscountType;
import com.ecommerce.gocgac.entity.enums.VoucherStatus;
import com.ecommerce.gocgac.entity.enums.VoucherType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VoucherResponse {

    private Long id;
    private Long storeId;
    private String voucherCode;
    private String voucherName;
    private VoucherType voucherType;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer userUsageLimit;
    private Long memberRankId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private VoucherStatus status;

    public static VoucherResponse from(Voucher v) {
        return VoucherResponse.builder()
            .id(v.getId())
            .storeId(v.getStoreId())
            .voucherCode(v.getVoucherCode())
            .voucherName(v.getVoucherName())
            .voucherType(v.getVoucherType())
            .discountType(v.getDiscountType())
            .discountValue(v.getDiscountValue())
            .maxDiscountAmount(v.getMaxDiscountAmount())
            .minOrderValue(v.getMinOrderValue())
            .usageLimit(v.getUsageLimit())
            .usageCount(v.getUsageCount())
            .userUsageLimit(v.getUserUsageLimit())
            .memberRankId(v.getMemberRankId())
            .startDate(v.getStartDate())
            .endDate(v.getEndDate())
            .status(v.getStatus())
            .build();
    }
}