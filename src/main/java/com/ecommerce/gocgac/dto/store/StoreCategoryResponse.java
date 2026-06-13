package com.ecommerce.gocgac.dto.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreCategoryResponse {
    
    private Long id;
    
    private Long storeId;
    
    private Long parentId;
    
    private String categoryName;
    
    private Integer displayOrder;
    
    private Integer level;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    /** Danh sách danh mục con (có khi gọi API tree) */
    private List<StoreCategoryResponse> children;
}

