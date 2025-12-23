package com.ecommerce.gocgac.service.auth;

import com.ecommerce.gocgac.common.response.AuthResponse;
import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.auth.ChangePasswordRequest;
import com.ecommerce.gocgac.dto.auth.GoogleLoginRequest;
import com.ecommerce.gocgac.dto.auth.LoginRequest;
import com.ecommerce.gocgac.dto.auth.RegisterRequest;
import com.ecommerce.gocgac.dto.user.UserDTO;
import com.ecommerce.gocgac.entity.Role;
import com.ecommerce.gocgac.entity.SocialLogin;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.enums.SocialProvider;
import com.ecommerce.gocgac.entity.enums.UserStatus;
import com.ecommerce.gocgac.entity.enums.UserType;
import com.ecommerce.gocgac.exception.AuthException;
import com.ecommerce.gocgac.external.KeycloakClient;
import com.ecommerce.gocgac.repository.RoleRepository;
import com.ecommerce.gocgac.repository.SocialLoginRepository;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SocialLoginRepository socialLoginRepository;
    private final KeycloakClient keycloakClient;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Value("${google.token-info-url:https://oauth2.googleapis.com/tokeninfo}")
    private String googleTokenInfoUrl;

    @Value("${app.social.password-secret:change-me}")
    private String socialPasswordSecret;
    
    /**
     * Đăng nhập
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Xác thực với Keycloak
            Map<String, Object> keycloakResponse = keycloakClient.login(
                request.getEmail(), request.getPassword());
            
            // Lấy user từ database
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Người dùng không tồn tại trong hệ thống"));
            
            // Kiểm tra user có bị khóa không
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new AuthException("Tài khoản của bạn đã bị khóa");
            }
            
            // Cập nhật thời gian đăng nhập cuối
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            return buildAuthResponse(user, keycloakResponse);
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Login error for email {}: {}", request.getEmail(), e.getMessage(), e);
            throw new AuthException("Đăng nhập thất bại: " + e.getMessage());
        }
    }
    
    /**
     * Đăng ký
     */
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        try {
            // Kiểm tra email đã tồn tại chưa
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new AuthException("Email đã được sử dụng");
            }
            
            // Đăng ký user trong Keycloak
            Map<String, Object> keycloakResult = keycloakClient.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getFullName()
            );
            
            String keycloakUserId = (String) keycloakResult.get("userId");
            if (keycloakUserId == null) {
                throw new AuthException("Không thể tạo tài khoản trong Keycloak");
            }
            
            // Tạo user trong database
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
            user.setAddress(request.getAddress());
            user.setUserType(request.getUserType());
            user.setStatus(UserStatus.ACTIVE);
            user.setKeycloakId(keycloakUserId);
            user.setEmailVerified(false);
            user.setPhoneVerified(false);
            user.setLoyaltyPoints(0);
            
            // Gán role mặc định
            Role defaultRole = resolveDefaultRole(request.getUserType());
            user.getRoles().add(defaultRole);
            // Gán role trong Keycloak (BẮT BUỘC - để user có thể đăng nhập và sử dụng API)
            try {
                keycloakClient.assignRoleToUser(keycloakUserId, defaultRole.getCode());
                log.info("Đã gán role {} cho user {} trong Keycloak", defaultRole.getCode(), keycloakUserId);
            } catch (Exception e) {
                log.error("Không thể gán role trong Keycloak: {}", e.getMessage(), e);
                // Nếu không thể gán role, rollback: xóa user trong Keycloak và database
                try {
                    keycloakClient.deleteUser(keycloakUserId);
                    log.info("Đã xóa user {} trong Keycloak do không thể gán role", keycloakUserId);
                } catch (Exception deleteException) {
                    log.error("Không thể xóa user trong Keycloak: {}", deleteException.getMessage());
                }
                throw new AuthException("Không thể gán role cho user. Vui lòng thử lại hoặc liên hệ quản trị viên.");
            }
            
            user = userRepository.save(user);
            // Flush để đảm bảo user đã được lưu vào database trước khi gửi email
            userRepository.flush();
            
            // Gửi email verification từ Keycloak (không block transaction)
            // Sử dụng try-catch riêng để không ảnh hưởng đến transaction
            try {
                emailVerificationService.sendVerificationEmail(user.getEmail());
                log.info("Đã gửi email verification từ Keycloak cho user: {}", user.getEmail());
            } catch (AuthException e) {
                // Nếu email đã verified hoặc lỗi khác, chỉ log warning
                log.warn("Không thể gửi email verification cho user {}: {}", user.getEmail(), e.getMessage());
                // Không throw exception vì user đã được tạo thành công
            } catch (Exception e) {
                log.warn("Lỗi không mong đợi khi gửi email verification cho user {}: {}", user.getEmail(), e.getMessage());
                // Không throw exception vì user đã được tạo thành công
            }
            
            // Lấy roles của user
            List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

            // Tạo UserDTO để trả về
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setEmail(user.getEmail());
            userDTO.setFullName(user.getFullName());
            userDTO.setPhone(user.getPhone());
            userDTO.setAvatarUrl(user.getAvatarUrl());
            userDTO.setRoles(roles);
            userDTO.setUserType(user.getUserType().name());
            
            // Tạo response
            MessageResponse response = new MessageResponse();
            response.setMessage("Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.");
            response.setStatus(HttpStatus.CREATED.value()); // 201
            response.setData(userDTO);
            
            return response;
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Register error for email {}: {}", request.getEmail(), e.getMessage(), e);
            throw new AuthException("Đăng ký thất bại: " + e.getMessage());
        }
    }

    /**
     * Đăng nhập / đăng ký bằng Google ID token
     */
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        try {
            GoogleUser googleUser = verifyGoogleIdToken(request.getIdToken());
            String socialPassword = buildSocialPassword(googleUser.sub());

            Optional<SocialLogin> existingSocial = socialLoginRepository
                .findByProviderAndProviderUserId(SocialProvider.GOOGLE, googleUser.sub());

            User user;
            if (existingSocial.isPresent()) {
                user = existingSocial.get().getUser();
            } else {
                // Nếu email đã tồn tại từ đăng ký thông thường, không tự động liên kết để tránh override mật khẩu
                if (userRepository.findByEmail(googleUser.email()).isPresent()) {
                    throw new AuthException("Email đã tồn tại, vui lòng đăng nhập bằng email/mật khẩu hoặc liên hệ hỗ trợ để liên kết Google.");
                }
                user = createUserFromGoogle(googleUser, socialPassword);
            }

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new AuthException("Tài khoản của bạn đã bị khóa");
            }

            Map<String, Object> keycloakResponse = keycloakClient.login(user.getEmail(), socialPassword);

            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            return buildAuthResponse(user, keycloakResponse);
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google login error: {}", e.getMessage(), e);
            throw new AuthException("Đăng nhập Google thất bại: " + e.getMessage());
        }
    }
    
    /**
     * Refresh token
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            Map<String, Object> keycloakResponse = keycloakClient.refreshToken(refreshToken);
            
            String accessToken = (String) keycloakResponse.get("access_token");
            String newRefreshToken = (String) keycloakResponse.get("refresh_token");
            Long expiresIn = ((Number) keycloakResponse.get("expires_in")).longValue();
            
            AuthResponse response = new AuthResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(newRefreshToken != null ? newRefreshToken : refreshToken);
            response.setExpiresIn(expiresIn);
            
            return response;
        } catch (Exception e) {
            log.error("Refresh token error: {}", e.getMessage(), e);
            throw new AuthException("Refresh token thất bại: " + e.getMessage());
        }
    }

    /**
     * Đổi mật khẩu (self-service) sử dụng Keycloak
     */
    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request) {
        try {
            // Tìm user trong database
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Người dùng không tồn tại trong hệ thống"));

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new AuthException("Tài khoản của bạn đã bị khóa");
            }

            // Gọi Keycloak để đổi mật khẩu (xác thực mật khẩu hiện tại bên Keycloak)
            try {
                keycloakClient.changeUserPassword(
                    request.getEmail(),
                    request.getCurrentPassword(),
                    request.getNewPassword()
                );
            } catch (RuntimeException e) {
                // Chuyển message từ Keycloak thành AuthException dễ hiểu
                String msg = e.getMessage();
                if (msg != null && msg.contains("Mật khẩu hiện tại không đúng")) {
                    throw new AuthException("Mật khẩu hiện tại không đúng");
                }
                log.error("Lỗi khi đổi mật khẩu trong Keycloak cho user {}: {}", request.getEmail(), msg);
                throw new AuthException("Không thể đổi mật khẩu: " + msg);
            }

            // Cập nhật mật khẩu hash trong database để đồng bộ
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            MessageResponse response = new MessageResponse();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Đổi mật khẩu thành công");
            response.setData(null);
            return response;
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Change password error for email {}: {}", request.getEmail(), e.getMessage(), e);
            throw new AuthException("Đổi mật khẩu thất bại: " + e.getMessage());
        }
    }

    private Role resolveDefaultRole(UserType userType) {
        return roleRepository.findByCode(userType.name())
            .orElseGet(() -> roleRepository.findByCode("CUSTOMER")
                .orElseThrow(() -> new AuthException("Role CUSTOMER không tồn tại trong hệ thống")));
    }

    private AuthResponse buildAuthResponse(User user, Map<String, Object> keycloakResponse) {
        List<String> roles = user.getRoles().stream()
            .map(Role::getCode)
            .collect(Collectors.toList());

        AuthResponse response = new AuthResponse();
        response.setAccessToken((String) keycloakResponse.get("access_token"));
        response.setRefreshToken((String) keycloakResponse.get("refresh_token"));
        response.setExpiresIn(((Number) keycloakResponse.get("expires_in")).longValue());

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setFullName(user.getFullName());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setRoles(roles);
        userInfo.setUserType(user.getUserType().name());

        response.setUser(userInfo);
        return response;
    }

    private GoogleUser verifyGoogleIdToken(String idToken) {
        try {
            String url = googleTokenInfoUrl + "?id_token=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            RestTemplate restTemplate = new RestTemplate();
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = restTemplate.getForObject(url, Map.class);

            if (payload == null || payload.get("sub") == null) {
                throw new AuthException("Token Google không hợp lệ");
            }

            String audience = payload.get("aud") != null ? payload.get("aud").toString() : "";
            if (googleClientId != null && !googleClientId.isBlank() && !googleClientId.equals(audience)) {
                throw new AuthException("Token Google không thuộc ứng dụng này");
            }

            Object expObj = payload.get("exp");
            if (expObj != null) {
                long exp = Long.parseLong(expObj.toString());
                if (Instant.ofEpochSecond(exp).isBefore(Instant.now())) {
                    throw new AuthException("Token Google đã hết hạn");
                }
            }

            boolean emailVerified = Boolean.parseBoolean(String.valueOf(payload.getOrDefault("email_verified", "false")));

            return new GoogleUser(
                payload.get("sub").toString(),
                payload.get("email") != null ? payload.get("email").toString() : "",
                payload.getOrDefault("name", "").toString(),
                payload.getOrDefault("picture", "").toString(),
                emailVerified
            );
        } catch (HttpClientErrorException e) {
            log.warn("Không thể xác thực token Google: {}", e.getResponseBodyAsString());
            throw new AuthException("Token Google không hợp lệ");
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification error: {}", e.getMessage(), e);
            throw new AuthException("Token Google không hợp lệ");
        }
    }

    private User createUserFromGoogle(GoogleUser googleUser, String socialPassword) {
        Map<String, Object> keycloakResult = keycloakClient.registerUser(
            googleUser.email(),
            socialPassword,
            googleUser.fullName(),
            googleUser.emailVerified()
        );

        String keycloakUserId = (String) keycloakResult.get("userId");
        if (keycloakUserId == null) {
            throw new AuthException("Không thể tạo tài khoản Google trong Keycloak");
        }

        User user = new User();
        user.setEmail(googleUser.email());
        user.setPasswordHash(passwordEncoder.encode(socialPassword));
        user.setFullName(googleUser.fullName());
        user.setAvatarUrl(googleUser.avatarUrl());
        user.setUserType(UserType.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setKeycloakId(keycloakUserId);
        user.setEmailVerified(googleUser.emailVerified());
        user.setPhoneVerified(false);
        user.setLoyaltyPoints(0);

        Role defaultRole = resolveDefaultRole(UserType.CUSTOMER);
        user.getRoles().add(defaultRole);
        try {
            keycloakClient.assignRoleToUser(keycloakUserId, defaultRole.getCode());
        } catch (Exception e) {
            log.error("Không thể gán role cho user Google {}: {}", keycloakUserId, e.getMessage(), e);
            try {
                keycloakClient.deleteUser(keycloakUserId);
            } catch (Exception deleteException) {
                log.warn("Không thể xóa user Google trong Keycloak sau khi gán role thất bại: {}", deleteException.getMessage());
            }
            throw new AuthException("Không thể gán role cho tài khoản Google");
        }

        SocialLogin socialLogin = new SocialLogin();
        socialLogin.setProvider(SocialProvider.GOOGLE);
        socialLogin.setProviderUserId(googleUser.sub());
        socialLogin.setUser(user);
        user.getSocialLogins().add(socialLogin);

        user = userRepository.save(user);
        userRepository.flush();
        return user;
    }

    private String buildSocialPassword(String providerUserId) {
        String secret = socialPasswordSecret != null ? socialPasswordSecret : "change-me";
        return "SOCIAL-" + providerUserId + "-" + secret;
    }

    private record GoogleUser(String sub, String email, String fullName, String avatarUrl, boolean emailVerified) {}
}

