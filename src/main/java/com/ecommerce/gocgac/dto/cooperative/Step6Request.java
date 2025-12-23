package com.ecommerce.gocgac.dto.cooperative;

import com.ecommerce.gocgac.entity.enums.BusinessScale;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step6Request {
    
    @NotNull(message = "ID đơn đăng ký không được để trống")
    private Long registrationId;
    
    @NotEmpty(message = "Loại sản phẩm kinh doanh không được để trống")
    @Size(min = 1, max = 20, message = "Phải có ít nhất 1 loại sản phẩm và tối đa 20 loại")
    private List<@NotBlank(message = "Loại sản phẩm không được để trống") String> productTypes;
    
    @NotBlank(message = "Mô tả sản phẩm chính không được để trống")
    @Size(min = 50, max = 2000, message = "Mô tả sản phẩm chính phải từ 50 đến 2000 ký tự")
    private String mainProductDescription;
    
    @NotNull(message = "Quy mô sản xuất/kinh doanh không được để trống")
    private BusinessScale businessScale;
    
    @NotNull(message = "Thông tin chứng nhận/giấy phép đặc biệt không được để trống")
    private Boolean hasSpecialCertification;
    
    @Size(max = 1000, message = "Chi tiết chứng nhận không được vượt quá 1000 ký tự")
    private String specialCertificationDetails;
}

