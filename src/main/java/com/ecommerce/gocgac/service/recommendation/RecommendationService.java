package com.ecommerce.gocgac.service.recommendation;

import com.ecommerce.gocgac.dto.recommendation.RecommendedProductResponse;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.ProductRecommendation;
import com.ecommerce.gocgac.entity.UserProductView;
import com.ecommerce.gocgac.entity.enums.RecommendationType;
import com.ecommerce.gocgac.repository.OrderItemRepository;
import com.ecommerce.gocgac.repository.ProductRecommendationRepository;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.repository.UserProductViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Gợi ý sản phẩm (Sprint 9 - M08).
 *
 * <p>Phục vụ: "sản phẩm liên quan" (theo {@code product_recommendations}, fallback cùng danh mục)
 * và "gợi ý cho bạn" (cá nhân hóa theo lịch sử xem). Sinh dữ liệu gợi ý bằng batch:
 * tương tự theo danh mục (SIMILAR) và mua kèm theo đơn hàng (FREQUENTLY_BOUGHT).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ProductRecommendationRepository recommendationRepository;
    private final ProductRepository productRepository;
    private final UserProductViewRepository viewRepository;
    private final OrderItemRepository orderItemRepository;

    private static final int SIMILAR_PER_PRODUCT = 10;
    private static final int FBT_MIN_COOCCURRENCE = 2;
    private static final BigDecimal SCORE_SIMILAR = new BigDecimal("1.0000");

    // ========== Serving ==========

    /** Sản phẩm liên quan tới một sản phẩm. */
    public List<RecommendedProductResponse> getRelatedProducts(Long productId, int limit) {
        List<Long> ids = recommendationRepository.findBySourceProductIdOrderByScoreDesc(productId).stream()
            .map(ProductRecommendation::getRecommendedProductId)
            .distinct()
            .limit(limit)
            .collect(Collectors.toCollection(ArrayList::new));

        if (ids.isEmpty()) {
            return sameCategoryFallback(productId, limit);
        }
        return loadPublicInOrder(ids);
    }

    /** Gợi ý cá nhân hóa cho người dùng theo lịch sử xem. */
    public List<RecommendedProductResponse> getForYou(Long userId, int limit) {
        List<Long> viewedIds = viewRepository.findTop50ByUserIdOrderByViewedAtDesc(userId).stream()
            .map(UserProductView::getProductId)
            .distinct()
            .toList();
        if (viewedIds.isEmpty()) {
            return List.of();
        }
        Set<Long> exclude = new HashSet<>(viewedIds);

        // Lấy sản phẩm được gợi ý từ các sản phẩm đã xem
        LinkedHashSet<Long> candidates = new LinkedHashSet<>();
        for (ProductRecommendation r : recommendationRepository.findBySourceProductIdInOrderByScoreDesc(viewedIds)) {
            if (!exclude.contains(r.getRecommendedProductId())) {
                candidates.add(r.getRecommendedProductId());
            }
        }

        // Fallback theo danh mục của sản phẩm đã xem nếu còn thiếu
        if (candidates.size() < limit) {
            List<Long> categoryIds = productRepository.findAllById(viewedIds).stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
            if (!categoryIds.isEmpty()) {
                productRepository.findPublicByCategoryIds(categoryIds, PageRequest.of(0, limit * 3)).stream()
                    .map(Product::getId)
                    .filter(id -> !exclude.contains(id))
                    .forEach(candidates::add);
            }
        }

        return loadPublicInOrder(candidates.stream().limit(limit).toList());
    }

    // ========== Generation (batch) ==========

    @Transactional
    public int regenerateSimilar() {
        recommendationRepository.deleteByRecommendationType(RecommendationType.SIMILAR);
        List<Product> products = productRepository.findAllPublic();
        Map<Long, List<Product>> byCategory = products.stream()
            .filter(p -> p.getCategoryId() != null)
            .collect(Collectors.groupingBy(Product::getCategoryId));

        List<ProductRecommendation> batch = new ArrayList<>();
        for (Product p : products) {
            if (p.getCategoryId() == null) continue;
            int added = 0;
            for (Product other : byCategory.get(p.getCategoryId())) {
                if (other.getId().equals(p.getId())) continue;
                batch.add(rec(p.getId(), other.getId(), RecommendationType.SIMILAR, SCORE_SIMILAR));
                if (++added >= SIMILAR_PER_PRODUCT) break;
            }
        }
        recommendationRepository.saveAll(batch);
        log.info("Generated {} SIMILAR recommendations", batch.size());
        return batch.size();
    }

    @Transactional
    public int regenerateFrequentlyBought() {
        recommendationRepository.deleteByRecommendationType(RecommendationType.FREQUENTLY_BOUGHT);

        // Gom productId theo orderId
        Map<Long, Set<Long>> byOrder = new HashMap<>();
        for (Object[] row : orderItemRepository.findAllOrderProductPairs()) {
            Long orderId = ((Number) row[0]).longValue();
            Long productId = ((Number) row[1]).longValue();
            byOrder.computeIfAbsent(orderId, k -> new HashSet<>()).add(productId);
        }

        // Đếm số lần xuất hiện cùng nhau (có hướng)
        Map<String, Integer> pairCount = new HashMap<>();
        for (Set<Long> items : byOrder.values()) {
            List<Long> list = new ArrayList<>(items);
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < list.size(); j++) {
                    if (i == j) continue;
                    pairCount.merge(list.get(i) + ":" + list.get(j), 1, Integer::sum);
                }
            }
        }

        int max = pairCount.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        List<ProductRecommendation> batch = new ArrayList<>();
        for (Map.Entry<String, Integer> e : pairCount.entrySet()) {
            if (e.getValue() < FBT_MIN_COOCCURRENCE) continue;
            String[] parts = e.getKey().split(":");
            BigDecimal score = BigDecimal.valueOf((double) e.getValue() / max).setScale(4, RoundingMode.HALF_UP);
            batch.add(rec(Long.parseLong(parts[0]), Long.parseLong(parts[1]),
                RecommendationType.FREQUENTLY_BOUGHT, score));
        }
        recommendationRepository.saveAll(batch);
        log.info("Generated {} FREQUENTLY_BOUGHT recommendations", batch.size());
        return batch.size();
    }

    @Transactional
    public Map<String, Integer> regenerateAll() {
        int similar = regenerateSimilar();
        int fbt = regenerateFrequentlyBought();
        return Map.of("similar", similar, "frequentlyBought", fbt);
    }

    // ========== Helpers ==========

    private List<RecommendedProductResponse> sameCategoryFallback(Long productId, int limit) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getCategoryId() == null) {
            return List.of();
        }
        return productRepository.findPublicByCategoryIds(List.of(product.getCategoryId()),
                PageRequest.of(0, limit + 1)).stream()
            .filter(p -> !p.getId().equals(productId))
            .limit(limit)
            .map(RecommendedProductResponse::from)
            .toList();
    }

    /** Load sản phẩm public theo đúng thứ tự id đầu vào. */
    private List<RecommendedProductResponse> loadPublicInOrder(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        Map<Long, Product> byId = productRepository.findPublicProductsByIdsList(ids).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<RecommendedProductResponse> result = new ArrayList<>();
        for (Long id : ids) {
            Product p = byId.get(id);
            if (p != null) {
                result.add(RecommendedProductResponse.from(p));
            }
        }
        return result;
    }

    private ProductRecommendation rec(Long source, Long recommended, RecommendationType type, BigDecimal score) {
        ProductRecommendation r = new ProductRecommendation();
        r.setSourceProductId(source);
        r.setRecommendedProductId(recommended);
        r.setRecommendationType(type);
        r.setScore(score);
        return r;
    }
}
