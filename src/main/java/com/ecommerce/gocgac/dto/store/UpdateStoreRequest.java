package com.ecommerce.gocgac.dto.store;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Mọi trường đều tùy chọn — chỉ trường khác null mới được cập nhật.
 */
@Data
public class UpdateStoreRequest {

    @Size(max = 255, message = "Tên gian hàng tối đa 255 ký tự")
    private String storeName;

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
