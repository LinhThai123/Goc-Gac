package com.ecommerce.gocgac.controller.customer;

import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        
        // Debug: Lấy authorities từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("fullName", user.getFullName());
        profile.put("phone", user.getPhone());
        profile.put("address", user.getAddress());
        profile.put("userType", user.getUserType());
        profile.put("roles", JwtUtils.getRoles());
        profile.put("authorities", authorities); // Debug: authorities từ Spring Security
        profile.put("authenticationName", authentication.getName()); // Debug
        
        return ResponseEntity.ok(profile);
    }
}

