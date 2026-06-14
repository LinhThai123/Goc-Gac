package com.ecommerce.gocgac.controller.ad;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.ad.CreateAdCampaignRequest;
import com.ecommerce.gocgac.dto.ad.UpdateAdCampaignRequest;
import com.ecommerce.gocgac.service.ad.AdCampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Controller chiến dịch quảng cáo (M18).
 * - Người bán: tạo/sửa/gửi duyệt/liệt kê.
 * - Công khai: lấy quảng cáo theo vị trí, ghi click + redirect.
 */
@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@Tag(name = "Ad Campaign", description = "API quảng cáo")
public class AdCampaignController {

    private final AdCampaignService adCampaignService;
    private final CurrentUserService currentUserService;

    private static final String SELLER_ROLES = "hasAnyRole('SELLER', 'COOPERATIVE_MANAGER', 'SUPER_ADMIN')";

    // ===== Public =====

    @GetMapping("/public")
    @Operation(summary = "Quảng cáo đang hiển thị theo vị trí")
    public ResponseEntity<MessageResponse> serving(@RequestParam String position) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy quảng cáo thành công",
            adCampaignService.getServableAds(position)));
    }

    @GetMapping("/track/{id}")
    @Operation(summary = "Ghi nhận click + redirect", description = "Trừ ngân sách theo CPC")
    public ResponseEntity<Void> track(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserIdOrNull();
        String target = adCampaignService.trackClick(id, userId, clientIp(request), request.getHeader("User-Agent"));
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }

    // ===== Seller =====

    @PostMapping
    @PreAuthorize(SELLER_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo chiến dịch")
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateAdCampaignRequest request) {
        return ResponseEntity.ok(MessageResponse.created("Tạo chiến dịch thành công",
            adCampaignService.create(currentUserService.getCurrentUserId(), request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SELLER_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật chiến dịch (khi nháp/bị từ chối)")
    public ResponseEntity<MessageResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdateAdCampaignRequest request) {
        return ResponseEntity.ok(MessageResponse.ok("Cập nhật chiến dịch thành công",
            adCampaignService.update(currentUserService.getCurrentUserId(), id, request)));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize(SELLER_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Gửi duyệt chiến dịch")
    public ResponseEntity<MessageResponse> submit(@PathVariable Long id) {
        return ResponseEntity.ok(MessageResponse.ok("Đã gửi duyệt",
            adCampaignService.submit(currentUserService.getCurrentUserId(), id)));
    }

    @GetMapping("/my")
    @PreAuthorize(SELLER_ROLES)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Chiến dịch của gian hàng tôi")
    public ResponseEntity<MessageResponse> myCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách thành công",
            adCampaignService.getMyCampaigns(currentUserService.getCurrentUserId(), PageRequest.of(page, size))));
    }

    // ===== Helpers =====

    private Long currentUserIdOrNull() {
        try {
            return currentUserService.getCurrentUserId();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
