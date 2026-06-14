package com.ecommerce.gocgac.scheduler;

import com.ecommerce.gocgac.service.system.BackupLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tác vụ sao lưu CSDL định kỳ (M20) — chạy hằng ngày lúc 02:00.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackupScheduler {

    private final BackupLogService backupLogService;

    @Scheduled(cron = "0 0 2 * * *")
    public void dailyBackup() {
        try {
            backupLogService.createBackup(null, "SCHEDULED");
        } catch (Exception e) {
            log.error("Lỗi khi chạy sao lưu định kỳ", e);
        }
    }
}
