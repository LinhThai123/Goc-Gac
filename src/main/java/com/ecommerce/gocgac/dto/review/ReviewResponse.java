package com.ecommerce.gocgac.dto.review;

import com.ecommerce.gocgac.entity.ProductReview;
import com.ecommerce.gocgac.entity.enums.ReviewStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ReviewResponse {

    private Long id;
    private Long productId;
    private Long userId;
    private Long orderId;
    private Integer rating;
    private String reviewTitle;
    private String reviewContent;
    private List<String> images;
    private Integer helpfulCount;
    private Boolean isVerifiedPurchase;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private List<ReviewReplyResponse> replies;

    public static ReviewResponse from(ProductReview r, List<ReviewReplyResponse> replies) {
        return ReviewResponse.builder()
            .id(r.getId())
            .productId(r.getProductId())
            .userId(r.getUserId())
            .orderId(r.getOrderId())
            .rating(r.getRating())
            .reviewTitle(r.getReviewTitle())
            .reviewContent(r.getReviewContent())
            .images(parseImages(r.getImages()))
            .helpfulCount(r.getHelpfulCount())
            .isVerifiedPurchase(r.getIsVerifiedPurchase())
            .status(r.getStatus())
            .createdAt(r.getCreatedAt())
            .replies(replies)
            .build();
    }

    private static List<String> parseImages(String images) {
        if (images == null || images.isBlank()) {
            return List.of();
        }
        return Arrays.stream(images.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
}
