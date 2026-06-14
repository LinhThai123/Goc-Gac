package com.ecommerce.gocgac.dto.system;

import com.ecommerce.gocgac.entity.BackupLog;
import com.ecommerce.gocgac.entity.enums.BackupStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BackupLogResponse {

    private Long id;
    private String backupName;
    private String backupType;
    private String filePath;
    private Long fileSize;
    private BackupStatus status;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public static BackupLogResponse from(BackupLog b) {
        return BackupLogResponse.builder()
            .id(b.getId())
            .backupName(b.getBackupName())
            .backupType(b.getBackupType())
            .filePath(b.getFilePath())
            .fileSize(b.getFileSize())
            .status(b.getStatus())
            .errorMessage(b.getErrorMessage())
            .startedAt(b.getStartedAt())
            .completedAt(b.getCompletedAt())
            .build();
    }
}
