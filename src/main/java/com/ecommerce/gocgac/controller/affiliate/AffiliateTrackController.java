package com.ecommerce.gocgac.controller.affiliate;

import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.service.affiliate.AffiliateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Endpoint công khai ghi nhận click affiliate rồi redirect tới trang sản phẩm (M17).
 */
@RestController
@RequestMapping("/api/affiliate/track")
@RequiredArgsConstructor
@Tag(name = "Affiliate Track", description = "API ghi nhận click affiliate")
public class AffiliateTrackController {

    private final AffiliateService affiliateService;
    private final CurrentUserService currentUserService;

    @GetMapping("/{code}")
    @Operation(summary = "Ghi nhận click + redirect tới sản phẩm")
    public ResponseEntity<Void> track(@PathVariable String code,
            @RequestParam Long productId,
            HttpServletRequest request) {
        Long userId = currentUserIdOrNull();
        String target = affiliateService.trackClick(code, productId, userId, clientIp(request));
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }

    private Long currentUserIdOrNull() {
        try {
            return currentUserService.getCurrentUserId();
        } catch (RuntimeException e) {
            return null; // khách chưa đăng nhập
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
