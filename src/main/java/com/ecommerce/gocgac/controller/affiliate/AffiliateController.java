package com.ecommerce.gocgac.controller.affiliate;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.affiliate.CreateAffiliateLinkRequest;
import com.ecommerce.gocgac.dto.affiliate.RegisterAffiliateRequest;
import com.ecommerce.gocgac.service.affiliate.AffiliateService;
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
 * Controller tiếp thị liên kết phía người dùng/đối tác (M17).
 */
@RestController
@RequestMapping("/api/affiliate")
@RequiredArgsConstructor
@Tag(name = "Affiliate", description = "API tiếp thị liên kết")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class AffiliateController {

    private final AffiliateService affiliateService;
    private final CurrentUserService currentUserService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký làm đối tác")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterAffiliateRequest request) {
        affiliateService.register(currentUserService.getCurrentUserId(), request);
        return ResponseEntity.ok(MessageResponse.ok("Gửi đăng ký thành công, chờ duyệt"));
    }

    @GetMapping("/my-registration")
    @Operation(summary = "Đơn đăng ký của tôi")
    public ResponseEntity<MessageResponse> myRegistration() {
        return ResponseEntity.ok(MessageResponse.ok("Lấy đơn đăng ký thành công",
            affiliateService.getMyRegistration(currentUserService.getCurrentUserId())));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Tổng quan đối tác (click/đơn/doanh thu/hoa hồng)")
    public ResponseEntity<MessageResponse> dashboard() {
        return ResponseEntity.ok(MessageResponse.ok("Lấy dashboard thành công",
            affiliateService.getDashboard(currentUserService.getCurrentUserId())));
    }

    @PostMapping("/links")
    @Operation(summary = "Tạo link tiếp thị cho sản phẩm")
    public ResponseEntity<MessageResponse> createLink(@Valid @RequestBody CreateAffiliateLinkRequest request) {
        return ResponseEntity.ok(MessageResponse.created("Tạo link thành công",
            affiliateService.createLink(currentUserService.getCurrentUserId(), request)));
    }

    @GetMapping("/links")
    @Operation(summary = "Danh sách link của tôi")
    public ResponseEntity<MessageResponse> myLinks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách link thành công",
            affiliateService.getMyLinks(currentUserService.getCurrentUserId(), PageRequest.of(page, size))));
    }

    @GetMapping("/commissions")
    @Operation(summary = "Hoa hồng của tôi")
    public ResponseEntity<MessageResponse> myCommissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy hoa hồng thành công",
            affiliateService.getMyCommissions(currentUserService.getCurrentUserId(), PageRequest.of(page, size))));
    }
}
