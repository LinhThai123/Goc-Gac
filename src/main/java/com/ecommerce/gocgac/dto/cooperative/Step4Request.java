package com.ecommerce.gocgac.dto.cooperative;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step4Request {
    
    @NotNull(message = "ID đơn đăng ký không được để trống")
    private Long registrationId;
    
    @NotBlank(message = "Họ và tên người đại diện không được để trống")
    @Size(max = 200, message = "Họ và tên không được vượt quá 200 ký tự")
    private String representativeName;
    
    @NotBlank(message = "Chức vụ không được để trống")
    @Size(max = 100, message = "Chức vụ không được vượt quá 100 ký tự")
    private String representativePosition;
    
    @NotBlank(message = "Số CCCD/CMND không được để trống")
    @Size(max = 50, message = "Số CCCD/CMND không được vượt quá 50 ký tự")
    private String representativeIdNumber;
    
    @NotNull(message = "Ngày cấp CCCD không được để trống")
    @PastOrPresent(message = "Ngày cấp không được là tương lai")
    private LocalDate representativeIdIssueDate;
    
    @NotBlank(message = "Nơi cấp CCCD không được để trống")
    @Size(max = 200, message = "Nơi cấp không được vượt quá 200 ký tự")
    private String representativeIdIssuePlace;
    
    @NotBlank(message = "Email người đại diện không được để trống")
    @Email(message = "Email không hợp lệ")
    private String representativeEmail;
    
    @NotBlank(message = "Số điện thoại người đại diện không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String representativePhone;
    
    @URL(message = "URL ảnh mặt trước CCCD không hợp lệ")
    private String representativeIdFrontImage;
    
    @URL(message = "URL ảnh mặt sau CCCD không hợp lệ")
    private String representativeIdBackImage;
}

