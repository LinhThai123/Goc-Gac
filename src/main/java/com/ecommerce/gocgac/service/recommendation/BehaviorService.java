package com.ecommerce.gocgac.service.recommendation;

import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.recommendation.RecommendedProductResponse;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.UserProductView;
import com.ecommerce.gocgac.entity.UserSearchHistory;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.repository.UserProductViewRepository;
import com.ecommerce.gocgac.repository.UserSearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ghi nhận &amp; truy vấn hành vi người dùng (Sprint 9 - M08): lượt xem sản phẩm, lịch sử tìm kiếm.
 */
@Service
@RequiredArgsConstructor
public class BehaviorService {

    private final UserProductViewRepository viewRepository;
    private final UserSearchHistoryRepository searchHistoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void recordView(Long userId, Long productId, Integer viewDuration) {
        UserProductView v = new UserProductView();
        v.setUserId(userId);
        v.setProductId(productId);
        v.setViewDuration(viewDuration);
        viewRepository.save(v);
    }

    @Transactional
    public void recordSearch(Long userId, String keyword, Integer resultCount) {
        UserSearchHistory h = new UserSearchHistory();
        h.setUserId(userId);
        h.setSearchKeyword(XssSanitizer.sanitize(keyword));
        h.setResultCount(resultCount != null ? resultCount : 0);
        searchHistoryRepository.save(h);
    }

    /** Sản phẩm xem gần đây (distinct, giữ thứ tự mới nhất). */
    public List<RecommendedProductResponse> getRecentViewedProducts(Long userId, int limit) {
        List<Long> productIds = viewRepository.findTop50ByUserIdOrderByViewedAtDesc(userId).stream()
            .map(UserProductView::getProductId)
            .distinct()
            .limit(limit)
            .toList();
        return loadInOrder(productIds);
    }

    /** Từ khóa tìm kiếm gần đây (distinct). */
    public List<String> getRecentSearchKeywords(Long userId) {
        return searchHistoryRepository.findTop20ByUserIdOrderBySearchedAtDesc(userId).stream()
            .map(UserSearchHistory::getSearchKeyword)
            .distinct()
            .toList();
    }

    private List<RecommendedProductResponse> loadInOrder(List<Long> productIds) {
        if (productIds.isEmpty()) return List.of();
        Map<Long, Product> byId = productRepository.findAllById(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<RecommendedProductResponse> result = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>();
        for (Long id : productIds) {
            Product p = byId.get(id);
            if (p != null && seen.add(id)) {
                result.add(RecommendedProductResponse.from(p));
            }
        }
        return result;
    }
}
