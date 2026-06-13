package com.ecommerce.gocgac.dto.review;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplyRequest {

    @NotBlank(message = "Nội dung phản hồi không được để trống")
    private String content;
}
