package com.ecommerce.gocgac.dto.affiliate;

import com.ecommerce.gocgac.entity.AffiliateLink;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AffiliateLinkResponse {

    private Long id;
    private Long productId;
    private Long storeId;
    private String affiliateCode;
    private String linkUrl;
    private Integer clickCount;
    private Integer conversionCount;

    public static AffiliateLinkResponse from(AffiliateLink l) {
        return AffiliateLinkResponse.builder()
            .id(l.getId())
            .productId(l.getProductId())
            .storeId(l.getStoreId())
            .affiliateCode(l.getAffiliateCode())
            .linkUrl(l.getLinkUrl())
            .clickCount(l.getClickCount())
            .conversionCount(l.getConversionCount())
            .build();
    }
}
