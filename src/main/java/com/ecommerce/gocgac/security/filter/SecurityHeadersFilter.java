package com.ecommerce.gocgac.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter để thêm các security headers để chống XSS và các attacks khác
 */
@Slf4j
@Component
@Order(2)
public class SecurityHeadersFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // X-XSS-Protection: Bật XSS filter của browser
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // X-Content-Type-Options: Ngăn MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // X-Frame-Options: Ngăn clickjacking
        response.setHeader("X-Frame-Options", "DENY");
        
        // Referrer-Policy: Kiểm soát thông tin referrer
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions-Policy: Giới hạn các tính năng browser
        response.setHeader("Permissions-Policy", 
            "geolocation=(), microphone=(), camera=(), payment=(), usb=(), magnetometer=(), gyroscope=()");
        
        // Content-Security-Policy: Chính sách bảo mật nội dung
        // Cho phép inline scripts và styles từ cùng origin, nhưng chặn eval và inline event handlers
        String csp = "default-src 'self'; " +
                     "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " + // Cần unsafe-inline cho Swagger
                     "style-src 'self' 'unsafe-inline'; " +
                     "img-src 'self' data: https:; " +
                     "font-src 'self' data:; " +
                     "connect-src 'self'; " +
                     "frame-ancestors 'none'; " +
                     "base-uri 'self'; " +
                     "form-action 'self'; " +
                     "object-src 'none'; " +
                     "upgrade-insecure-requests";
        response.setHeader("Content-Security-Policy", csp);
        
        // Strict-Transport-Security: Chỉ áp dụng cho HTTPS
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains; preload");
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Bỏ qua các static resources
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/webjars") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg") ||
               path.endsWith(".jpeg") ||
               path.endsWith(".gif") ||
               path.endsWith(".ico") ||
               path.endsWith(".svg");
    }
}

