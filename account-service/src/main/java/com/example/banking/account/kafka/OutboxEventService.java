package com.example.banking.account.kafka;

import com.example.banking.account.entity.OutboxEvent;
import com.example.banking.account.repository.OutboxEventRepository;
import com.example.banking.common.kafka.BankingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(String topic, BankingEvent event) {
        try {
            repository.save(OutboxEvent.builder()
                    .id(event.getEventId())
                    .aggregateType(event.getAggregateType())
                    .aggregateId(event.getAggregateId())
                    .eventType(event.getEventType())
                    .topic(topic)
                    .payload(objectMapper.writeValueAsString(event))
                    .build());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize account event", exception);
        }
    }
}
