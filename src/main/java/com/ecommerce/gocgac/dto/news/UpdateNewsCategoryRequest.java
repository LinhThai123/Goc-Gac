package com.ecommerce.gocgac.dto.news;

import lombok.Data;

/** Cập nhật danh mục tin — chỉ trường khác null mới được áp dụng. */
@Data
public class UpdateNewsCategoryRequest {

    private String categoryName;
    private String description;
    private Integer displayOrder;
    private Boolean isActive;
}
