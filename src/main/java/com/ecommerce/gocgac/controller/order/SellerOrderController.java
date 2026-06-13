package com.ecommerce.gocgac.controller.order;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.order.UpdateOrderStatusRequest;
import com.ecommerce.gocgac.service.order.OrderService;
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
 * Controller xử lý đơn hàng phía người bán / quản lý HTX (M09).
 */
@RestController
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
@Tag(name = "Seller Order", description = "API xử lý đơn hàng của gian hàng")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('SELLER', 'COOPERATIVE_MANAGER', 'SUPER_ADMIN')")
public class SellerOrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @Operation(summary = "Danh sách đơn của gian hàng", description = "Lấy đơn hàng thuộc gian hàng của tôi")
    public ResponseEntity<MessageResponse> getStoreOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách đơn hàng thành công",
            orderService.getStoreOrders(userId, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết đơn của gian hàng", description = "Lấy chi tiết đơn hàng thuộc gian hàng của tôi")
    public ResponseEntity<MessageResponse> getStoreOrder(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy chi tiết đơn hàng thành công",
            orderService.getStoreOrder(userId, id)));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái đơn",
               description = "Chuyển trạng thái: PENDING→CONFIRMED→PROCESSING→SHIPPING→DELIVERED; hoặc CANCELLED")
    public ResponseEntity<MessageResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Cập nhật trạng thái đơn hàng thành công",
            orderService.updateStatus(userId, id, request.getStatus(), request.getNote())));
    }
}
