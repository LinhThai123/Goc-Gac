package com.ecommerce.gocgac.dto.menu;

import lombok.Data;

/** Cập nhật menu — chỉ trường khác null mới được áp dụng. */
@Data
public class UpdateMenuItemRequest {

    private Long parentId;
    private String menuName;
    private String menuUrl;
    private String icon;
    private Integer displayOrder;
    private String permissionCode;
    private Boolean isActive;
}
