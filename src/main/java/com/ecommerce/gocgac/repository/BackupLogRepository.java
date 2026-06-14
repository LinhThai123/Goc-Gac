package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.BackupLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackupLogRepository extends JpaRepository<BackupLog, Long> {

    Page<BackupLog> findAllByOrderByStartedAtDesc(Pageable pageable);
}
