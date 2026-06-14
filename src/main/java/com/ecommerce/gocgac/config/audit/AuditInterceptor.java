package com.ecommerce.gocgac.config.audit;

import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.repository.UserRepository;
import com.ecommerce.gocgac.service.system.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Ghi nhật ký audit cho các request thay đổi dữ liệu (POST/PUT/DELETE/PATCH).
 * Chạy ở afterCompletion, best-effort — không bao giờ làm hỏng request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditInterceptor implements HandlerInterceptor {

    private final SystemLogService systemLogService;
    private final UserRepository userRepository;

    private static final Set<String> MUTATING = Set.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            if (!MUTATING.contains(request.getMethod())) {
                return;
            }
            String uri = request.getRequestURI();
            Long userId = resolveUserId();
            String module = extractModule(uri);
            String details = request.getMethod() + " " + uri + " -> " + response.getStatus();

            systemLogService.record(userId, request.getMethod(), module, null, null,
                clientIp(request), request.getHeader("User-Agent"), details);
        } catch (Exception e) {
            log.debug("Audit logging skipped: {}", e.getMessage());
        }
    }

    private Long resolveUserId() {
        try {
            String email = JwtUtils.getEmail();
            if (!StringUtils.hasText(email)) return null;
            return userRepository.findByEmail(email).map(u -> u.getId()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractModule(String uri) {
        // /api/<module>/... -> <module>
        String[] parts = uri.split("/");
        if (parts.length >= 3 && "api".equals(parts[1])) {
            return parts[2];
        }
        return parts.length >= 2 ? parts[1] : "unknown";
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
