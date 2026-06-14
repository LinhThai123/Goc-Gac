package com.ecommerce.gocgac.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpsertAutoReplyRequest {

    /** Từ khóa kích hoạt (chứa trong tin nhắn của khách). Null/blank = áp dụng mọi tin. */
    private String triggerKeyword;

    @NotBlank(message = "Nội dung trả lời không được để trống")
    private String replyMessage;

    private Boolean isActive;
}
