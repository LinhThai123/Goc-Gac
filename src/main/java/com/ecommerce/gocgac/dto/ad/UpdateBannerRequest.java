package com.ecommerce.gocgac.dto.ad;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateBannerRequest {

    private String bannerName;
    private String bannerPosition;
    private String imageUrl;
    private String mobileImageUrl;
    private String linkUrl;
    private String targetType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer displayOrder;
    private Boolean isActive;
}
