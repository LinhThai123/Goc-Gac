package com.ecommerce.gocgac.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequest {
    
    @NotBlank(message = "Tên permission không được để trống")
    @Size(max = 100, message = "Tên permission không được vượt quá 100 ký tự")
    private String name;
    
    @NotBlank(message = "Code permission không được để trống")
    @Size(max = 100, message = "Code permission không được vượt quá 100 ký tự")
    @Pattern(regexp = "^[a-z0-9_]+:[a-z0-9_]+$", 
             message = "Code permission phải có định dạng resource:action (ví dụ: product:create)")
    private String code;
    
    private String description;
    
    @Size(max = 50, message = "Resource không được vượt quá 50 ký tự")
    private String resource; // product, order, user, etc.
    
    @Size(max = 50, message = "Action không được vượt quá 50 ký tự")
    private String action; // create, read, update, delete, approve, etc.
}

