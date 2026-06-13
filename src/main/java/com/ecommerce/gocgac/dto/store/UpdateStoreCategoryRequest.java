package com.ecommerce.gocgac.dto.store;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho việc cập nhật store category
 * Tất cả các field đều optional - chỉ cập nhật những field được gửi lên
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStoreCategoryRequest {
    
    @Size(max = 255, message = "Tên danh mục không được vượt quá 255 ký tự")
    private String categoryName;
    
    private Long parentId; // Optional, có thể thay đổi parent

    /** true = chuyển category về root (parentId = null, level = 1) */
    private Boolean makeRoot;
    
    @Min(value = 0, message = "Thứ tự hiển thị phải >= 0")
    private Integer displayOrder;
    
    private Boolean isActive;
}

