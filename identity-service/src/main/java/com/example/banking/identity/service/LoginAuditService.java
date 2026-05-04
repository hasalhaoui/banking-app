package com.example.banking.identity.service;

import com.example.banking.identity.entity.LoginAudit;
import com.example.banking.identity.repository.LoginAuditRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginAuditService {

    private final LoginAuditRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String username, boolean success, String ipAddress, String userAgent, String reason) {
        repository.save(LoginAudit.builder()
                .username(username)
                .success(success)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .failureReason(reason)
                .occurredAt(Instant.now())
                .build());
    }
}
