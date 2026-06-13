package com.ecommerce.gocgac.service.review;

import com.ecommerce.gocgac.common.response.PageResponse;
import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.review.CreateReviewRequest;
import com.ecommerce.gocgac.dto.review.ReviewReplyResponse;
import com.ecommerce.gocgac.dto.review.ReviewResponse;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.ProductReview;
import com.ecommerce.gocgac.entity.ReviewReply;
import com.ecommerce.gocgac.entity.enums.OrderStatus;
import com.ecommerce.gocgac.entity.enums.ReviewStatus;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.OrderItemRepository;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.repository.ProductReviewRepository;
import com.ecommerce.gocgac.repository.ReviewReplyRepository;
import com.ecommerce.gocgac.service.notification.NotificationService;
import com.ecommerce.gocgac.service.store.StoreResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Dịch vụ Đánh giá &amp; nhận xét sản phẩm (Sprint 7 - M14).
 *
 * <p>Chỉ khách đã mua &amp; nhận hàng mới được đánh giá. Đánh giá ở trạng thái PENDING
 * cho tới khi admin duyệt (APPROVED) mới hiển thị công khai và tính vào điểm trung bình.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ReviewReplyRepository replyRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreResolver storeResolver;
    private final NotificationService notificationService;

    private static final String REF_REVIEW = "REVIEW";

    // ========== Customer ==========

    @Transactional
    public ReviewResponse createReview(Long userId, CreateReviewRequest req) {
        Product product = productRepository.findById(req.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        boolean purchased = orderItemRepository.hasUserPurchasedProduct(
            userId, product.getId(), OrderStatus.DELIVERED);
        if (!purchased) {
            throw new BusinessException("Bạn chỉ có thể đánh giá sản phẩm đã mua và nhận hàng");
        }
        if (reviewRepository.existsByProductIdAndUserId(product.getId(), userId)) {
            throw new BusinessException("Bạn đã đánh giá sản phẩm này rồi");
        }

        ProductReview review = new ProductReview();
        review.setProductId(product.getId());
        review.setUserId(userId);
        review.setOrderId(req.getOrderId());
        review.setRating(req.getRating());
        review.setReviewTitle(XssSanitizer.sanitize(req.getReviewTitle()));
        review.setReviewContent(XssSanitizer.sanitizeHtml(req.getReviewContent()));
        review.setImages(joinImages(req.getImages()));
        review.setHelpfulCount(0);
        review.setIsVerifiedPurchase(true);
        review.setStatus(ReviewStatus.PENDING);
        review = reviewRepository.save(review);
        log.info("Review {} created by user {} for product {}", review.getId(), userId, product.getId());
        return buildResponse(review);
    }

    public PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        return PageResponse.from(
            reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, ReviewStatus.APPROVED, pageable)
                .map(this::buildResponse));
    }

    @Transactional
    public void markHelpful(Long reviewId) {
        ProductReview review = getReview(reviewId);
        review.setHelpfulCount((review.getHelpfulCount() != null ? review.getHelpfulCount() : 0) + 1);
        reviewRepository.save(review);
    }

    // ========== Seller reply ==========

    @Transactional
    public ReviewResponse reply(Long sellerUserId, Long reviewId, String content) {
        ProductReview review = getReview(reviewId);
        Product product = productRepository.findById(review.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        if (!product.getStoreId().equals(storeId)) {
            throw new BusinessException("Bạn chỉ có thể phản hồi đánh giá sản phẩm của gian hàng mình");
        }

        ReviewReply reply = new ReviewReply();
        reply.setReviewId(reviewId);
        reply.setUserId(sellerUserId);
        reply.setReplyContent(XssSanitizer.sanitizeHtml(content));
        replyRepository.save(reply);

        notificationService.notify(review.getUserId(), "REVIEW", "Phản hồi đánh giá",
            "Gian hàng đã phản hồi đánh giá của bạn.", REF_REVIEW, reviewId);
        return buildResponse(review);
    }

    // ========== Admin moderation ==========

    public PageResponse<ReviewResponse> getPendingReviews(Pageable pageable) {
        return PageResponse.from(
            reviewRepository.findByStatusOrderByCreatedAtDesc(ReviewStatus.PENDING, pageable)
                .map(this::buildResponse));
    }

    @Transactional
    public ReviewResponse approve(Long reviewId) {
        ProductReview review = getReview(reviewId);
        review.setStatus(ReviewStatus.APPROVED);
        reviewRepository.save(review);
        recomputeRating(review.getProductId());
        notificationService.notify(review.getUserId(), "REVIEW", "Đánh giá được duyệt",
            "Đánh giá của bạn đã được duyệt và hiển thị.", REF_REVIEW, reviewId);
        return buildResponse(review);
    }

    @Transactional
    public ReviewResponse hide(Long reviewId) {
        ProductReview review = getReview(reviewId);
        review.setStatus(ReviewStatus.HIDDEN);
        reviewRepository.save(review);
        recomputeRating(review.getProductId());
        return buildResponse(review);
    }

    // ========== Helpers ==========

    /** Tính lại điểm trung bình &amp; số lượng đánh giá (chỉ tính review APPROVED). */
    @Transactional
    public void recomputeRating(Long productId) {
        Double avg = reviewRepository.averageRating(productId, ReviewStatus.APPROVED);
        long count = reviewRepository.countByProductIdAndStatus(productId, ReviewStatus.APPROVED);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        BigDecimal average = BigDecimal.valueOf(avg != null ? avg : 0.0).setScale(2, RoundingMode.HALF_UP);
        product.setRatingAverage(average);
        product.setRatingCount((int) count);
        productRepository.save(product);
        log.debug("Product {} rating updated: avg={}, count={}", productId, average, count);
    }

    private ProductReview getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));
    }

    private ReviewResponse buildResponse(ProductReview review) {
        List<ReviewReplyResponse> replies = replyRepository.findByReviewIdOrderByCreatedAtAsc(review.getId())
            .stream().map(ReviewReplyResponse::from).toList();
        return ReviewResponse.from(review, replies);
    }

    private String joinImages(List<String> images) {
        if (images == null || images.isEmpty()) return null;
        return String.join(",", images);
    }
}
