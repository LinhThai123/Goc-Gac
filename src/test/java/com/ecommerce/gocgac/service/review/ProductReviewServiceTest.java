package com.ecommerce.gocgac.service.review;

import com.ecommerce.gocgac.dto.review.CreateReviewRequest;
import com.ecommerce.gocgac.dto.review.ReviewResponse;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.ProductReview;
import com.ecommerce.gocgac.entity.enums.OrderStatus;
import com.ecommerce.gocgac.entity.enums.ReviewStatus;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.repository.OrderItemRepository;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.repository.ProductReviewRepository;
import com.ecommerce.gocgac.repository.ReviewReplyRepository;
import com.ecommerce.gocgac.service.notification.NotificationService;
import com.ecommerce.gocgac.service.product.ProductSearchService;
import com.ecommerce.gocgac.service.store.StoreResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductReviewServiceTest {

    @Mock private ProductReviewRepository reviewRepository;
    @Mock private ReviewReplyRepository replyRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private StoreResolver storeResolver;
    @Mock private NotificationService notificationService;
    @Mock private ProductSearchService productSearchService;

    @InjectMocks private ProductReviewService reviewService;

    private static final Long USER_ID = 7L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long STORE_ID = 1L;
    private static final Long REVIEW_ID = 9L;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(PRODUCT_ID);
        product.setStoreId(STORE_ID);
        product.setProductName("Trà shan tuyết");
    }

    private CreateReviewRequest request(int rating) {
        CreateReviewRequest r = new CreateReviewRequest();
        r.setProductId(PRODUCT_ID);
        r.setRating(rating);
        r.setReviewContent("Sản phẩm tốt");
        return r;
    }

    private ProductReview review(ReviewStatus status) {
        ProductReview r = new ProductReview();
        r.setId(REVIEW_ID);
        r.setProductId(PRODUCT_ID);
        r.setUserId(USER_ID);
        r.setRating(5);
        r.setHelpfulCount(3);
        r.setStatus(status);
        return r;
    }

    @Test
    @DisplayName("Tạo đánh giá (đã mua): trạng thái PENDING, verified purchase")
    void create_success() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(orderItemRepository.hasUserPurchasedProduct(USER_ID, PRODUCT_ID, OrderStatus.DELIVERED)).thenReturn(true);
        when(reviewRepository.existsByProductIdAndUserId(PRODUCT_ID, USER_ID)).thenReturn(false);
        when(reviewRepository.save(any(ProductReview.class))).thenAnswer(inv -> {
            ProductReview r = inv.getArgument(0);
            r.setId(REVIEW_ID);
            return r;
        });
        when(replyRepository.findByReviewIdOrderByCreatedAtAsc(REVIEW_ID)).thenReturn(List.of());

        ReviewResponse res = reviewService.createReview(USER_ID, request(5));

        assertThat(res.getStatus()).isEqualTo(ReviewStatus.PENDING);
        assertThat(res.getIsVerifiedPurchase()).isTrue();
        assertThat(res.getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("Chưa mua hàng → BusinessException")
    void create_notPurchased_throws() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(orderItemRepository.hasUserPurchasedProduct(USER_ID, PRODUCT_ID, OrderStatus.DELIVERED)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.createReview(USER_ID, request(5)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("đã mua");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đánh giá trùng → BusinessException")
    void create_duplicate_throws() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(orderItemRepository.hasUserPurchasedProduct(USER_ID, PRODUCT_ID, OrderStatus.DELIVERED)).thenReturn(true);
        when(reviewRepository.existsByProductIdAndUserId(PRODUCT_ID, USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(USER_ID, request(5)))
            .isInstanceOf(BusinessException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Duyệt đánh giá: APPROVED + cập nhật rating sản phẩm")
    void approve_recomputesRating() {
        ProductReview rv = review(ReviewStatus.PENDING);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(rv));
        when(reviewRepository.averageRating(PRODUCT_ID, ReviewStatus.APPROVED)).thenReturn(4.5);
        when(reviewRepository.countByProductIdAndStatus(PRODUCT_ID, ReviewStatus.APPROVED)).thenReturn(2L);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(replyRepository.findByReviewIdOrderByCreatedAtAsc(REVIEW_ID)).thenReturn(List.of());

        reviewService.approve(REVIEW_ID);

        assertThat(rv.getStatus()).isEqualTo(ReviewStatus.APPROVED);
        assertThat(product.getRatingAverage()).isEqualByComparingTo("4.50");
        assertThat(product.getRatingCount()).isEqualTo(2);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Phản hồi của gian hàng khác → BusinessException")
    void reply_notOwner_throws() {
        ProductReview rv = review(ReviewStatus.APPROVED);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(rv));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(storeResolver.resolveStoreId(USER_ID)).thenReturn(2L); // khác store của sản phẩm (1)

        assertThatThrownBy(() -> reviewService.reply(USER_ID, REVIEW_ID, "Cảm ơn bạn"))
            .isInstanceOf(BusinessException.class);

        verify(replyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Phản hồi của gian hàng sở hữu → lưu phản hồi")
    void reply_owner_success() {
        ProductReview rv = review(ReviewStatus.APPROVED);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(rv));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(storeResolver.resolveStoreId(USER_ID)).thenReturn(STORE_ID);
        when(replyRepository.findByReviewIdOrderByCreatedAtAsc(REVIEW_ID)).thenReturn(List.of());

        reviewService.reply(USER_ID, REVIEW_ID, "Cảm ơn bạn");

        verify(replyRepository).save(any());
        verify(notificationService).notify(eq(USER_ID), eq("REVIEW"), any(), any(), any(), eq(REVIEW_ID));
    }

    @Test
    @DisplayName("Đánh dấu hữu ích: tăng helpfulCount")
    void markHelpful_increments() {
        ProductReview rv = review(ReviewStatus.APPROVED);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(rv));

        reviewService.markHelpful(REVIEW_ID);

        assertThat(rv.getHelpfulCount()).isEqualTo(4); // 3 + 1
        verify(reviewRepository).save(rv);
    }
}
