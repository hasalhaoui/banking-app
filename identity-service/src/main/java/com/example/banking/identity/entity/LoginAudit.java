package com.example.banking.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
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
@Table(name = "login_audit")
public class LoginAudit extends BaseEntity {

    @Column(nullable = false, length = 180)
    private String username;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 80)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private Instant occurredAt;
}
