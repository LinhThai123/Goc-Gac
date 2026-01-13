package com.ecommerce.gocgac.dto.category;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    
    @Size(max = 255, message = "Category slug không được vượt quá 255 ký tự")
    @Pattern(regexp = "^[a-z0-9-]*$", message = "Category slug chỉ chứa chữ thường, số và dấu gạch ngang")
    private String categorySlug;
    
    @Size(max = 255, message = "Category name không được vượt quá 255 ký tự")
    private String categoryName;
    
    private Long parentId;
    
    private String description;
    
    @Size(max = 500, message = "Image URL không được vượt quá 500 ký tự")
    private String imageUrl;
    
    @Size(min = 0, message = "Display order phải >= 0")
    private Integer displayOrder;
    
    private Boolean isActive;
}

