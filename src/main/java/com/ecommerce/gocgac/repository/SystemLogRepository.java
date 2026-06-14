package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    Page<SystemLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<SystemLog> findByModuleOrderByCreatedAtDesc(String module, Pageable pageable);

    Page<SystemLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
