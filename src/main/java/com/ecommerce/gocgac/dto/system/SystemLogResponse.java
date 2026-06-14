package com.ecommerce.gocgac.dto.system;

import com.ecommerce.gocgac.entity.SystemLog;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SystemLogResponse {

    private Long id;
    private Long userId;
    private String action;
    private String module;
    private String referenceType;
    private Long referenceId;
    private String ipAddress;
    private String userAgent;
    private String details;
    private LocalDateTime createdAt;

    public static SystemLogResponse from(SystemLog l) {
        return SystemLogResponse.builder()
            .id(l.getId())
            .userId(l.getUserId())
            .action(l.getAction())
            .module(l.getModule())
            .referenceType(l.getReferenceType())
            .referenceId(l.getReferenceId())
            .ipAddress(l.getIpAddress())
            .userAgent(l.getUserAgent())
            .details(l.getDetails())
            .createdAt(l.getCreatedAt())
            .build();
    }
}
