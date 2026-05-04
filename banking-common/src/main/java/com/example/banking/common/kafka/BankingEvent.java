package com.example.banking.common.kafka;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankingEvent {

    private UUID eventId;
    private String eventType;
    private String aggregateType;
    private String aggregateId;
    private String customerId;
    private Instant occurredAt;
    private Map<String, Object> payload;
}
