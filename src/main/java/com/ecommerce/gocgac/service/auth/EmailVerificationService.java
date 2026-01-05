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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    
    private final UserRepository userRepository;
    private final KeycloakClient keycloakClient;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${server.port:8086}")
    private int serverPort;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
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
            log.info("Đang gửi email verification cho user: {} (Keycloak ID: {})", email, user.getKeycloakId());
            
            // Thử gửi với callback URL trước
            // Nếu lỗi "Invalid redirect uri", sẽ thử lại không có redirect URI
            String callbackUrl = buildCallbackUrl(email);
            log.debug("Callback URL: {}", callbackUrl);
            
            try {
                // Gửi email với redirect URI trỏ về callback endpoint
                // Keycloak sẽ redirect về URL này sau khi user verify email thành công
                keycloakClient.sendVerificationEmail(user.getKeycloakId(), keycloakClient.getClientId(), callbackUrl);
                log.info("✅ Đã gửi email verification từ Keycloak cho user: {} với callback URL: {}", email, callbackUrl);
            } catch (Exception e) {
                // Nếu lỗi "Invalid redirect uri", thử lại không có redirect URI
                if (e.getMessage() != null && e.getMessage().contains("Invalid redirect uri")) {
                    log.warn("⚠️ Redirect URI chưa được cấu hình trong Keycloak client. " +
                            "Đang thử gửi email không có redirect URI. " +
                            "Vui lòng cấu hình redirect URI trong Keycloak để sử dụng callback: {}", callbackUrl);
                    
                    // Thử lại không có redirect URI
                    keycloakClient.sendVerificationEmail(user.getKeycloakId(), keycloakClient.getClientId(), null);
                    log.info("✅ Đã gửi email verification từ Keycloak cho user: {} (không có redirect URI)", email);
                    log.warn("⚠️ Lưu ý: User sẽ không được redirect về callback endpoint sau khi verify. " +
                            "Cần cấu hình redirect URI trong Keycloak client để sử dụng callback.");
                } else {
                    // Nếu lỗi khác, throw lại
                    throw e;
                }
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email verification cho user {}: {}", email, e.getMessage(), e);
            
            // Kiểm tra nếu lỗi liên quan đến cấu hình email
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("SMTP") || errorMessage.contains("email"))) {
                throw new AuthException("Không thể gửi email xác thực. Vui lòng kiểm tra cấu hình SMTP trong Keycloak Admin Console: " + e.getMessage());
            }
            
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
    
    /**
     * Đồng bộ email verified status từ Keycloak vào database cho một user cụ thể
     * Method này được gọi từ callback endpoint khi Keycloak redirect về
     */
    @Transactional
    public void syncEmailVerifiedStatus(String email) {
        try {
            checkEmailVerifiedStatus(email);
            log.info("Đã đồng bộ email verified status cho user: {}", email);
        } catch (Exception e) {
            log.error("Lỗi khi đồng bộ email verified status cho user {}: {}", email, e.getMessage(), e);
            throw new AuthException("Không thể đồng bộ email verified status: " + e.getMessage());
        }
    }
    
    /**
     * Đồng bộ email verified status từ Keycloak vào database cho tất cả users chưa verified
     * 
     * Lưu ý: Method này KHÔNG được dùng trong callback endpoint nữa.
     * Callback chỉ sync user có email cụ thể (trùng với email đang được xác thực).
     * 
     * Method này có thể dùng cho:
     * - Scheduled job để đồng bộ định kỳ
     * - Admin tool để sync tất cả users
     * 
     * Chỉ update những users có emailVerified = false trong database nhưng đã verified trong Keycloak
     * (tức là chỉ update từ false sang true, không update từ true sang false)
     */
    @Transactional
    public void syncAllUnverifiedUsers() {
        try {
            // Lấy tất cả users chưa verified trong database
            List<User> unverifiedUsers = userRepository.findByEmailVerifiedFalse();
            
            if (unverifiedUsers.isEmpty()) {
                log.debug("Không có users chưa verified để đồng bộ");
                return;
            }
            
            log.info("Bắt đầu đồng bộ email verified status cho {} users chưa verified", unverifiedUsers.size());
            
            int syncedCount = 0;
            for (User user : unverifiedUsers) {
                if (user.getKeycloakId() != null) {
                    try {
                        // Chỉ check những users có emailVerified = false trong database
                        // Nếu Keycloak đã verify (true), thì update database
                        boolean isVerifiedInKeycloak = keycloakClient.isEmailVerified(user.getKeycloakId());
                        
                        // Chỉ update nếu Keycloak đã verify (từ false sang true)
                        if (isVerifiedInKeycloak && !user.getEmailVerified()) {
                            user.setEmailVerified(true);
                            userRepository.save(user);
                            syncedCount++;
                            log.info("✅ Đã cập nhật email_verified từ false sang true cho user: {}", user.getEmail());
                        }
                    } catch (Exception e) {
                        log.warn("Không thể đồng bộ email verified status cho user {}: {}", 
                            user.getEmail(), e.getMessage());
                    }
                }
            }
            
            log.info("Hoàn thành đồng bộ: đã cập nhật {} users từ false sang true", syncedCount);
        } catch (Exception e) {
            log.error("Lỗi khi đồng bộ tất cả users: {}", e.getMessage(), e);
            throw new AuthException("Không thể đồng bộ tất cả users: " + e.getMessage());
        }
    }
    
    /**
     * Xây dựng callback URL để Keycloak redirect về sau khi verify email
     * URL này bao gồm email trong query params để callback có thể xác định user cần sync
     * 
     * Lưu ý: Keycloak có thể không giữ query params khi redirect.
     * Nếu không có email trong callback, sẽ không sync gì cả (chỉ log warning).
     */
    private String buildCallbackUrl(String email) {
        try {
            // Xây dựng URL callback
            // Lưu ý: Keycloak không chấp nhận query params trong redirect URI validation
            // Nên chỉ dùng path, không dùng query params
            // Callback sẽ tự động sync tất cả users chưa verified khi được gọi
            String baseUrl = System.getProperty("app.base.url", "http://localhost:" + serverPort);
            if (contextPath != null && !contextPath.isEmpty()) {
                baseUrl += contextPath;
            }
            String callbackPath = "/api/auth/email-verification/callback";
            String callbackUrl = baseUrl + callbackPath;
            log.debug("Built callback URL (không có query params vì Keycloak không hỗ trợ): {}", callbackUrl);
            return callbackUrl;
        } catch (Exception e) {
            log.warn("Không thể build callback URL, sử dụng URL mặc định: {}", e.getMessage());
            String baseUrl = "http://localhost:" + serverPort;
            if (contextPath != null && !contextPath.isEmpty()) {
                baseUrl += contextPath;
            }
            return baseUrl + "/api/auth/email-verification/callback";
        }
    }
    
    /**
     * Lấy frontend URL để redirect về sau khi verify
     */
    public String getFrontendUrl() {
        return frontendUrl;
    }
}

