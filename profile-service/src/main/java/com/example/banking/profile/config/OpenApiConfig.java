package com.example.banking.profile.config;

import com.example.banking.common.config.OpenApiFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI profileOpenApi() {
        return OpenApiFactory.serviceApi("Profile Service API",
                "Customer profiles, addresses, consent, KYC, and risk profile management.");
    }
}
