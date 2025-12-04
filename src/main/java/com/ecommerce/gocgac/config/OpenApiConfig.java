package com.ecommerce.gocgac.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gốc gác Management API")
                        .description("REST API cho hệ thống quản lý hợp tác xã")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Gốc gác IT Team")
                                .email("it-support@gocgac.com.vn")
                                .url("https://www.gocgac.vn"))
                        .license(new License()
                                .name("Gốc gac License")
                                .url("https://www.gocgac.vn/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8086/").description("Development Server"),
                        new Server().url("https://api.gocgac.com/api").description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .name("Authorization")
                                .description("JWT Authorization header using the Bearer scheme. Example: 'Bearer {token}'")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}

