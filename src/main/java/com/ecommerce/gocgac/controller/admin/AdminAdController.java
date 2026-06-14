package com.ecommerce.gocgac.controller.admin;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.ad.ReviewAdRequest;
import com.ecommerce.gocgac.service.ad.AdCampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Kiểm duyệt quảng cáo (M18) — admin. Nằm dưới /api/admin/** đã giới hạn ADMIN/SUPER_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/ads")
@RequiredArgsConstructor
@Tag(name = "Admin Ad", description = "API kiểm duyệt quảng cáo")
@SecurityRequirement(name = "bearerAuth")
public class AdminAdController {

    private final AdCampaignService adCampaignService;
    private final CurrentUserService currentUserService;

    @GetMapping("/pending")
    @Operation(summary = "Chiến dịch chờ duyệt")
    public ResponseEntity<MessageResponse> pending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách thành công",
            adCampaignService.getPending(PageRequest.of(page, size))));
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Duyệt/từ chối chiến dịch")
    public ResponseEntity<MessageResponse> review(@PathVariable Long id,
            @Valid @RequestBody ReviewAdRequest request) {
        return ResponseEntity.ok(MessageResponse.ok(request.isApproved() ? "Đã duyệt quảng cáo" : "Đã từ chối quảng cáo",
            adCampaignService.review(id, currentUserService.getCurrentUserId(), request)));
    }
}
