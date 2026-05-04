package com.example.banking.profile.kafka;

import com.example.banking.common.kafka.BankingEvent;
import com.example.banking.profile.config.ProfileProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ProfileProperties properties;

    public void publishKycSubmitted(BankingEvent event) {
        try {
            kafkaTemplate.send(properties.kafka().kycSubmittedTopic(), event.getAggregateId(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize profile event", exception);
        }
    }
}
