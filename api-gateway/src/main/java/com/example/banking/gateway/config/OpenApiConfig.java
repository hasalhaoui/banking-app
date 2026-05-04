package com.example.banking.gateway.config;

import com.example.banking.common.config.OpenApiFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenApi() {
        return OpenApiFactory.serviceApi("Banking API Gateway",
                "Gateway and BFF entry point for the banking microservices platform.");
    }
}
