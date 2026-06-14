package com.ecommerce.gocgac.dto.news;

import com.ecommerce.gocgac.entity.NewsCategory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsCategoryResponse {

    private Long id;
    private String categoryName;
    private String categorySlug;
    private String description;
    private Integer displayOrder;
    private Boolean isActive;

    public static NewsCategoryResponse from(NewsCategory c) {
        return NewsCategoryResponse.builder()
            .id(c.getId())
            .categoryName(c.getCategoryName())
            .categorySlug(c.getCategorySlug())
            .description(c.getDescription())
            .displayOrder(c.getDisplayOrder())
            .isActive(c.getIsActive())
            .build();
    }
}
