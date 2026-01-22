package com.ecommerce.gocgac.dto.store;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoreCategoryRequest {
    
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 255, message = "Tên danh mục không được vượt quá 255 ký tự")
    private String categoryName;
    
    private Long parentId; // Optional, null nếu là category root
    
    @Min(value = 0, message = "Thứ tự hiển thị phải >= 0")
    private Integer displayOrder = 0;
    
    private Boolean isActive = true;
}

