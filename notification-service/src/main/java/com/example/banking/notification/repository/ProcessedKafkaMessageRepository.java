package com.example.banking.notification.repository;

import com.example.banking.notification.entity.ProcessedKafkaMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedKafkaMessageRepository extends JpaRepository<ProcessedKafkaMessage, UUID> {

    boolean existsByMessageIdAndConsumerGroup(UUID messageId, String consumerGroup);
}
