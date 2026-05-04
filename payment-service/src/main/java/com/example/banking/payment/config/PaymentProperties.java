package com.example.banking.payment.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(Security security, Kafka kafka, Risk risk) {

    public record Security(Jwt jwt, List<String> allowedOrigins) {
    }

    public record Jwt(String issuer, String secret, Duration ttl) {
    }

    public record Kafka(Topics topics, Outbox outbox) {
    }

    public record Topics(String paymentInitiated, String paymentApproved, String paymentExecuted, String paymentRejected) {
    }

    public record Outbox(Duration pollInterval, int batchSize, int maxAttempts) {
    }

    public record Risk(BigDecimal manualApprovalThreshold, BigDecimal highRiskThreshold) {
    }
}
