package com.ecommerce.gocgac.scheduler;

import com.ecommerce.gocgac.service.affiliate.AffiliateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Duyệt hoa hồng tiếp thị liên kết sau khi đơn DELIVERED qua hạn cooling-off (M17).
 * Chạy hằng ngày lúc 03:00.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AffiliateCommissionScheduler {

    private final AffiliateService affiliateService;

    @Scheduled(cron = "0 0 3 * * *")
    public void approveDueCommissions() {
        try {
            affiliateService.approveDueCommissions();
        } catch (Exception e) {
            log.error("Lỗi khi duyệt hoa hồng affiliate định kỳ", e);
        }
    }
}
