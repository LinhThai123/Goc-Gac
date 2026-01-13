package com.ecommerce.gocgac.controller.customer;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.dto.user.UpdateProfileRequest;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.repository.UserRepository;
import com.ecommerce.gocgac.service.storage.MinioService;
import com.ecommerce.gocgac.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller cho khách hàng
 * Yêu cầu role CUSTOMER hoặc cao hơn
 */
@Slf4j
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'COOPERATIVE_MANAGER', 'SUPER_ADMIN')")
@Tag(name = "Customer", description = "API quản lý thông tin khách hàng")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {
    
    private final UserRepository userRepository;
    private final UserService userService;
    private final MinioService minioService;
    
    @GetMapping("/profile")
    @Operation(summary = "Lấy thông tin profile", description = "Lấy thông tin profile của user hiện tại")
    public ResponseEntity<MessageResponse> getProfile() {
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
        profile.put("avatarUrl", user.getAvatarUrl());
        profile.put("dateOfBirth", user.getDateOfBirth());
        profile.put("userType", user.getUserType());
        profile.put("loyaltyPoints", user.getLoyaltyPoints());
        profile.put("roles", JwtUtils.getRoles());
        profile.put("authorities", authorities);
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Lấy thông tin profile thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(profile);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/profile")
    @Operation(summary = "Cập nhật thông tin profile", description = "Cập nhật thông tin profile của user hiện tại")
    public ResponseEntity<MessageResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        try {
            String email = JwtUtils.getEmail();
            User updatedUser = userService.updateProfile(email, request);
            
            // Tạo response
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", updatedUser.getId());
            profile.put("email", updatedUser.getEmail());
            profile.put("fullName", updatedUser.getFullName());
            profile.put("phone", updatedUser.getPhone());
            profile.put("address", updatedUser.getAddress());
            profile.put("avatarUrl", updatedUser.getAvatarUrl());
            profile.put("dateOfBirth", updatedUser.getDateOfBirth());
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Cập nhật profile thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(profile);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("❌ Error updating profile: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload avatar", description = "Upload và cập nhật avatar cho user hiện tại")
    public ResponseEntity<MessageResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                MessageResponse response = new MessageResponse();
                response.setMessage("File không được để trống");
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate file type (chỉ cho phép image)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                MessageResponse response = new MessageResponse();
                response.setMessage("Chỉ chấp nhận file ảnh");
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate file size (max 5MB cho avatar)
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                MessageResponse response = new MessageResponse();
                response.setMessage("File vượt quá kích thước cho phép (5MB)");
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Upload file lên MINIO
            String email = JwtUtils.getEmail();
            String avatarUrl = minioService.uploadFile(file, "users");
            
            // Cập nhật avatar URL vào database
            User updatedUser = userService.updateAvatar(email, avatarUrl);
            
            // Tạo response
            Map<String, Object> data = new HashMap<>();
            data.put("avatarUrl", updatedUser.getAvatarUrl());
            data.put("fileName", file.getOriginalFilename());
            data.put("fileSize", file.getSize());
            data.put("contentType", file.getContentType());
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Upload avatar thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(data);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("❌ Error uploading avatar: {}", e.getMessage(), e);
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi upload avatar: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            log.error("❌ Unexpected error uploading avatar: {}", e.getMessage(), e);
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi không mong muốn: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/profile/avatar")
    @Operation(summary = "Xóa avatar", description = "Xóa avatar của user hiện tại")
    public ResponseEntity<MessageResponse> deleteAvatar() {
        try {
            String email = JwtUtils.getEmail();
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Xóa file từ MINIO nếu có
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                try {
                    minioService.deleteFile(user.getAvatarUrl());
                } catch (Exception e) {
                    log.warn("⚠️ Could not delete avatar file from MINIO: {}", e.getMessage());
                }
            }
            
            // Xóa avatar URL trong database
            user.setAvatarUrl(null);
            userRepository.save(user);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Xóa avatar thành công");
            response.setStatus(HttpStatus.OK.value());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error deleting avatar: {}", e.getMessage(), e);
            MessageResponse response = new MessageResponse();
            response.setMessage("Lỗi khi xóa avatar: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

