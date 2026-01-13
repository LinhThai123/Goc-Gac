package com.ecommerce.gocgac.service.user;

import com.ecommerce.gocgac.dto.user.UpdateProfileRequest;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Cập nhật thông tin profile của user
     */
    @Transactional
    public User updateProfile(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);
        
        // Chỉ cập nhật các field được cung cấp (không null)
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getPhone() != null) {
            // Kiểm tra phone đã được sử dụng bởi user khác chưa
            if (!request.getPhone().equals(user.getPhone())) {
                if (userRepository.existsByPhone(request.getPhone())) {
                    throw new RuntimeException("Số điện thoại đã được sử dụng");
                }
            }
            user.setPhone(request.getPhone());
        }
        
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("✅ Updated profile for user: {}", email);
        return updatedUser;
    }
    
    /**
     * Cập nhật avatar URL cho user
     */
    @Transactional
    public User updateAvatar(String email, String avatarUrl) {
        User user = findByEmail(email);
        
        // Xóa avatar cũ nếu có (có thể implement logic xóa file từ MINIO)
        String oldAvatarUrl = user.getAvatarUrl();
        
        user.setAvatarUrl(avatarUrl);
        User updatedUser = userRepository.save(user);
        
        log.info("✅ Updated avatar for user: {} from {} to {}", email, oldAvatarUrl, avatarUrl);
        return updatedUser;
    }
}

