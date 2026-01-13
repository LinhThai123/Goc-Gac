package com.ecommerce.gocgac.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @Size(max = 255, message = "Họ tên không được vượt quá 255 ký tự")
    private String fullName;
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phone;
    
    @Size(max = 1000, message = "Địa chỉ không được vượt quá 1000 ký tự")
    private String address;
    
    private LocalDate dateOfBirth;
    
    // Avatar URL sẽ được cập nhật qua endpoint upload riêng
    // Không cần trong request này
}

