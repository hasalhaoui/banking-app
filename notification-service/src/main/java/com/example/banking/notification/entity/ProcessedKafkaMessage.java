package com.example.banking.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "processed_kafka_message",
        uniqueConstraints = @UniqueConstraint(name = "uk_processed_message_consumer", columnNames = {"message_id", "consumer_group"}))
public class ProcessedKafkaMessage {

    @Id
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "consumer_group", nullable = false, length = 120)
    private String consumerGroup;

    @Column(nullable = false, length = 180)
    private String topic;

    @Column(nullable = false)
    private Instant processedAt;
}
