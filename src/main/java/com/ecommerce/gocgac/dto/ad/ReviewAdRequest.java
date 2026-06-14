package com.ecommerce.gocgac.dto.ad;

import lombok.Data;

@Data
public class ReviewAdRequest {

    private boolean approved;
    private String rejectionReason;
}
