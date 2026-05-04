package com.example.banking.notification.dto;

import com.example.banking.notification.entity.NotificationChannel;
import com.example.banking.notification.entity.NotificationStatus;
import java.time.Instant;
import lombok.Builder;

@Builder
public record NotificationResponse(Long id, NotificationChannel channel, NotificationStatus status, String eventType,
                                   String title, String message, Instant generatedAt) {
}
