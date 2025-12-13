package com.ecommerce.gocgac.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

/**
 * Utility class for working with JWT tokens from Keycloak
 */
public class JwtUtils {
    
    /**
     * Lấy JWT token từ SecurityContext
     */
    public static Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            return (Jwt) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * Lấy user ID từ JWT token
     */
    public static String getUserId() {
        Jwt jwt = getCurrentJwt();
        if (jwt != null) {
            return jwt.getSubject();
        }
        return null;
    }
    
    /**
     * Lấy email từ JWT token
     */
    public static String getEmail() {
        Jwt jwt = getCurrentJwt();
        if (jwt != null) {
            return jwt.getClaimAsString("email");
        }
        return null;
    }
    
    /**
     * Lấy roles từ JWT token (cả realm roles và client roles từ gocgac_app)
     */
    @SuppressWarnings("unchecked")
    public static List<String> getRoles() {
        Jwt jwt = getCurrentJwt();
        if (jwt != null) {
            List<String> allRoles = new java.util.ArrayList<>();
            
            // Lấy realm roles
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null) {
                List<String> realmRoles = (List<String>) realmAccess.get("roles");
                if (realmRoles != null) {
                    allRoles.addAll(realmRoles);
                }
            }
            
            // Lấy client roles từ gocgac_app
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("gocgac_app");
                if (clientAccess != null) {
                    List<String> clientRoles = (List<String>) clientAccess.get("roles");
                    if (clientRoles != null) {
                        allRoles.addAll(clientRoles);
                    }
                }
            }
            
            return allRoles;
        }
        return List.of();
    }
    
    /**
     * Kiểm tra user có role cụ thể không
     */
    public static boolean hasRole(String role) {
        List<String> roles = getRoles();
        return roles.contains(role);
    }
    
    /**
     * Lấy full name từ JWT token
     */
    public static String getFullName() {
        Jwt jwt = getCurrentJwt();
        if (jwt != null) {
            String firstName = jwt.getClaimAsString("given_name");
            String lastName = jwt.getClaimAsString("family_name");
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            return jwt.getClaimAsString("name");
        }
        return null;
    }
}

