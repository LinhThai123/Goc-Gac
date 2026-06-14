package com.ecommerce.gocgac.dto.menu;

import com.ecommerce.gocgac.entity.MenuItem;
import com.ecommerce.gocgac.entity.enums.MenuType;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class MenuItemResponse {

    private Long id;
    private Long parentId;
    private MenuType menuType;
    private String menuName;
    private String menuUrl;
    private String icon;
    private Integer displayOrder;
    private String permissionCode;
    private Boolean isActive;
    private List<MenuItemResponse> children;

    public static MenuItemResponse from(MenuItem m) {
        return MenuItemResponse.builder()
            .id(m.getId())
            .parentId(m.getParentId())
            .menuType(m.getMenuType())
            .menuName(m.getMenuName())
            .menuUrl(m.getMenuUrl())
            .icon(m.getIcon())
            .displayOrder(m.getDisplayOrder())
            .permissionCode(m.getPermissionCode())
            .isActive(m.getIsActive())
            .children(new ArrayList<>())
            .build();
    }
}
