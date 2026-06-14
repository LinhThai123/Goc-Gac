package com.ecommerce.gocgac.dto.chat;

import com.ecommerce.gocgac.entity.Conversation;
import com.ecommerce.gocgac.entity.enums.ConversationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationResponse {

    private Long id;
    private Long buyerId;
    private Long sellerId;
    private Long storeId;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    /** Số tin chưa đọc của phía đang xem (buyer hoặc seller). */
    private Integer unreadCount;
    private ConversationStatus status;
    private LocalDateTime createdAt;

    public static ConversationResponse from(Conversation c, int unreadForViewer) {
        return ConversationResponse.builder()
            .id(c.getId())
            .buyerId(c.getBuyerId())
            .sellerId(c.getSellerId())
            .storeId(c.getStoreId())
            .lastMessage(c.getLastMessage())
            .lastMessageAt(c.getLastMessageAt())
            .unreadCount(unreadForViewer)
            .status(c.getStatus())
            .createdAt(c.getCreatedAt())
            .build();
    }
}
