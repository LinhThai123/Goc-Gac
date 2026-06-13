package com.ecommerce.gocgac.dto.notification;

import com.ecommerce.gocgac.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String notificationType;
    private String title;
    private String message;
    private String referenceType;
    private Long referenceId;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
            n.getId(),
            n.getNotificationType(),
            n.getTitle(),
            n.getMessage(),
            n.getReferenceType(),
            n.getReferenceId(),
            n.getIsRead(),
            n.getReadAt(),
            n.getCreatedAt()
        );
    }
}
