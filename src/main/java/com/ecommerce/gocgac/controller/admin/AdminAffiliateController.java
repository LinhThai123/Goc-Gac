package com.ecommerce.gocgac.controller.admin;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.affiliate.ReviewAffiliateRequest;
import com.ecommerce.gocgac.entity.enums.CommissionStatus;
import com.ecommerce.gocgac.service.affiliate.AffiliateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Quản trị tiếp thị liên kết (M17) — duyệt đối tác, đối soát &amp; chi trả hoa hồng.
 * Nằm dưới /api/admin/** → đã giới hạn ADMIN/SUPER_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/affiliate")
@RequiredArgsConstructor
@Tag(name = "Admin Affiliate", description = "API quản trị tiếp thị liên kết")
@SecurityRequirement(name = "bearerAuth")
public class AdminAffiliateController {

    private final AffiliateService affiliateService;
    private final CurrentUserService currentUserService;

    @GetMapping("/registrations")
    @Operation(summary = "Đơn đăng ký đối tác chờ duyệt")
    public ResponseEntity<MessageResponse> pendingRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách thành công",
            affiliateService.getPendingRegistrations(PageRequest.of(page, size))));
    }

    @PostMapping("/registrations/{id}/review")
    @Operation(summary = "Duyệt/từ chối đơn đăng ký đối tác")
    public ResponseEntity<MessageResponse> review(@PathVariable Long id,
            @Valid @RequestBody ReviewAffiliateRequest request) {
        affiliateService.review(id, currentUserService.getCurrentUserId(), request);
        return ResponseEntity.ok(MessageResponse.ok(request.isApproved() ? "Đã duyệt đối tác" : "Đã từ chối đơn"));
    }

    @GetMapping("/commissions")
    @Operation(summary = "Hoa hồng theo trạng thái")
    public ResponseEntity<MessageResponse> commissions(
            @RequestParam(defaultValue = "PENDING") CommissionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy hoa hồng thành công",
            affiliateService.listCommissions(status, PageRequest.of(page, size))));
    }

    @PostMapping("/commissions/{id}/pay")
    @Operation(summary = "Chi trả hoa hồng (APPROVED → PAID)")
    public ResponseEntity<MessageResponse> pay(@PathVariable Long id) {
        return ResponseEntity.ok(MessageResponse.ok("Đã chi trả hoa hồng",
            affiliateService.payCommission(id)));
    }
}
