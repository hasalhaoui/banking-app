package com.example.banking.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Table(name = "payment_instruction")
public class PaymentInstruction extends BaseEntity {

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long sourceAccountId;

    @Column(nullable = false, length = 40)
    private String sourceIban;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, unique = true, length = 80)
    private String paymentReference;

    @Column(nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @Column(nullable = false, length = 500)
    private String remittanceInformation;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;

    private Instant scheduledFor;
    private Instant approvedAt;
    private Instant executedAt;

    @Column(length = 500)
    private String rejectionReason;
}
