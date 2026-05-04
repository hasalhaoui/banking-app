package com.example.banking.notification.config;

import com.example.banking.common.config.OpenApiFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationOpenApi() {
        return OpenApiFactory.serviceApi("Notification Service API",
                "Customer notification inbox and idempotent Kafka event notification processing.");
    }
}
