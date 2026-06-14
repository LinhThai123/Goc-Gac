package com.ecommerce.gocgac.dto.chat;

import com.ecommerce.gocgac.entity.Message;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String messageText;
    private String attachmentUrl;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(Message m) {
        return ChatMessageResponse.builder()
            .id(m.getId())
            .conversationId(m.getConversationId())
            .senderId(m.getSenderId())
            .messageText(m.getMessageText())
            .attachmentUrl(m.getAttachmentUrl())
            .isRead(m.getIsRead())
            .readAt(m.getReadAt())
            .createdAt(m.getCreatedAt())
            .build();
    }
}
