package com.ecommerce.gocgac.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateReviewRequest {

    @NotNull(message = "productId không được để trống")
    private Long productId;

    /** Đơn hàng liên quan (tùy chọn). */
    private Long orderId;

    @NotNull(message = "Số sao không được để trống")
    @Min(value = 1, message = "Số sao tối thiểu là 1")
    @Max(value = 5, message = "Số sao tối đa là 5")
    private Integer rating;

    private String reviewTitle;

    private String reviewContent;

    /** Danh sách URL ảnh đính kèm. */
    private List<String> images;
}
