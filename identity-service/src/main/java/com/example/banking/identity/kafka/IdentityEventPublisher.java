package com.example.banking.identity.kafka;

import com.example.banking.common.kafka.BankingEvent;
import com.example.banking.identity.config.IdentityProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentityEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final IdentityProperties properties;

    public void publishCustomerRegistered(BankingEvent event) {
        try {
            kafkaTemplate.send(properties.kafka().customerRegisteredTopic(), event.getAggregateId(),
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize identity event", exception);
        }
    }
}
