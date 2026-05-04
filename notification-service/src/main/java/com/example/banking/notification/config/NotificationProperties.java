package com.example.banking.notification.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(Security security, Kafka kafka) {

    public record Security(Jwt jwt, List<String> allowedOrigins) {
    }

    public record Jwt(String issuer, String secret, Duration ttl) {
    }

    public record Kafka(String subscribedTopics, String consumerGroup) {
    }
}
