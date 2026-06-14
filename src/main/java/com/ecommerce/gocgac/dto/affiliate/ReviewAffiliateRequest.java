package com.ecommerce.gocgac.dto.affiliate;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewAffiliateRequest {

    private boolean approved;

    /** Mức hoa hồng mặc định (%) gán cho đối tác khi duyệt (fallback nếu sản phẩm chưa đặt rate). */
    private BigDecimal commissionRate;

    private String rejectionReason;
}
