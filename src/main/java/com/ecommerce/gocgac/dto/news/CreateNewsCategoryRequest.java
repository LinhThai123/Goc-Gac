package com.ecommerce.gocgac.dto.news;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateNewsCategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    private String categoryName;

    /** Slug tùy chọn — tự sinh từ tên nếu để trống. */
    private String categorySlug;

    private String description;

    private Integer displayOrder;
}
