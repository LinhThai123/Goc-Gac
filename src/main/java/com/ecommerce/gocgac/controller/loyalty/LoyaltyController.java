package com.ecommerce.gocgac.controller.loyalty;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.loyalty.UpsertLoyaltyConfigRequest;
import com.ecommerce.gocgac.service.loyalty.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Khách hàng thân thiết (M13).
 * - Khách: xem điểm/hạng và lịch sử giao dịch điểm.
 * - Admin: cấu hình quy tắc tích/đổi điểm.
 */
@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
@Tag(name = "Loyalty", description = "API khách hàng thân thiết (điểm thưởng)")
@SecurityRequirement(name = "bearerAuth")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Điểm & hạng của tôi")
    public ResponseEntity<MessageResponse> myInfo() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy thông tin điểm thưởng thành công",
            loyaltyService.getMyInfo(userId)));
    }

    @GetMapping("/transactions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lịch sử điểm thưởng của tôi")
    public ResponseEntity<MessageResponse> myTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy lịch sử điểm thành công",
            loyaltyService.getMyTransactions(userId, PageRequest.of(page, size))));
    }

    // ===== Admin =====

    @GetMapping("/config")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Danh sách cấu hình điểm thưởng")
    public ResponseEntity<MessageResponse> listConfig() {
        return ResponseEntity.ok(MessageResponse.ok("Lấy cấu hình thành công", loyaltyService.listConfigs()));
    }

    @PutMapping("/config")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Tạo/cập nhật cấu hình điểm thưởng",
               description = "Ví dụ EARN_RATE (VND/điểm khi tích), REDEEM_RATE (VND/điểm khi đổi)")
    public ResponseEntity<MessageResponse> upsertConfig(@Valid @RequestBody UpsertLoyaltyConfigRequest request) {
        return ResponseEntity.ok(MessageResponse.ok("Lưu cấu hình thành công",
            loyaltyService.upsertConfig(request)));
    }
}
