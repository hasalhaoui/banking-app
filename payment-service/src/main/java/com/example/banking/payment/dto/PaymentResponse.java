package com.example.banking.payment.dto;

import com.example.banking.payment.entity.PaymentStatus;
import com.example.banking.payment.entity.PaymentType;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long customerId;
    private Long sourceAccountId;
    private String sourceIban;
    private BeneficiaryResponse beneficiary;
    private PaymentType type;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String paymentReference;
    private BigDecimal riskScore;
    private Instant scheduledFor;
    private Instant approvedAt;
    private Instant executedAt;
    private String rejectionReason;
}
