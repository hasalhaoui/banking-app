package com.example.banking.identity.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity")
public record IdentityProperties(Security security, Kafka kafka, Risk risk) {

    public record Security(Jwt jwt, List<String> allowedOrigins) {
    }

    public record Jwt(String issuer, String secret, Duration ttl) {
    }

    public record Kafka(String customerRegisteredTopic) {
    }

    public record Risk(int maxFailedLoginAttempts) {
    }
}
