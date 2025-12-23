package com.ecommerce.gocgac.controller.cooperative;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.dto.cooperative.*;
import com.ecommerce.gocgac.entity.CooperativeRegistration;
import com.ecommerce.gocgac.exception.CooperativeException;
import com.ecommerce.gocgac.repository.UserRepository;
import com.ecommerce.gocgac.service.cooperative.CooperativeRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/cooperative/registration")
@RequiredArgsConstructor
@Tag(name = "Cooperative Registration", description = "API đăng ký HTX (6 bước)")
public class CooperativeRegistrationController {
    
    private final CooperativeRegistrationService registrationService;
    private final UserRepository userRepository;
    
    /**
     * Lấy userId từ JWT token
     */
    private Long getCurrentUserId() {
        String email = JwtUtils.getEmail();
        if (email == null) {
            throw new CooperativeException("Không thể xác định user từ token");
        }
        return userRepository.findByEmail(email)
            .map(user -> user.getId())
            .orElseThrow(() -> new CooperativeException("User không tồn tại"));
    }
    
    @PostMapping("/step1")
    @Operation(summary = "Bước 1: Thông tin HTX cơ bản", description = "Tạo hoặc cập nhật thông tin HTX cơ bản")
    public ResponseEntity<MessageResponse> step1(@Valid @RequestBody Step1Request request) {
        Long userId = getCurrentUserId();
        MessageResponse response = registrationService.createOrUpdateStep1(userId, request);
        return ResponseEntity.status(response.getStatus() != null ? HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK)
                .body(response);
    }
    
    @PostMapping("/step2")
    @Operation(summary = "Bước 2: Thông tin liên hệ", description = "Cập nhật thông tin liên hệ")
    public ResponseEntity<MessageResponse> step2(@Valid @RequestBody Step2Request request) {
        Long userId = getCurrentUserId();
        MessageResponse response = registrationService.updateStep2(userId, request);
        return ResponseEntity.status(response.getStatus() != null ? HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK)
                .body(response);
    }
    
    @PostMapping("/step3")
    @Operation(summary = "Bước 3: Địa chỉ kinh doanh", description = "Cập nhật địa chỉ kinh doanh")
    public ResponseEntity<MessageResponse> step3(@Valid @RequestBody Step3Request request) {
        Long userId = getCurrentUserId();
        MessageResponse response = registrationService.updateStep3(userId, request);
        return ResponseEntity.status(response.getStatus() != null ? HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK)
                .body(response);
    }
    
    @PostMapping("/step4")
    @Operation(summary = "Bước 4: Thông tin người đại diện", description = "Cập nhật thông tin người đại diện")
    public ResponseEntity<MessageResponse> step4(@Valid @RequestBody Step4Request request) {
        Long userId = getCurrentUserId();
        MessageResponse response = registrationService.updateStep4(userId, request);
        return ResponseEntity.status(response.getStatus() != null ? HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK)
                .body(response);
    }
    
    @PostMapping("/step5")
    @Operation(summary = "Bước 5: Thông tin pháp lý", description = "Cập nhật thông tin pháp lý")
    public ResponseEntity<MessageResponse> step5(@Valid @RequestBody Step5Request request) {
        Long userId = getCurrentUserId();
        MessageResponse response = registrationService.updateStep5(userId, request);
        return ResponseEntity.status(response.getStatus() != null ? HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK)
                .body(response);
    }
    
    @PostMapping("/step6")
    @Operation(summary = "Bước 6: Thông tin kinh doanh", description = "Cập nhật thông tin kinh doanh")
    public ResponseEntity<MessageResponse> step6(@Valid @RequestBody Step6Request request) {
        Long userId = getCurrentUserId();
        MessageResponse response = registrationService.updateStep6(userId, request);
        return ResponseEntity.status(response.getStatus() != null ? HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK)
                .body(response);
    }
    
    @PostMapping("/submit")
    @Operation(summary = "Submit đơn đăng ký", description = "Gửi đơn đăng ký để admin phê duyệt")
    public ResponseEntity<MessageResponse> submit(@Valid @RequestBody SubmitRegistrationRequest request) {
        Long userId = getCurrentUserId();
        MessageResponse response = registrationService.submitRegistration(userId, request.getRegistrationId());
        return ResponseEntity.status(response.getStatus() != null ? HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK)
                .body(response);
    }
    
    @GetMapping("/my-registration")
    @Operation(summary = "Lấy đơn đăng ký của tôi", description = "Lấy thông tin đơn đăng ký hiện tại (DRAFT hoặc PENDING)")
    public ResponseEntity<CooperativeRegistration> getMyRegistration() {
        Long userId = getCurrentUserId();
        Optional<CooperativeRegistration> registration = registrationService.getMyRegistration(userId);
        
        if (registration.isPresent()) {
            return ResponseEntity.ok(registration.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{registrationId}")
    @Operation(summary = "Lấy chi tiết đơn đăng ký", description = "Lấy thông tin chi tiết đơn đăng ký theo ID")
    public ResponseEntity<CooperativeRegistration> getRegistrationDetail(@PathVariable Long registrationId) {
        Long userId = getCurrentUserId();
        CooperativeRegistration registration = registrationService.getRegistrationDetail(userId, registrationId);
        return ResponseEntity.ok(registration);
    }
}

