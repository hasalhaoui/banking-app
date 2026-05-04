package com.example.banking.payment.config;

import com.example.banking.common.config.OpenApiFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentOpenApi() {
        return OpenApiFactory.serviceApi("Payment Service API",
                "Beneficiaries, internal transfers, SEPA/SWIFT payments, approval workflow, scheduling, and idempotency.");
    }
}
