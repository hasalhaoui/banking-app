package com.example.banking.payment.repository;

import com.example.banking.common.kafka.OutboxStatus;
import com.example.banking.payment.entity.OutboxEvent;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusInAndAttemptsLessThanOrderByCreatedAtAsc(Collection<OutboxStatus> statuses,
                                                                                 int maxAttempts);
}
