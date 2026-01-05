package com.ecommerce.gocgac.controller.auth;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.auth.ResendVerificationRequest;
import com.ecommerce.gocgac.service.auth.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/email-verification")
@RequiredArgsConstructor
@Tag(name = "Email Verification", description = "API xác thực email sử dụng Keycloak")
public class EmailVerificationController {
    
    private final EmailVerificationService emailVerificationService;
    
    @PostMapping("/send")
    @Operation(summary = "Gửi email xác thực", description = "Gửi email xác thực từ Keycloak")
    public ResponseEntity<MessageResponse> sendVerificationEmail(
            @Valid @RequestBody ResendVerificationRequest request) {
        emailVerificationService.sendVerificationEmail(request.getEmail());
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Đã gửi email xác thực. Vui lòng kiểm tra hộp thư của bạn.");
        response.setStatus(HttpStatus.OK.value());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/resend")
    @Operation(summary = "Gửi lại email xác thực", description = "Gửi lại email xác thực từ Keycloak")
    public ResponseEntity<MessageResponse> resendVerificationEmail(
            @Valid @RequestBody ResendVerificationRequest request) {
        emailVerificationService.resendVerificationEmail(request.getEmail());
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Đã gửi lại email xác thực. Vui lòng kiểm tra hộp thư của bạn.");
        response.setStatus(HttpStatus.OK.value());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    @Operation(summary = "Kiểm tra trạng thái xác thực email", 
               description = "Kiểm tra email đã được xác thực chưa và đồng bộ với Keycloak")
    public ResponseEntity<MessageResponse> checkEmailVerifiedStatus(@RequestParam String email) {
        boolean isVerified = emailVerificationService.checkEmailVerifiedStatus(email);
        
        MessageResponse response = new MessageResponse();
        response.setMessage(isVerified ? "Email đã được xác thực" : "Email chưa được xác thực");
        response.setStatus(HttpStatus.OK.value());
        response.setData(Map.of("emailVerified", isVerified));
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify")
    @Operation(summary = "Xác thực email thủ công", 
               description = "Xác thực email thủ công (thường Keycloak tự động verify khi user click link)")
    public ResponseEntity<MessageResponse> verifyEmailManually(
            @Valid @RequestBody ResendVerificationRequest request) {
        emailVerificationService.verifyEmailManually(request.getEmail());
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Email đã được xác thực thành công");
        response.setStatus(HttpStatus.OK.value());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/callback")
    @Operation(summary = "Callback từ Keycloak sau khi verify email", 
               description = "Endpoint này được gọi khi user click link verification từ Keycloak. " +
                           "Keycloak sẽ redirect về endpoint này sau khi verify email thành công. " +
                           "Endpoint này sẽ tự động đồng bộ email_verified từ false sang true cho tất cả users đã verify trong Keycloak nhưng chưa verify trong database.")
    public ResponseEntity<?> handleKeycloakCallback(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String client_id) {
        
        try {
            log.info("📧 Callback từ Keycloak sau khi verify email. Đang đồng bộ tất cả users chưa verified...");
            // Giải pháp: Sync tất cả users chưa verified (chỉ update từ false sang true)
            // Vì khi callback được gọi nghĩa là có user vừa verify, nên sync tất cả sẽ sync user đó
            emailVerificationService.syncAllUnverifiedUsers();
            
            log.info("✅ Đã đồng bộ email verified status cho tất cả users chưa verified");
            // Redirect về frontend với thông báo thành công
            return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", emailVerificationService.getFrontendUrl() + 
                    "/login")
                .build();
                
        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý callback từ Keycloak: {}", e.getMessage(), e);
            // Nếu có lỗi, vẫn redirect về frontend nhưng với thông báo lỗi
            try {
                return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", emailVerificationService.getFrontendUrl() + 
                        "/email-verified?success=false&error=" + 
                        java.net.URLEncoder.encode(e.getMessage(), "UTF-8"))
                    .build();
            } catch (java.io.UnsupportedEncodingException ex) {
                return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", emailVerificationService.getFrontendUrl() + 
                        "/email-verified?success=false")
                    .build();
            }
        }
    }
}

