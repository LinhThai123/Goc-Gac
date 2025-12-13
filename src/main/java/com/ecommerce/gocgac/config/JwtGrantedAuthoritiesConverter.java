package com.ecommerce.gocgac.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converter để map roles từ Keycloak JWT token sang Spring Security authorities
 * Keycloak trả về roles trong "realm_access.roles"
 */
@Slf4j
public class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        log.debug("Converting JWT to authorities. JWT claims: {}", jwt.getClaims().keySet());
        
        // Lấy roles từ realm_access (realm roles) - Keycloak format
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null && !roles.isEmpty()) {
                log.debug("Found realm roles: {}", roles);
                authorities.addAll(roles.stream()
                    .map(role -> {
                        String authority = "ROLE_" + role;
                        log.debug("Adding authority: {}", authority);
                        return new SimpleGrantedAuthority(authority);
                    })
                    .collect(Collectors.toList()));
            } else {
                log.warn("realm_access.roles is null or empty");
            }
        } else {
            log.warn("realm_access claim is null in JWT token");
        }
        
        // Lấy roles từ resource_access (client roles) - từ client gocgac_app
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            // Lấy roles từ client gocgac_app
            @SuppressWarnings("unchecked")
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("gocgac_app");
            if (clientAccess != null) {
                @SuppressWarnings("unchecked")
                List<String> clientRoles = (List<String>) clientAccess.get("roles");
                if (clientRoles != null && !clientRoles.isEmpty()) {
                    authorities.addAll(clientRoles.stream()
                        .map(role -> {
                            String authority = "ROLE_" + role;
                            return new SimpleGrantedAuthority(authority);
                        })
                        .collect(Collectors.toList()));
                } else {
                    log.debug("gocgac_app client roles is null or empty");
                }
            } else {
                log.debug("gocgac_app not found in resource_access");
            }
        }
        
        log.info("Converted JWT to {} authorities: {}", authorities.size(), 
            authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        
        return authorities;
    }
}

