package com.example.banking.notification.kafka;

import com.example.banking.common.error.KafkaProcessingException;
import com.example.banking.common.kafka.BankingEvent;
import com.example.banking.notification.config.NotificationProperties;
import com.example.banking.notification.service.NotificationService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankingEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final NotificationProperties properties;

    @KafkaListener(topics = "#{'${notification.kafka.subscribed-topics}'.split(',')}",
            groupId = "${notification.kafka.consumer-group:notification-service}")
    public void consume(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            BankingEvent event = objectMapper.readValue(payload, BankingEvent.class);
            notificationService.createFromEvent(event, topic, properties.kafka().consumerGroup());
        } catch (JacksonException exception) {
            throw new KafkaProcessingException("Unable to deserialize banking event", exception);
        }
    }
}