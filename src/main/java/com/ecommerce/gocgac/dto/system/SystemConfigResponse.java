package com.ecommerce.gocgac.dto.system;

import com.ecommerce.gocgac.entity.SystemConfig;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemConfigResponse {

    private Long id;
    private String configKey;
    private String configValue;
    private String configType;
    private String description;
    private Boolean isPublic;

    public static SystemConfigResponse from(SystemConfig c) {
        return SystemConfigResponse.builder()
            .id(c.getId())
            .configKey(c.getConfigKey())
            .configValue(c.getConfigValue())
            .configType(c.getConfigType())
            .description(c.getDescription())
            .isPublic(c.getIsPublic())
            .build();
    }
}
