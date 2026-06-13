package com.ecommerce.gocgac.controller.voucher;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.voucher.CreateVoucherRequest;
import com.ecommerce.gocgac.dto.voucher.UpdateVoucherRequest;
import com.ecommerce.gocgac.service.voucher.VoucherService;
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
 * Controller Voucher (M12).
 * - Người bán: CRUD voucher của gian hàng.
 * - Khách hàng: xem voucher khả dụng, lưu voucher, xem voucher đã lưu.
 */
@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Tag(name = "Voucher", description = "API voucher / mã giảm giá")
@SecurityRequirement(name = "bearerAuth")
public class VoucherController {

    private final VoucherService voucherService;
    private final CurrentUserService currentUserService;

    private static final String SELLER_ROLES = "hasAnyRole('SELLER', 'COOPERATIVE_MANAGER', 'SUPER_ADMIN')";

    // ===== Seller =====

    @PostMapping
    @PreAuthorize(SELLER_ROLES)
    @Operation(summary = "Tạo voucher", description = "Người bán tạo voucher cho gian hàng")
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateVoucherRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.created("Tạo voucher thành công",
            voucherService.createVoucher(userId, request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SELLER_ROLES)
    @Operation(summary = "Cập nhật voucher")
    public ResponseEntity<MessageResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdateVoucherRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Cập nhật voucher thành công",
            voucherService.updateVoucher(userId, id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SELLER_ROLES)
    @Operation(summary = "Ngừng voucher", description = "Đặt voucher về trạng thái INACTIVE")
    public ResponseEntity<MessageResponse> deactivate(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        voucherService.deactivateVoucher(userId, id);
        return ResponseEntity.ok(MessageResponse.ok("Đã ngừng voucher"));
    }

    @GetMapping("/my-store")
    @PreAuthorize(SELLER_ROLES)
    @Operation(summary = "Voucher của gian hàng tôi")
    public ResponseEntity<MessageResponse> getStoreVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách voucher thành công",
            voucherService.getStoreVouchers(userId, PageRequest.of(page, size))));
    }

    // ===== Customer =====

    @GetMapping("/store/{storeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Voucher khả dụng của gian hàng")
    public ResponseEntity<MessageResponse> getAvailable(@PathVariable Long storeId) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy voucher khả dụng thành công",
            voucherService.getAvailableForStore(storeId)));
    }

    @PostMapping("/{id}/save")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lưu voucher", description = "Khách lưu voucher vào ví của mình")
    public ResponseEntity<MessageResponse> save(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        voucherService.saveVoucher(userId, id);
        return ResponseEntity.ok(MessageResponse.ok("Đã lưu voucher"));
    }

    @GetMapping("/my-saved")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Voucher đã lưu của tôi")
    public ResponseEntity<MessageResponse> getMyVouchers() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy voucher đã lưu thành công",
            voucherService.getMyVouchers(userId)));
    }
}
