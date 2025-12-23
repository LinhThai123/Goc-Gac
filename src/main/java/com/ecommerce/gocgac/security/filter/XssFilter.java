package com.ecommerce.gocgac.security.filter;

import com.ecommerce.gocgac.common.util.XssSanitizer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Filter để sanitize request parameters và body để chống XSS attacks
 */
@Slf4j
@Component
@Order(1)
public class XssFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // Wrap request với XssRequestWrapper để sanitize parameters
        XssRequestWrapper wrappedRequest = new XssRequestWrapper(request);
        
        // Log và detect XSS attempts trong query string và headers
        detectXssAttempts(request);
        
        // Tiếp tục filter chain với wrapped request
        filterChain.doFilter(wrappedRequest, response);
    }
    
    /**
     * Detect và log XSS attempts trong query string và headers
     */
    private void detectXssAttempts(HttpServletRequest request) {
        // Sanitize query string
        String queryString = request.getQueryString();
        if (queryString != null && XssSanitizer.containsXss(queryString)) {
            log.warn("XSS attempt detected in query string: {}", queryString);
        }
        
        // Sanitize headers (một số headers có thể chứa user input)
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();
                if (headerValue != null && XssSanitizer.containsXss(headerValue)) {
                    log.warn("XSS attempt detected in header '{}': {}", headerName, headerValue);
                }
            }
        }
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Bỏ qua các static resources và actuator endpoints
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/webjars") ||
               path.startsWith("/actuator") ||
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

