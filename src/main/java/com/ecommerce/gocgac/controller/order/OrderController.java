package com.ecommerce.gocgac.controller.order;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.order.CancelOrderRequest;
import com.ecommerce.gocgac.dto.order.CheckoutRequest;
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
 * Controller đơn hàng phía khách hàng (M09).
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "API đơn hàng (khách hàng)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    @PostMapping("/checkout")
    @Operation(summary = "Đặt hàng", description = "Tạo đơn từ giỏ hàng (tách theo gian hàng), giữ tồn kho")
    public ResponseEntity<MessageResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.created("Đặt hàng thành công", orderService.checkout(userId, request)));
    }

    @GetMapping
    @Operation(summary = "Danh sách đơn của tôi", description = "Lấy đơn hàng của tôi, mới nhất trước")
    public ResponseEntity<MessageResponse> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách đơn hàng thành công",
            orderService.getMyOrders(userId, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết đơn", description = "Lấy chi tiết đơn hàng của tôi")
    public ResponseEntity<MessageResponse> getMyOrder(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Lấy chi tiết đơn hàng thành công",
            orderService.getMyOrder(userId, id)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Hủy đơn", description = "Khách hủy đơn (khi đơn chưa giao), hoàn tồn kho")
    public ResponseEntity<MessageResponse> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(MessageResponse.ok("Đã hủy đơn hàng",
            orderService.cancelMyOrder(userId, id, request.getReason())));
    }
}
