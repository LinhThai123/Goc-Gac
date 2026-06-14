package com.ecommerce.gocgac.service.recommendation;

import com.ecommerce.gocgac.dto.recommendation.RecommendedProductResponse;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.ProductRecommendation;
import com.ecommerce.gocgac.entity.enums.RecommendationType;
import com.ecommerce.gocgac.repository.OrderItemRepository;
import com.ecommerce.gocgac.repository.ProductRecommendationRepository;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.repository.UserProductViewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock private ProductRecommendationRepository recommendationRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserProductViewRepository viewRepository;
    @Mock private OrderItemRepository orderItemRepository;

    @InjectMocks private RecommendationService recommendationService;

    private Product product(Long id, Long categoryId) {
        Product p = new Product();
        p.setId(id);
        p.setCategoryId(categoryId);
        p.setProductName("SP " + id);
        p.setSlug("sp-" + id);
        return p;
    }

    private ProductRecommendation rec(Long source, Long recommended) {
        ProductRecommendation r = new ProductRecommendation();
        r.setSourceProductId(source);
        r.setRecommendedProductId(recommended);
        r.setRecommendationType(RecommendationType.SIMILAR);
        return r;
    }

    @Test
    @DisplayName("Sản phẩm liên quan: đọc từ bảng gợi ý, giữ đúng thứ tự")
    void getRelated_fromTable() {
        when(recommendationRepository.findBySourceProductIdOrderByScoreDesc(1L))
            .thenReturn(List.of(rec(1L, 2L), rec(1L, 3L)));
        when(productRepository.findPublicProductsByIdsList(List.of(2L, 3L)))
            .thenReturn(List.of(product(2L, 9L), product(3L, 9L)));

        List<RecommendedProductResponse> res = recommendationService.getRelatedProducts(1L, 10);

        assertThat(res).extracting(RecommendedProductResponse::getProductId).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("Không có gợi ý sẵn → fallback cùng danh mục (loại chính nó)")
    void getRelated_fallbackSameCategory() {
        when(recommendationRepository.findBySourceProductIdOrderByScoreDesc(1L)).thenReturn(List.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(1L, 9L)));
        when(productRepository.findPublicByCategoryIds(eq(List.of(9L)), any()))
            .thenReturn(List.of(product(1L, 9L), product(5L, 9L), product(6L, 9L)));

        List<RecommendedProductResponse> res = recommendationService.getRelatedProducts(1L, 10);

        assertThat(res).extracting(RecommendedProductResponse::getProductId).containsExactly(5L, 6L);
    }

    @Test
    @DisplayName("Sinh 'mua kèm': chỉ lưu cặp xuất hiện cùng nhau >= 2 lần")
    void regenerateFrequentlyBought_threshold() {
        // order1: {10,20}, order2: {10,20}, order3: {10,30}
        List<Object[]> pairs = List.of(
            new Object[]{1L, 10L}, new Object[]{1L, 20L},
            new Object[]{2L, 10L}, new Object[]{2L, 20L},
            new Object[]{3L, 10L}, new Object[]{3L, 30L}
        );
        when(orderItemRepository.findAllOrderProductPairs()).thenReturn(pairs);

        int count = recommendationService.regenerateFrequentlyBought();

        verify(recommendationRepository).deleteByRecommendationType(RecommendationType.FREQUENTLY_BOUGHT);
        // (10->20) và (20->10) đạt ngưỡng 2; (10->30),(30->10) chỉ 1 lần nên bị loại
        assertThat(count).isEqualTo(2);

        ArgumentCaptor<List<ProductRecommendation>> captor = ArgumentCaptor.forClass(List.class);
        verify(recommendationRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("'Gợi ý cho bạn' khi chưa xem gì → rỗng")
    void forYou_noHistory_empty() {
        when(viewRepository.findTop50ByUserIdOrderByViewedAtDesc(7L)).thenReturn(List.of());

        assertThat(recommendationService.getForYou(7L, 10)).isEmpty();
    }
}
