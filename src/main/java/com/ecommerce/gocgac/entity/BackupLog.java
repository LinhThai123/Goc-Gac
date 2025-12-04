package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.BackupStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "backup_logs", indexes = {
    @Index(name = "idx_backup_logs_status", columnList = "status"),
    @Index(name = "idx_backup_logs_started", columnList = "started_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BackupLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "backup_name", nullable = false)
    private String backupName;
    
    @Column(name = "backup_type", nullable = false, length = 50)
    private String backupType;
    
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackupStatus status = BackupStatus.IN_PROGRESS;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt = LocalDateTime.now();
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
}

