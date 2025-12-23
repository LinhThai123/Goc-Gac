package com.ecommerce.gocgac.controller.auth;

import com.ecommerce.gocgac.common.response.AuthResponse;
import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.auth.ChangePasswordRequest;
import com.ecommerce.gocgac.dto.auth.GoogleLoginRequest;
import com.ecommerce.gocgac.dto.auth.LoginRequest;
import com.ecommerce.gocgac.dto.auth.RefreshTokenRequest;
import com.ecommerce.gocgac.dto.auth.RegisterRequest;
import com.ecommerce.gocgac.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API đăng nhập, đăng ký và quản lý token")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Đăng nhập bằng email và mật khẩu")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/google")
    @Operation(summary = "Đăng nhập với Google", description = "Nhận Google ID token và trả về token hệ thống")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    @Operation(summary = "Đăng ký", description = "Đăng ký tài khoản mới")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        MessageResponse response = authService.register(request);
        HttpStatus httpStatus = response.getStatus() != null ? 
            HttpStatus.valueOf(response.getStatus()) : HttpStatus.CREATED;
        return ResponseEntity.status(httpStatus).body(response);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Làm mới access token bằng refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu sử dụng email, mật khẩu hiện tại và mật khẩu mới")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        MessageResponse response = authService.changePassword(request);
        HttpStatus httpStatus = response.getStatus() != null ?
            HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK;
        return ResponseEntity.status(httpStatus).body(response);
    }
}

