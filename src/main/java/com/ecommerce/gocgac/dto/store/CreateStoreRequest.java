package com.ecommerce.gocgac.dto.store;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateStoreRequest {

    @NotBlank(message = "Tên gian hàng không được để trống")
    @Size(max = 255, message = "Tên gian hàng tối đa 255 ký tự")
    private String storeName;

    @Size(max = 50, message = "Mã gian hàng tối đa 50 ký tự")
    private String storeCode; // optional - tự sinh nếu để trống

    private String description;

    @Size(max = 500)
    private String logoUrl;

    @Size(max = 500)
    private String bannerUrl;

    @Size(max = 500)
    private String videoUrl;

    @Size(max = 20)
    private String contactPhone;

    @Email(message = "Email liên hệ không hợp lệ")
    private String contactEmail;

    private String address;
}
