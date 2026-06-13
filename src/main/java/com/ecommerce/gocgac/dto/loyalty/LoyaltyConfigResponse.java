package com.ecommerce.gocgac.dto.loyalty;

import com.ecommerce.gocgac.entity.LoyaltyConfig;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoyaltyConfigResponse {

    private Long id;
    private String eventType;
    private String eventName;
    private Integer pointsAwarded;
    private Boolean isActive;
    private String description;

    public static LoyaltyConfigResponse from(LoyaltyConfig c) {
        return LoyaltyConfigResponse.builder()
            .id(c.getId())
            .eventType(c.getEventType())
            .eventName(c.getEventName())
            .pointsAwarded(c.getPointsAwarded())
            .isActive(c.getIsActive())
            .description(c.getDescription())
            .build();
    }
}
