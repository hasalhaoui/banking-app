package com.example.banking.identity.config;

import com.example.banking.common.config.OpenApiFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI identityOpenApi() {
        return OpenApiFactory.serviceApi("Identity Service API",
                "Authentication, RBAC, MFA flags, OAuth2 login, and user lifecycle APIs.");
    }
}
