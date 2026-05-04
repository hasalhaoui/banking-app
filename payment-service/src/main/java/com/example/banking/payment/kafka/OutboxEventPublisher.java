package com.example.banking.payment.kafka;

import com.example.banking.common.kafka.OutboxStatus;
import com.example.banking.payment.config.PaymentProperties;
import com.example.banking.payment.entity.OutboxEvent;
import com.example.banking.payment.repository.OutboxEventRepository;
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
    private final PaymentProperties properties;

    @Scheduled(fixedDelayString = "${payment.kafka.outbox.poll-interval:PT10S}")
    @Transactional
    public void publishPending() {
        repository.findTop100ByStatusInAndAttemptsLessThanOrderByCreatedAtAsc(
                        List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), properties.kafka().outbox().maxAttempts())
                .stream()
                .limit(properties.kafka().outbox().batchSize())
                .forEach(this::publish);
    }

    private void publish(OutboxEvent event) {
        try {
            kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload()).get(10, TimeUnit.SECONDS);
            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
            event.setLastError(null);
        } catch (Exception exception) {
            event.setAttempts(event.getAttempts() + 1);
            event.setStatus(OutboxStatus.FAILED);
            event.setLastError(exception.getMessage());
            log.warn("Failed publishing payment outbox event id={} attempts={}", event.getId(), event.getAttempts(), exception);
        }
    }
}
