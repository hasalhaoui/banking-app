package com.example.banking.account.kafka;

import com.example.banking.account.config.AccountProperties;
import com.example.banking.account.entity.OutboxEvent;
import com.example.banking.account.repository.OutboxEventRepository;
import com.example.banking.common.kafka.OutboxStatus;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AccountProperties properties;

    @Scheduled(fixedDelayString = "${account.kafka.outbox.poll-interval:PT10S}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> events = repository.findTop100ByStatusInAndAttemptsLessThanOrderByCreatedAtAsc(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), properties.kafka().outbox().maxAttempts());
        events.stream().limit(properties.kafka().outbox().batchSize()).forEach(this::publish);
    }

    private void publish(OutboxEvent event) {
        try {
            kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload()).get(10, TimeUnit.SECONDS);
            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
            event.setLastError(null);
        } catch (Exception exception) {
            event.setStatus(OutboxStatus.FAILED);
            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(exception.getMessage());
            log.warn("Failed publishing account outbox event id={} attempts={}", event.getId(), event.getAttempts(), exception);
        }
    }
}
