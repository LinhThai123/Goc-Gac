package com.ecommerce.gocgac.controller.affiliate;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.affiliate.SetProductCommissionRequest;
import com.ecommerce.gocgac.service.affiliate.ProductCommissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Người bán đặt mức hoa hồng tiếp thị liên kết theo sản phẩm (M17).
 */
@RestController
@RequestMapping("/api/seller/product-commissions")
@RequiredArgsConstructor
@Tag(name = "Product Commission", description = "API hoa hồng theo sản phẩm")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('SELLER', 'COOPERATIVE_MANAGER', 'SUPER_ADMIN')")
public class ProductCommissionController {

    private final ProductCommissionService productCommissionService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @Operation(summary = "Danh sách hoa hồng theo sản phẩm của gian hàng")
    public ResponseEntity<MessageResponse> list() {
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách thành công",
            productCommissionService.list(currentUserService.getCurrentUserId())));
    }

    @PutMapping
    @Operation(summary = "Đặt/cập nhật hoa hồng cho sản phẩm")
    public ResponseEntity<MessageResponse> set(@Valid @RequestBody SetProductCommissionRequest request) {
        return ResponseEntity.ok(MessageResponse.ok("Lưu hoa hồng sản phẩm thành công",
            productCommissionService.setCommission(currentUserService.getCurrentUserId(), request)));
    }
}
