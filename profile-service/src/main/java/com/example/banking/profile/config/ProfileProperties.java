package com.example.banking.profile.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "profile")
public record ProfileProperties(Security security, Kafka kafka) {

    public record Security(Jwt jwt, List<String> allowedOrigins) {
    }

    public record Jwt(String issuer, String secret, Duration ttl) {
    }

    public record Kafka(String kycSubmittedTopic) {
    }
}
