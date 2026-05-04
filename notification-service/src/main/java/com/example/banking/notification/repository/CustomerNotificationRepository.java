package com.example.banking.notification.repository;

import com.example.banking.notification.entity.CustomerNotification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerNotificationRepository extends JpaRepository<CustomerNotification, Long> {

    List<CustomerNotification> findByCustomerIdOrderByGeneratedAtDesc(Long customerId);
}
