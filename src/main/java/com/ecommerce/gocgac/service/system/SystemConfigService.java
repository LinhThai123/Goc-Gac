package com.ecommerce.gocgac.service.system;

import com.ecommerce.gocgac.dto.system.SystemConfigResponse;
import com.ecommerce.gocgac.dto.system.UpsertSystemConfigRequest;
import com.ecommerce.gocgac.entity.SystemConfig;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Quản lý cấu hình hệ thống dạng key-value (Sprint 13 - M20).
 */
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository configRepository;

    @Transactional
    public SystemConfigResponse upsert(UpsertSystemConfigRequest req) {
        SystemConfig c = configRepository.findByConfigKey(req.getConfigKey())
            .orElseGet(SystemConfig::new);
        c.setConfigKey(req.getConfigKey());
        c.setConfigValue(req.getConfigValue());
        c.setConfigType(req.getConfigType());
        c.setDescription(req.getDescription());
        c.setIsPublic(req.getIsPublic() != null ? req.getIsPublic() : false);
        return SystemConfigResponse.from(configRepository.save(c));
    }

    public List<SystemConfigResponse> listAll() {
        return configRepository.findAll().stream().map(SystemConfigResponse::from).toList();
    }

    public List<SystemConfigResponse> listPublic() {
        return configRepository.findByIsPublicTrue().stream().map(SystemConfigResponse::from).toList();
    }

    public SystemConfigResponse getByKey(String key) {
        return SystemConfigResponse.from(configRepository.findByConfigKey(key)
            .orElseThrow(() -> new ResourceNotFoundException("Cấu hình không tồn tại: " + key)));
    }

    @Transactional
    public void delete(Long id) {
        if (!configRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cấu hình không tồn tại");
        }
        configRepository.deleteById(id);
    }
}
