package com.ecommerce.gocgac.service.auth;

import com.ecommerce.gocgac.dto.admin.RoleSummaryResponse;
import com.ecommerce.gocgac.entity.Role;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.RoleRepository;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gán / thu hồi vai trò cho người dùng ở cấp CSDL (Sprint 13 - M02).
 *
 * <p>Lưu ý: thay đổi ở bảng {@code user_roles}. Việc đồng bộ vai trò sang Keycloak
 * (để JWT phản ánh ngay) là bước riêng, chưa nằm trong phạm vi này.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleSummaryResponse> getUserRoles(Long userId) {
        User user = getUser(userId);
        return user.getRoles().stream().map(RoleSummaryResponse::from).toList();
    }

    @Transactional
    public List<RoleSummaryResponse> assignRole(Long userId, Long roleId) {
        User user = getUser(userId);
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vai trò không tồn tại"));
        boolean exists = user.getRoles().stream().anyMatch(r -> r.getId().equals(roleId));
        if (!exists) {
            user.getRoles().add(role);
            userRepository.save(user);
            log.info("Assigned role {} to user {}", role.getCode(), userId);
        }
        return user.getRoles().stream().map(RoleSummaryResponse::from).toList();
    }

    @Transactional
    public List<RoleSummaryResponse> revokeRole(Long userId, Long roleId) {
        User user = getUser(userId);
        user.getRoles().removeIf(r -> r.getId().equals(roleId));
        userRepository.save(user);
        log.info("Revoked role {} from user {}", roleId, userId);
        return user.getRoles().stream().map(RoleSummaryResponse::from).toList();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
    }
}
