package com.ecommerce.gocgac.service.auth;

import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.exception.AuthException;
import com.ecommerce.gocgac.external.KeycloakClient;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    
    private final UserRepository userRepository;
    private final KeycloakClient keycloakClient;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    /**
     * Gửi email verification từ Keycloak
     * Keycloak sẽ tự động gửi email với link verification
     * 
     * Lưu ý: Không dùng @Transactional vì method này được gọi từ transaction khác
     * Nếu cần update database, sẽ được handle trong transaction cha
     */
    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("Email không tồn tại trong hệ thống"));
        
        if (user.getKeycloakId() == null) {
            throw new AuthException("User chưa được liên kết với Keycloak");
        }
        
        // Kiểm tra email đã verified chưa (chỉ check, không update database)
        boolean isVerified = keycloakClient.isEmailVerified(user.getKeycloakId());
        if (isVerified) {
            // Email đã verified, không cần gửi lại
            log.info("Email {} đã được xác thực, không cần gửi lại", email);
            return; // Return thay vì throw exception để tránh rollback transaction
        }
        
        // Gửi email verification từ Keycloak
        try {
            // Không truyền redirect_uri để tránh lỗi "Invalid redirect uri"
            // Keycloak sẽ tự xử lý redirect sau khi verify
            // Nếu muốn redirect về frontend, cần cấu hình redirect URI trong Keycloak client trước
            keycloakClient.sendVerificationEmail(user.getKeycloakId(), keycloakClient.getClientId(), null);
            log.info("Đã gửi email verification từ Keycloak cho user: {}", email);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email verification cho user {}: {}", email, e.getMessage(), e);
            throw new AuthException("Không thể gửi email xác thực: " + e.getMessage());
        }
    }
    
    /**
     * Gửi lại email verification
     */
    public void resendVerificationEmail(String email) {
        sendVerificationEmail(email);
    }
    
    /**
     * Kiểm tra trạng thái email verified
     * Đồng bộ với Keycloak và cập nhật database
     */
    @Transactional
    public boolean checkEmailVerifiedStatus(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("Email không tồn tại trong hệ thống"));
        
        if (user.getKeycloakId() == null) {
            return false;
        }
        
        // Lấy status từ Keycloak
        boolean isVerified = keycloakClient.isEmailVerified(user.getKeycloakId());
        
        // Đồng bộ với database
        if (isVerified != user.getEmailVerified()) {
            user.setEmailVerified(isVerified);
            userRepository.save(user);
            log.info("Đã đồng bộ email verified status cho user {}: {}", email, isVerified);
        }
        
        return isVerified;
    }
    
    /**
     * Verify email manually (nếu cần)
     * Thường thì Keycloak sẽ tự verify khi user click link trong email
     * Method này dùng để verify thủ công nếu cần
     */
    @Transactional
    public void verifyEmailManually(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("Email không tồn tại trong hệ thống"));
        
        if (user.getKeycloakId() == null) {
            throw new AuthException("User chưa được liên kết với Keycloak");
        }
        
        // Verify trong Keycloak
        try {
            keycloakClient.verifyEmail(user.getKeycloakId());
            
            // Đồng bộ với database
            user.setEmailVerified(true);
            userRepository.save(user);
            
            log.info("Đã verify email manually cho user: {}", email);
        } catch (Exception e) {
            log.error("Lỗi khi verify email manually cho user {}: {}", email, e.getMessage(), e);
            throw new AuthException("Không thể verify email: " + e.getMessage());
        }
    }
}

