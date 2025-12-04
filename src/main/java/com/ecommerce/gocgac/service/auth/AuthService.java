package com.ecommerce.gocgac.service.auth;

import com.ecommerce.gocgac.common.response.AuthResponse;
import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.auth.LoginRequest;
import com.ecommerce.gocgac.dto.auth.RegisterRequest;
import com.ecommerce.gocgac.dto.user.UserDTO;
import org.springframework.http.HttpStatus;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.Role;
import com.ecommerce.gocgac.entity.enums.UserStatus;
import com.ecommerce.gocgac.exception.AuthException;
import com.ecommerce.gocgac.external.KeycloakClient;
import com.ecommerce.gocgac.repository.UserRepository;
import com.ecommerce.gocgac.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final KeycloakClient keycloakClient;
    private final PasswordEncoder passwordEncoder;
    
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
            
            // Lấy roles của user
            List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
            
            // Tạo response
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
            Role defaultRole = roleRepository.findByCode(request.getUserType().name())
                .orElseGet(() -> {
                    // Nếu role chưa tồn tại, sử dụng role CUSTOMER
                    return roleRepository.findByCode("CUSTOMER")
                        .orElseThrow(() -> new AuthException("Role CUSTOMER không tồn tại trong hệ thống"));
                });
            user.getRoles().add(defaultRole);
            // Gán role trong Keycloak (không bắt buộc, có thể làm sau)
            try {
                keycloakClient.assignRoleToUser(keycloakUserId, defaultRole.getCode());
            } catch (Exception e) {
                log.warn("Không thể gán role trong Keycloak: {}", e.getMessage());
            }
            
            user = userRepository.save(user);
            
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
            response.setMessage("Đăng ký thành công");
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
}

