package com.ecommerce.gocgac.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    
    private Long id;
    
    private Long parentId;
    
    private String categoryName;
    
    private String categorySlug;
    
    private String description;
    
    private String imageUrl;
    
    private Integer displayOrder;
    
    private Integer level;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    /** Danh sách category con (chỉ có khi gọi API tree) */
    private List<CategoryResponse> children;
}

