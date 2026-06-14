package com.ecommerce.gocgac.dto.chat;

import com.ecommerce.gocgac.entity.AutoReply;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutoReplyResponse {

    private Long id;
    private Long storeId;
    private String triggerKeyword;
    private String replyMessage;
    private Boolean isActive;

    public static AutoReplyResponse from(AutoReply a) {
        return AutoReplyResponse.builder()
            .id(a.getId())
            .storeId(a.getStoreId())
            .triggerKeyword(a.getTriggerKeyword())
            .replyMessage(a.getReplyMessage())
            .isActive(a.getIsActive())
            .build();
    }
}
