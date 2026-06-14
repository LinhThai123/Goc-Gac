package com.ecommerce.gocgac.dto.ad;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateBannerRequest {

    @NotBlank(message = "Tên banner không được để trống")
    private String bannerName;

    @NotBlank(message = "Vị trí banner không được để trống")
    private String bannerPosition;

    @NotBlank(message = "Ảnh banner không được để trống")
    private String imageUrl;

    private String mobileImageUrl;
    private String linkUrl;
    private String targetType; // _self | _blank
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer displayOrder;
}
