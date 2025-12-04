package com.ecommerce.gocgac.controller.customer;

import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller cho khách hàng
 * Yêu cầu role CUSTOMER hoặc cao hơn
 */
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'COOPERATIVE_MANAGER', 'SUPER_ADMIN')")
public class CustomerController {
    
    private final UserRepository userRepository;
    
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        String email = JwtUtils.getEmail();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("fullName", user.getFullName());
        profile.put("phone", user.getPhone());
        profile.put("address", user.getAddress());
        profile.put("userType", user.getUserType());
        profile.put("roles", JwtUtils.getRoles());
        
        return ResponseEntity.ok(profile);
    }
}

