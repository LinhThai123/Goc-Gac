  package com.ecommerce.gocgac.dto.cooperative;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step3Request {
    
    @NotNull(message = "ID đơn đăng ký không được để trống")
    private Long registrationId;
    
    @NotBlank(message = "Địa chỉ đầy đủ không được để trống")
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String fullAddress;
    
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    @Size(max = 100, message = "Tỉnh/Thành phố không được vượt quá 100 ký tự")
    private String province;
    
    @NotBlank(message = "Quận/Huyện không được để trống")
    @Size(max = 100, message = "Quận/Huyện không được vượt quá 100 ký tự")
    private String district;
    
    @NotBlank(message = "Phường/Xã không được để trống")
    @Size(max = 100, message = "Phường/Xã không được vượt quá 100 ký tự")
    private String ward;
    
    @Size(max = 20, message = "Mã bưu chính không được vượt quá 20 ký tự")
    private String postalCode;
    
    @NotNull(message = "Hiển thị bản đồ không được để trống")
    private Boolean showMap;
    
    @DecimalMin(value = "-90.0", message = "Vĩ độ không hợp lệ")
    @DecimalMax(value = "90.0", message = "Vĩ độ không hợp lệ")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Kinh độ không hợp lệ")
    @DecimalMax(value = "180.0", message = "Kinh độ không hợp lệ")
    private Double longitude;
}

