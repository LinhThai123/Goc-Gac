package com.ecommerce.gocgac.scheduler;

import com.ecommerce.gocgac.service.recommendation.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Sinh lại dữ liệu gợi ý định kỳ (M08) — hằng tuần Chủ nhật 03:00.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationScheduler {

    private final RecommendationService recommendationService;

    @Scheduled(cron = "0 0 3 * * SUN")
    public void weeklyRegenerate() {
        try {
            recommendationService.regenerateAll();
        } catch (Exception e) {
            log.error("Lỗi khi sinh lại gợi ý định kỳ", e);
        }
    }
}
