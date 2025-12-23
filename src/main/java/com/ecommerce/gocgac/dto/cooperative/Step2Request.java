package com.ecommerce.gocgac.dto.cooperative;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step2Request {
    
    @NotNull(message = "ID đơn đăng ký không được để trống")
    private Long registrationId;
    
    @NotBlank(message = "Email liên hệ không được để trống")
    @Email(message = "Email không hợp lệ")
    private String contactEmail;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String contactPhone;
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phụ không hợp lệ")
    private String contactPhoneAlt;
    
    @URL(message = "Website không hợp lệ")
    private String website;
    
    private String facebookPage; // Có thể là URL hoặc username
}

