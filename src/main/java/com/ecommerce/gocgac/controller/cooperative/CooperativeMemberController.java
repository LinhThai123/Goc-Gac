package com.ecommerce.gocgac.controller.cooperative;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.dto.cooperative.JoinCooperativeRequest;
import com.ecommerce.gocgac.dto.cooperative.MemberResponse;
import com.ecommerce.gocgac.exception.CooperativeException;
import com.ecommerce.gocgac.repository.UserRepository;
import com.ecommerce.gocgac.service.cooperative.CooperativeMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cooperative/member")
@RequiredArgsConstructor
@Tag(name = "Cooperative Member", description = "API đăng ký thành viên HTX")
public class CooperativeMemberController {
    
    private final CooperativeMemberService memberService;
    private final UserRepository userRepository;
    
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
     * User (CUSTOMER) đăng ký thành viên HTX
     */
    @PostMapping("/join")
    @Operation(summary = "Đăng ký thành viên HTX", description = "User (CUSTOMER) đăng ký trở thành thành viên của HTX")
    public ResponseEntity<MessageResponse> joinCooperative(@Valid @RequestBody JoinCooperativeRequest request) {
        Long userId = getCurrentUserId();
        MessageResponse response = memberService.joinCooperative(userId, request);
        return ResponseEntity.status(response.getStatus() != null ? 
            HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK).body(response);
    }
    
    /**
     * Lấy danh sách HTX mà User đã tham gia (APPROVED)
     */
    @GetMapping("/my-cooperatives")
    @Operation(summary = "Lấy danh sách HTX của tôi", description = "Lấy danh sách HTX mà User đã tham gia")
    public ResponseEntity<List<MemberResponse>> getMyCooperatives() {
        Long userId = getCurrentUserId();
        List<MemberResponse> cooperatives = memberService.getMyCooperatives(userId);
        return ResponseEntity.ok(cooperatives);
    }
    
    /**
     * Lấy danh sách đơn đăng ký của User (PENDING)
     */
    @GetMapping("/my-applications")
    @Operation(summary = "Lấy đơn đăng ký của tôi", description = "Lấy danh sách đơn đăng ký thành viên HTX đang chờ duyệt")
    public ResponseEntity<List<MemberResponse>> getMyApplications() {
        Long userId = getCurrentUserId();
        List<MemberResponse> applications = memberService.getMyPendingApplications(userId);
        return ResponseEntity.ok(applications);
    }
}

