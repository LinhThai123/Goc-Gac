package com.ecommerce.gocgac.dto.cooperative;

import com.ecommerce.gocgac.entity.enums.CooperativeScale;
import com.ecommerce.gocgac.entity.enums.CooperativeType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step1Request {
    
    @NotBlank(message = "Tên HTX không được để trống")
    @Size(min = 5, max = 200, message = "Tên HTX phải từ 5 đến 200 ký tự")
    private String cooperativeName;
    
    @NotBlank(message = "Slug không được để trống")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug chỉ được chứa chữ thường, số và dấu gạch ngang")
    @Size(min = 3, max = 100, message = "Slug phải từ 3 đến 100 ký tự")
    private String slug;
    
    @NotBlank(message = "Mã số HTX không được để trống")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Mã số HTX chỉ được chứa chữ hoa, số và dấu gạch ngang")
    @Size(min = 5, max = 50, message = "Mã số HTX phải từ 5 đến 50 ký tự")
    private String cooperativeCode;
    
    @NotNull(message = "Ngày thành lập không được để trống")
    @PastOrPresent(message = "Ngày thành lập không được là tương lai")
    private LocalDate establishmentDate;
    
    @NotNull(message = "Loại hình HTX không được để trống")
    private CooperativeType cooperativeType;
    
    @NotNull(message = "Quy mô HTX không được để trống")
    private CooperativeScale scale;
    
    @NotBlank(message = "Mô tả ngắn không được để trống")
    @Size(min = 50, max = 500, message = "Mô tả ngắn phải từ 50 đến 500 ký tự")
    private String shortDescription;
}

