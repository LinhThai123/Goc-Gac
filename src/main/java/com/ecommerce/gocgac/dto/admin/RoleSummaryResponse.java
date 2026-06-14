package com.ecommerce.gocgac.dto.admin;

import com.ecommerce.gocgac.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleSummaryResponse {

    private Long id;
    private String name;
    private String code;
    private String description;

    public static RoleSummaryResponse from(Role r) {
        return RoleSummaryResponse.builder()
            .id(r.getId())
            .name(r.getName())
            .code(r.getCode())
            .description(r.getDescription())
            .build();
    }
}
