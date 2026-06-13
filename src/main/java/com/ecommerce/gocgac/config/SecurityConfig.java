package com.ecommerce.gocgac.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    /**
     * Security filter chain cho các public endpoints (Swagger, API docs, Auth)
     * Phải có Order cao hơn để được xử lý trước
     * KHÔNG có OAuth2 Resource Server để tránh validate JWT cho các endpoint này
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**",
                "/configuration/**",
                "/api/auth/**",  // Auth endpoints không cần JWT validation
                "/api/public/**",
                "/actuator/health"
            )
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        
        return http.build();
    }

    /**
     * Security filter chain chính cho các API endpoints cần authentication
     * Có OAuth2 Resource Server để validate JWT
     */
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Debug endpoints (chỉ dùng trong development) - yêu cầu authentication
                .requestMatchers("/api/debug/**").authenticated()
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                // Cooperative Registration endpoints - Cho phép CUSTOMER và SELLER đăng ký HTX
                .requestMatchers("/api/cooperative/registration/**").authenticated()
                // Cooperative Member endpoints - Cho phép CUSTOMER đăng ký thành viên HTX
                .requestMatchers("/api/cooperative/member/**").authenticated()
                // Cooperative Manager endpoints - Quản lý cooperative của mình
                .requestMatchers("/api/cooperative/my-cooperative", "/api/cooperative/update", 
                                "/api/cooperative/members/**")
                    .hasAnyRole("COOPERATIVE_MANAGER", "SUPER_ADMIN")
                // Public Cooperative endpoints - Xem thông tin cooperative
                .requestMatchers("/api/cooperative/**").permitAll()
                // Public Product endpoints - Cho phép người dùng chưa đăng nhập và người mua hàng xem sản phẩm
                .requestMatchers("/api/products/public/**").permitAll()
                // Product Search endpoints - Public search với Elasticsearch
                .requestMatchers("/api/products/search").permitAll()
                // Public Category endpoints
                .requestMatchers("/api/categories/public/**").permitAll()
                // Public Channel endpoints
                .requestMatchers("/api/channels/**").permitAll()
                // Public Review endpoints (xem đánh giá đã duyệt)
                .requestMatchers("/api/reviews/public/**").permitAll()
                // Public Store Category endpoints (storefront)
                .requestMatchers("/api/store-categories/public/**").permitAll()
                // Seller endpoints - Cho phép SELLER, COOPERATIVE_MANAGER, SUPER_ADMIN
                .requestMatchers("/api/seller/**").hasAnyRole("SELLER", "COOPERATIVE_MANAGER", "SUPER_ADMIN")
                // Customer endpoints
                .requestMatchers("/api/customer/**").hasAnyRole("CUSTOMER", "SELLER", "COOPERATIVE_MANAGER", "SUPER_ADMIN")
                // Storage endpoints - Yêu cầu authentication
                .requestMatchers("/api/storage/**").authenticated()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new com.ecommerce.gocgac.config.JwtGrantedAuthoritiesConverter());
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4202", "http://localhost:8086"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

