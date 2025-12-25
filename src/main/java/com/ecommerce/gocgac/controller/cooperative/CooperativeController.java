package com.ecommerce.gocgac.controller.cooperative;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.entity.Cooperative;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.exception.CooperativeException;
import com.ecommerce.gocgac.repository.UserRepository;
import com.ecommerce.gocgac.dto.cooperative.MemberResponse;
import com.ecommerce.gocgac.dto.cooperative.ReviewMemberRequest;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.service.cooperative.CooperativeMemberService;
import com.ecommerce.gocgac.service.cooperative.CooperativeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final CooperativeMemberService memberService;
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
    
    // ========== Member Management (HTX Manager) ==========
    
    /**
     * Lấy danh sách thành viên của HTX (cho HTX Manager)
     */
    @GetMapping("/members")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy danh sách thành viên HTX", 
               description = "Lấy danh sách thành viên của HTX với pagination và filter theo status")
    public ResponseEntity<Page<MemberResponse>> getMembers(
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Long userId = getCurrentUserId();
        Optional<Cooperative> cooperative = cooperativeService.getCooperativeByUserId(userId);
        
        if (cooperative.isEmpty()) {
            throw new CooperativeException("Bạn chưa có HTX");
        }
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<MemberResponse> members = memberService.getMembers(
            cooperative.get().getId(), 
            status, 
            pageable
        );
        
        return ResponseEntity.ok(members);
    }
    
    /**
     * HTX Manager approve đơn đăng ký thành viên
     */
    @PostMapping("/members/{memberId}/approve")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SUPER_ADMIN')")
    @Operation(summary = "Phê duyệt đơn đăng ký thành viên", 
               description = "HTX Manager phê duyệt đơn đăng ký và tạo Store cho Seller")
    public ResponseEntity<MessageResponse> approveMember(
            @PathVariable Long memberId,
            @Valid @RequestBody ReviewMemberRequest request) {
        
        if (request.getStatus() != ApprovalStatus.APPROVED) {
            throw new CooperativeException("Status phải là APPROVED");
        }
        
        Long cooperativeManagerId = getCurrentUserId();
        MessageResponse response = memberService.approveMember(memberId, cooperativeManagerId);
        return ResponseEntity.status(response.getStatus() != null ? 
            HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK).body(response);
    }
    
    /**
     * HTX Manager reject đơn đăng ký thành viên
     */
    @PostMapping("/members/{memberId}/reject")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SUPER_ADMIN')")
    @Operation(summary = "Từ chối đơn đăng ký thành viên", 
               description = "HTX Manager từ chối đơn đăng ký với lý do")
    public ResponseEntity<MessageResponse> rejectMember(
            @PathVariable Long memberId,
            @Valid @RequestBody ReviewMemberRequest request) {
        
        if (request.getStatus() != ApprovalStatus.REJECTED) {
            throw new CooperativeException("Status phải là REJECTED");
        }
        
        if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
            throw new CooperativeException("Lý do từ chối không được để trống");
        }
        
        Long cooperativeManagerId = getCurrentUserId();
        MessageResponse response = memberService.rejectMember(
            memberId, 
            cooperativeManagerId, 
            request.getRejectionReason()
        );
        return ResponseEntity.status(response.getStatus() != null ? 
            HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK).body(response);
    }
    
    // TODO: Thêm endpoint PUT /api/cooperative/update để update cooperative info
    // Cần tạo UpdateCooperativeRequest DTO trước
}

