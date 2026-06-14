package com.ecommerce.gocgac.dto.menu;

import com.ecommerce.gocgac.entity.enums.MenuType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMenuItemRequest {

    private Long parentId;

    @NotNull(message = "Loại menu không được để trống")
    private MenuType menuType;

    @NotBlank(message = "Tên menu không được để trống")
    private String menuName;

    private String menuUrl;
    private String icon;
    private Integer displayOrder;

    /** Mã quyền/role để hiển thị (null = hiển thị cho mọi người dùng loại menu này). */
    private String permissionCode;
}
