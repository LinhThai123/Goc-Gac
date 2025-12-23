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
public class Step5Request {
    
    @NotNull(message = "ID đơn đăng ký không được để trống")
    private Long registrationId;
    
    @Size(max = 50, message = "Mã số thuế không được vượt quá 50 ký tự")
    private String taxCode;
    
    @NotBlank(message = "Số Giấy CNĐK HTX không được để trống")
    @Size(max = 100, message = "Số Giấy CNĐK HTX không được vượt quá 100 ký tự")
    private String registrationCertificateNumber;
    
    @NotNull(message = "Ngày cấp Giấy CNĐK HTX không được để trống")
    @PastOrPresent(message = "Ngày cấp không được là tương lai")
    private LocalDate registrationCertificateIssueDate;
    
    @NotBlank(message = "Nơi cấp Giấy CNĐK HTX không được để trống")
    @Size(max = 200, message = "Nơi cấp không được vượt quá 200 ký tự")
    private String registrationCertificateIssuePlace;
    
    @URL(message = "URL ảnh Giấy CNĐK HTX không hợp lệ")
    private String registrationCertificateImage;
    
    @URL(message = "URL ảnh Giấy chứng nhận mã số thuế không hợp lệ")
    private String taxCodeCertificateImage;
}

