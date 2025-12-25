package com.ecommerce.gocgac.controller.admin;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.dto.admin.ReviewCooperativeRequest;
import com.ecommerce.gocgac.entity.CooperativeRegistration;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.exception.CooperativeException;
import com.ecommerce.gocgac.repository.UserRepository;
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

/**
 * Controller cho admin quản lý đăng ký HTX
 * Yêu cầu role SUPER_ADMIN hoặc ADMIN
 */
@RestController
@RequestMapping("/api/admin/cooperative")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
@Tag(name = "Admin Cooperative Management", description = "API quản lý đăng ký HTX (Admin)")
public class AdminCooperativeController {
    
    private final CooperativeService cooperativeService;
    private final UserRepository userRepository;
    
    /**
     * Lấy userId từ JWT token
     */
    private Long getCurrentUserId() {
        String email = JwtUtils.getEmail();
        if (email == null) {
            throw new CooperativeException("Không thể xác định admin từ token");
        }
        return userRepository.findByEmail(email)
            .map(User::getId)
            .orElseThrow(() -> new CooperativeException("Admin không tồn tại"));
    }
    
    @GetMapping("/registrations")
    @Operation(summary = "Lấy danh sách đơn đăng ký HTX", 
               description = "Lấy danh sách đơn đăng ký với pagination và filter theo status")
    public ResponseEntity<Page<CooperativeRegistration>> getRegistrations(
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CooperativeRegistration> registrations = cooperativeService.getRegistrations(status, pageable);
        return ResponseEntity.ok(registrations);
    }
    
    @GetMapping("/registrations/{registrationId}")
    @Operation(summary = "Lấy chi tiết đơn đăng ký HTX", 
               description = "Lấy thông tin chi tiết đơn đăng ký theo ID")
    public ResponseEntity<CooperativeRegistration> getRegistrationDetail(
            @PathVariable Long registrationId) {
        CooperativeRegistration registration = cooperativeService.getRegistrationById(registrationId);
        return ResponseEntity.ok(registration);
    }
    
    @PostMapping("/registrations/{registrationId}/approve")
    @Operation(summary = "Phê duyệt đơn đăng ký HTX", 
               description = "Phê duyệt đơn đăng ký và tạo Cooperative entity")
    public ResponseEntity<MessageResponse> approveRegistration(
            @PathVariable Long registrationId,
            @Valid @RequestBody ReviewCooperativeRequest request) {
        
        if (request.getStatus() != ApprovalStatus.APPROVED) {
            throw new CooperativeException("Trạng thái phải là đã phê duyệt");
        }
        
        Long adminId = getCurrentUserId();
        MessageResponse response = cooperativeService.approveRegistration(registrationId, adminId);
        return ResponseEntity.status(response.getStatus() != null ? 
            HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK).body(response);
    }
    
    @PostMapping("/registrations/{registrationId}/reject")
    @Operation(summary = "Từ chối đơn đăng ký HTX", 
               description = "Từ chối đơn đăng ký với lý do")
    public ResponseEntity<MessageResponse> rejectRegistration(
            @PathVariable Long registrationId,
            @Valid @RequestBody ReviewCooperativeRequest request) {
        
        if (request.getStatus() != ApprovalStatus.REJECTED) {
            throw new CooperativeException("Trạng thái phải là đã từ chối");
        }
        
        if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
            throw new CooperativeException("Lý do từ chối không được để trống");
        }
        
        Long adminId = getCurrentUserId();
        MessageResponse response = cooperativeService.rejectRegistration(
            registrationId, 
            adminId, 
            request.getRejectionReason()
        );
        return ResponseEntity.status(response.getStatus() != null ? 
            HttpStatus.valueOf(response.getStatus()) : HttpStatus.OK).body(response);
    }
}

