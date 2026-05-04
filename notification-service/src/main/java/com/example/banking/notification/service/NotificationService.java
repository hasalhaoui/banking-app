package com.example.banking.notification.service;

import com.example.banking.common.error.EntityNotFoundException;
import com.example.banking.common.kafka.BankingEvent;
import com.example.banking.notification.dto.NotificationResponse;
import com.example.banking.notification.entity.CustomerNotification;
import com.example.banking.notification.entity.NotificationChannel;
import com.example.banking.notification.entity.NotificationStatus;
import com.example.banking.notification.entity.ProcessedKafkaMessage;
import com.example.banking.notification.repository.CustomerNotificationRepository;
import com.example.banking.notification.repository.ProcessedKafkaMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final CustomerNotificationRepository notificationRepository;
    private final ProcessedKafkaMessageRepository processedRepository;

    @Transactional
    public void createFromEvent(BankingEvent event, String topic, String consumerGroup) {
        if (processedRepository.existsByMessageIdAndConsumerGroup(event.getEventId(), consumerGroup)) {
            return;
        }
        Long customerId = Long.valueOf(event.getCustomerId());
        notificationRepository.save(CustomerNotification.builder()
                .customerId(customerId)
                .channel(NotificationChannel.IN_APP)
                .status(NotificationStatus.UNREAD)
                .eventType(event.getEventType())
                .title(titleFor(event.getEventType()))
                .message("A banking event was processed: " + event.getEventType())
                .generatedAt(Instant.now())
                .build());
        try {
            processedRepository.save(ProcessedKafkaMessage.builder()
                    .id(UUID.randomUUID())
                    .messageId(event.getEventId())
                    .consumerGroup(consumerGroup)
                    .topic(topic)
                    .processedAt(Instant.now())
                    .build());
        } catch (DataIntegrityViolationException ignored) {
            // Another consumer instance processed it first.
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listMine(Long customerId) {
        return notificationRepository.findByCustomerIdOrderByGeneratedAtDesc(customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markRead(Long customerId, Long notificationId) {
        CustomerNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + notificationId));
        if (!notification.getCustomerId().equals(customerId)) {
            throw new EntityNotFoundException("Notification not found: " + notificationId);
        }
        notification.setStatus(NotificationStatus.READ);
        return toResponse(notification);
    }

    private NotificationResponse toResponse(CustomerNotification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .channel(notification.getChannel())
                .status(notification.getStatus())
                .eventType(notification.getEventType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .generatedAt(notification.getGeneratedAt())
                .build();
    }

    private String titleFor(String eventType) {
        return switch (eventType) {
            case "payment-executed" -> "Payment executed";
            case "payment-rejected" -> "Payment rejected";
            case "account-opened" -> "Account opened";
            case "transaction-posted" -> "Transaction posted";
            default -> "Banking update";
        };
    }
}
