package com.example.banking.payment.kafka;

import com.example.banking.common.kafka.BankingEvent;
import com.example.banking.payment.entity.OutboxEvent;
import com.example.banking.payment.repository.OutboxEventRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
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
        } catch (JacksonException exception) {
            throw new IllegalStateException("Unable to serialize payment event", exception);
        }
    }
}