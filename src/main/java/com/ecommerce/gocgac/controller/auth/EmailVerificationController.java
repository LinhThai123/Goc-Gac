package com.ecommerce.gocgac.controller.auth;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.auth.ResendVerificationRequest;
import com.ecommerce.gocgac.service.auth.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
}

