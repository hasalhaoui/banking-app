package com.example.banking.account.config;

import com.example.banking.common.config.OpenApiFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountOpenApi() {
        return OpenApiFactory.serviceApi("Account Service API",
                "Account opening, balances, statements, limits, holds, and transaction posting APIs.");
    }
}
