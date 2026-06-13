package com.ecommerce.gocgac.common.service;

import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.exception.AuthException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Dịch vụ dùng chung để phân giải người dùng đang đăng nhập từ JWT (Keycloak)
 * sang thực thể {@link User} nội bộ.
 *
 * <p>Thay thế cho đoạn {@code getCurrentUserId()} bị lặp ở nhiều controller.
 * Việc phân giải dựa trên email trong token (khớp với hành vi hiện có của hệ thống).
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    /**
     * Lấy thực thể User đang đăng nhập.
     *
     * @throws AuthException nếu không xác định được người dùng từ token
     * @throws ResourceNotFoundException nếu token hợp lệ nhưng user không tồn tại trong CSDL
     */
    public User getCurrentUser() {
        String email = JwtUtils.getEmail();
        if (email == null) {
            throw new AuthException("Không thể xác định người dùng từ token");
        }
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
    }

    /**
     * Lấy ID nội bộ của người dùng đang đăng nhập.
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Lấy email của người dùng đang đăng nhập (có thể null nếu chưa đăng nhập).
     */
    public String getCurrentEmail() {
        return JwtUtils.getEmail();
    }
}
