package com.ecommerce.gocgac.controller.cooperative;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.dto.cooperative.CooperativeRegistrationRequest;
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
@Tag(name = "Cooperative Registration", description = "API đăng ký HTX")
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
    
    /**
     * Đăng ký HTX với tất cả thông tin trong 1 request (đơn giản hóa)
     * Đăng ký HTX - 1 request duy nhất với đầy đủ thông tin
     */
    @PostMapping("")
    @Operation(summary = "Đăng ký HTX", description = "Đăng ký HTX với tất cả thông tin trong 1 request")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody CooperativeRegistrationRequest request) {
        Long userId = getCurrentUserId();
        MessageResponse response = registrationService.registerCooperative(userId, request);
        return ResponseEntity.status(response.getStatus() != null ? HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK)
                .body(response);
    }
    
    @GetMapping("/my-registration")
    @Operation(summary = "Lấy đơn đăng ký của tôi", description = "Lấy thông tin đơn đăng ký hiện tại (PENDING)")
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
