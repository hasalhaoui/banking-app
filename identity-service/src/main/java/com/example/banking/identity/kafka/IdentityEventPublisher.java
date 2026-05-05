package com.example.banking.identity.kafka;

import com.example.banking.common.kafka.BankingEvent;
import com.example.banking.identity.config.IdentityProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentityEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;  // tools.jackson.databind.ObjectMapper
    private final IdentityProperties properties;

    public void publishCustomerRegistered(BankingEvent event) {
        try {
            kafkaTemplate.send(
                    properties.kafka().customerRegisteredTopic(),
                    event.getAggregateId(),
                    objectMapper.writeValueAsString(event)
            );
        } catch (JacksonException exception) {  // tools.jackson.core.JacksonException
            throw new IllegalStateException("Unable to serialize identity event", exception);
        }
    }
}
