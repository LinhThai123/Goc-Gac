package com.ecommerce.gocgac.controller.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller để debug JWT token và authorities
 * CHỈ DÙNG TRONG DEVELOPMENT
 */
@Slf4j
@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    @GetMapping("/jwt")
    public ResponseEntity<Map<String, Object>> debugJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> debugInfo = new HashMap<>();
        
        if (authentication != null) {
            debugInfo.put("authenticated", authentication.isAuthenticated());
            debugInfo.put("name", authentication.getName());
            debugInfo.put("principalType", authentication.getPrincipal().getClass().getName());
            
            // Lấy authorities
            List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            debugInfo.put("authorities", authorities);
            debugInfo.put("authoritiesCount", authorities.size());
            
            // Lấy JWT token nếu có
            if (authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                debugInfo.put("jwtId", jwt.getId());
                debugInfo.put("jwtSubject", jwt.getSubject());
                debugInfo.put("jwtIssuer", jwt.getIssuer());
                debugInfo.put("jwtAudience", jwt.getAudience());
                debugInfo.put("jwtExpiresAt", jwt.getExpiresAt());
                
                // Lấy tất cả claims
                Map<String, Object> claims = new HashMap<>();
                jwt.getClaims().forEach((key, value) -> {
                    if (!key.equals("exp") && !key.equals("iat") && !key.equals("jti")) {
                        claims.put(key, value);
                    }
                });
                debugInfo.put("jwtClaims", claims);
                
                // Lấy roles từ JWT
                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                if (realmAccess != null) {
                    debugInfo.put("realmAccess", realmAccess);
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) realmAccess.get("roles");
                    debugInfo.put("jwtRoles", roles);
                } else {
                    debugInfo.put("realmAccess", "null");
                    debugInfo.put("jwtRoles", "null");
                }
                
                // Lấy resource_access
                Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
                if (resourceAccess != null) {
                    debugInfo.put("resourceAccess", resourceAccess);
                } else {
                    debugInfo.put("resourceAccess", "null");
                }
            } else {
                debugInfo.put("jwtToken", "Not a JWT token");
            }
        } else {
            debugInfo.put("authenticated", false);
            debugInfo.put("message", "No authentication found");
        }
        
        return ResponseEntity.ok(debugInfo);
    }
}

