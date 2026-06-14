package com.ecommerce.gocgac.service.system;

import com.ecommerce.gocgac.common.response.PageResponse;
import com.ecommerce.gocgac.dto.system.BackupLogResponse;
import com.ecommerce.gocgac.entity.BackupLog;
import com.ecommerce.gocgac.entity.enums.BackupStatus;
import com.ecommerce.gocgac.repository.BackupLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Sao lưu cơ sở dữ liệu (Sprint 13 - M20).
 *
 * <p>Mặc định <b>tắt</b> ({@code gocgac.backup.enabled=false}) để an toàn: khi đó chỉ
 * ghi bản ghi {@link BackupLog} dạng placeholder. Khi bật, thực thi {@code pg_dump}
 * (yêu cầu pg_dump có trong PATH và thư mục đích ghi được).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupLogService {

    private final BackupLogRepository backupLogRepository;

    @Value("${gocgac.backup.enabled:false}")
    private boolean backupEnabled;

    @Value("${gocgac.backup.dir:backups}")
    private String backupDir;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Transactional
    public BackupLogResponse createBackup(Long userId, String type) {
        String name = "backup_" + LocalDateTime.now().toString().replaceAll("[:.]", "-");
        BackupLog backup = new BackupLog();
        backup.setBackupName(name);
        backup.setBackupType(type != null ? type : "MANUAL");
        backup.setStatus(BackupStatus.IN_PROGRESS);
        backup.setCreatedBy(userId);
        backup = backupLogRepository.save(backup);

        if (!backupEnabled) {
            backup.setStatus(BackupStatus.COMPLETED);
            backup.setCompletedAt(LocalDateTime.now());
            backup.setErrorMessage("Backup execution disabled — placeholder (đặt gocgac.backup.enabled=true để bật pg_dump)");
            return BackupLogResponse.from(backupLogRepository.save(backup));
        }

        try {
            File file = runPgDump(name);
            backup.setStatus(BackupStatus.COMPLETED);
            backup.setFilePath(file.getAbsolutePath());
            backup.setFileSize(file.length());
            backup.setCompletedAt(LocalDateTime.now());
            log.info("Backup {} completed: {}", name, file.getAbsolutePath());
        } catch (Exception e) {
            backup.setStatus(BackupStatus.FAILED);
            backup.setErrorMessage(e.getMessage());
            backup.setCompletedAt(LocalDateTime.now());
            log.error("Backup {} failed", name, e);
        }
        return BackupLogResponse.from(backupLogRepository.save(backup));
    }

    public PageResponse<BackupLogResponse> list(Pageable pageable) {
        return PageResponse.from(backupLogRepository.findAllByOrderByStartedAtDesc(pageable)
            .map(BackupLogResponse::from));
    }

    private File runPgDump(String name) throws Exception {
        // jdbc:postgresql://host:port/db
        String stripped = datasourceUrl.replace("jdbc:postgresql://", "");
        String hostPort = stripped.contains("/") ? stripped.substring(0, stripped.indexOf('/')) : stripped;
        String db = stripped.contains("/")
            ? stripped.substring(stripped.indexOf('/') + 1).replaceAll("[?].*$", "") : "postgres";
        String host = hostPort.contains(":") ? hostPort.substring(0, hostPort.indexOf(':')) : hostPort;
        String port = hostPort.contains(":") ? hostPort.substring(hostPort.indexOf(':') + 1) : "5432";

        File dir = new File(backupDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Không tạo được thư mục backup: " + backupDir);
        }
        File outFile = new File(dir, name + ".sql");

        ProcessBuilder pb = new ProcessBuilder(
            "pg_dump", "-h", host, "-p", port, "-U", datasourceUsername,
            "-d", db, "-f", outFile.getAbsolutePath());
        pb.environment().put("PGPASSWORD", datasourcePassword);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        boolean finished = process.waitFor(10, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new IllegalStateException("pg_dump quá thời gian cho phép");
        }
        if (process.exitValue() != 0) {
            throw new IllegalStateException("pg_dump lỗi (exit " + process.exitValue() + ")");
        }
        return outFile;
    }
}
