package com.ecommerce.gocgac.service.system;

import com.ecommerce.gocgac.common.response.PageResponse;
import com.ecommerce.gocgac.dto.system.SystemLogResponse;
import com.ecommerce.gocgac.entity.SystemLog;
import com.ecommerce.gocgac.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Ghi &amp; truy vấn nhật ký hệ thống (Sprint 13 - M20).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;

    /** Ghi một bản ghi nhật ký (transaction độc lập để không bị cuốn theo request). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String action, String module, String referenceType,
                       Long referenceId, String ipAddress, String userAgent, String details) {
        SystemLog l = new SystemLog();
        l.setUserId(userId);
        l.setAction(action);
        l.setModule(module);
        l.setReferenceType(referenceType);
        l.setReferenceId(referenceId);
        l.setIpAddress(ipAddress);
        l.setUserAgent(userAgent);
        l.setDetails(details);
        systemLogRepository.save(l);
    }

    public PageResponse<SystemLogResponse> query(String module, Long userId, Pageable pageable) {
        if (StringUtils.hasText(module)) {
            return PageResponse.from(systemLogRepository.findByModuleOrderByCreatedAtDesc(module, pageable)
                .map(SystemLogResponse::from));
        }
        if (userId != null) {
            return PageResponse.from(systemLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(SystemLogResponse::from));
        }
        return PageResponse.from(systemLogRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map(SystemLogResponse::from));
    }
}
