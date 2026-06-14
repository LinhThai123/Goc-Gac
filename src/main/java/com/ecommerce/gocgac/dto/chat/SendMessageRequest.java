package com.ecommerce.gocgac.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String messageText;

    private String attachmentUrl;
}
