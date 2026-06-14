package com.ecommerce.gocgac.dto.ad;

import com.ecommerce.gocgac.entity.Banner;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BannerResponse {

    private Long id;
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

    public static BannerResponse from(Banner b) {
        return BannerResponse.builder()
            .id(b.getId())
            .bannerName(b.getBannerName())
            .bannerPosition(b.getBannerPosition())
            .imageUrl(b.getImageUrl())
            .mobileImageUrl(b.getMobileImageUrl())
            .linkUrl(b.getLinkUrl())
            .targetType(b.getTargetType())
            .startDate(b.getStartDate())
            .endDate(b.getEndDate())
            .displayOrder(b.getDisplayOrder())
            .isActive(b.getIsActive())
            .build();
    }
}
