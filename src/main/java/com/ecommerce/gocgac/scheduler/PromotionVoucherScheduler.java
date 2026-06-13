package com.ecommerce.gocgac.scheduler;

import com.ecommerce.gocgac.service.promotion.PromotionService;
import com.ecommerce.gocgac.service.voucher.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tác vụ định kỳ cho voucher &amp; khuyến mãi (M12):
 * - Hết hạn voucher quá end_date.
 * - Kích hoạt / hết hạn khuyến mãi theo mốc thời gian.
 *
 * Chạy mỗi đầu giờ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionVoucherScheduler {

    private final VoucherService voucherService;
    private final PromotionService promotionService;

    @Scheduled(cron = "0 0 * * * *")
    public void run() {
        try {
            voucherService.expireOverdueVouchers();
            promotionService.refreshStatuses();
        } catch (Exception e) {
            log.error("Lỗi khi chạy tác vụ voucher/khuyến mãi định kỳ", e);
        }
    }
}
