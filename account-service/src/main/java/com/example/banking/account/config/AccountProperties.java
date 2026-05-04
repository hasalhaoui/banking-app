package com.example.banking.account.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "account")
public record AccountProperties(Security security, Kafka kafka, Limits limits) {

    public record Security(Jwt jwt, List<String> allowedOrigins) {
    }

    public record Jwt(String issuer, String secret, Duration ttl) {
    }

    public record Kafka(Topics topics, Outbox outbox) {
    }

    public record Topics(String accountOpened, String accountFrozen, String transactionPosted) {
    }

    public record Outbox(Duration pollInterval, int batchSize, int maxAttempts) {
    }

    public record Limits(String defaultCurrency, java.math.BigDecimal defaultDailyTransferLimit) {
    }
}
