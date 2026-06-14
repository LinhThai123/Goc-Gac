package com.ecommerce.gocgac.dto.system;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpsertSystemConfigRequest {

    @NotBlank(message = "configKey không được để trống")
    private String configKey;

    private String configValue;
    private String configType;
    private String description;
    private Boolean isPublic;
}
