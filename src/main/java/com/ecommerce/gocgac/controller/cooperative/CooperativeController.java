package com.ecommerce.gocgac.controller.cooperative;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.entity.Cooperative;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.exception.CooperativeException;
import com.ecommerce.gocgac.repository.UserRepository;
import com.ecommerce.gocgac.service.cooperative.CooperativeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller cho Cooperative (HTX)
 * - Public endpoints: Xem thông tin cooperative
 * - Protected endpoints: Quản lý cooperative của mình
 */
@RestController
@RequestMapping("/api/cooperative")
@RequiredArgsConstructor
@Tag(name = "Cooperative", description = "API quản lý và xem thông tin HTX")
public class CooperativeController {
    
    private final CooperativeService cooperativeService;
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
            .map(User::getId)
            .orElseThrow(() -> new CooperativeException("User không tồn tại"));
    }
    
    @GetMapping("/my-cooperative")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy thông tin HTX của tôi", 
               description = "Lấy thông tin HTX mà user đang quản lý (yêu cầu COOPERATIVE_MANAGER)")
    public ResponseEntity<MessageResponse> getMyCooperative() {
        Long userId = getCurrentUserId();
        Optional<Cooperative> cooperative = cooperativeService.getCooperativeByUserId(userId);
        
        MessageResponse response = new MessageResponse();
        if (cooperative.isPresent()) {
            response.setMessage("Lấy thông tin HTX thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(cooperative.get());
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("Bạn chưa có HTX");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @GetMapping("/{slug}")
    @Operation(summary = "Lấy thông tin HTX theo slug", 
               description = "Lấy thông tin HTX công khai theo slug (public endpoint)")
    public ResponseEntity<MessageResponse> getCooperativeBySlug(@PathVariable String slug) {
        Optional<Cooperative> cooperative = cooperativeService.getCooperativeBySlug(slug);
        
        MessageResponse response = new MessageResponse();
        if (cooperative.isPresent()) {
            response.setMessage("Lấy thông tin HTX thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(cooperative.get());
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("Không tìm thấy HTX với slug: " + slug);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    // TODO: Thêm endpoint PUT /api/cooperative/update để update cooperative info
    // Cần tạo UpdateCooperativeRequest DTO trước
}

