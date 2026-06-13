package com.ecommerce.gocgac.dto.loyalty;

import com.ecommerce.gocgac.entity.LoyaltyTransaction;
import com.ecommerce.gocgac.entity.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoyaltyTransactionResponse {

    private Long id;
    private TransactionType transactionType;
    private Integer points;
    private String eventType;
    private String referenceType;
    private Long referenceId;
    private String description;
    private LocalDateTime createdAt;

    public static LoyaltyTransactionResponse from(LoyaltyTransaction t) {
        return LoyaltyTransactionResponse.builder()
            .id(t.getId())
            .transactionType(t.getTransactionType())
            .points(t.getPoints())
            .eventType(t.getEventType())
            .referenceType(t.getReferenceType())
            .referenceId(t.getReferenceId())
            .description(t.getDescription())
            .createdAt(t.getCreatedAt())
            .build();
    }
}
