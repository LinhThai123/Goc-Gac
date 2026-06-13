package com.ecommerce.gocgac.dto.review;

import com.ecommerce.gocgac.entity.ReviewReply;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewReplyResponse {

    private Long id;
    private Long userId;
    private String replyContent;
    private LocalDateTime createdAt;

    public static ReviewReplyResponse from(ReviewReply r) {
        return ReviewReplyResponse.builder()
            .id(r.getId())
            .userId(r.getUserId())
            .replyContent(r.getReplyContent())
            .createdAt(r.getCreatedAt())
            .build();
    }
}
