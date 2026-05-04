package com.example.banking.payment.service;

import com.example.banking.common.error.BusinessRuleViolationException;
import com.example.banking.common.error.EntityNotFoundException;
import com.example.banking.common.kafka.BankingEvent;
import com.example.banking.common.kafka.EventNames;
import com.example.banking.payment.config.PaymentProperties;
import com.example.banking.payment.dto.BeneficiaryRequest;
import com.example.banking.payment.dto.BeneficiaryResponse;
import com.example.banking.payment.dto.PaymentDecisionRequest;
import com.example.banking.payment.dto.PaymentInitiationRequest;
import com.example.banking.payment.dto.PaymentResponse;
import com.example.banking.payment.entity.Beneficiary;
import com.example.banking.payment.entity.BeneficiaryStatus;
import com.example.banking.payment.entity.PaymentInstruction;
import com.example.banking.payment.entity.PaymentStatus;
import com.example.banking.payment.kafka.OutboxEventService;
import com.example.banking.payment.mapper.PaymentMapper;
import com.example.banking.payment.repository.BeneficiaryRepository;
import com.example.banking.payment.repository.PaymentInstructionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final PaymentInstructionRepository paymentRepository;
    private final PaymentMapper mapper;
    private final PaymentProperties properties;
    private final OutboxEventService outboxEventService;

    @Transactional
    public BeneficiaryResponse createBeneficiary(Long customerId, BeneficiaryRequest request) {
        Beneficiary beneficiary = Beneficiary.builder()
                .customerId(customerId)
                .nickname(request.getNickname())
                .accountHolderName(request.getAccountHolderName())
                .iban(request.getIban().replace(" ", ""))
                .bic(request.getBic())
                .countryCode(request.getCountryCode())
                .status(BeneficiaryStatus.ACTIVE)
                .build();
        return mapper.toBeneficiaryResponse(beneficiaryRepository.save(beneficiary));
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> listBeneficiaries(Long customerId) {
        return mapper.toBeneficiaryResponses(beneficiaryRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
                customerId, BeneficiaryStatus.ACTIVE));
    }

    @Transactional
    public PaymentResponse initiate(Long customerId, String idempotencyKey, PaymentInitiationRequest request) {
        return paymentRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey)
                .map(mapper::toPaymentResponse)
                .orElseGet(() -> createPayment(customerId, idempotencyKey, request));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listMine(Long customerId) {
        return mapper.toPaymentResponses(paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
    }

    @Transactional
    public PaymentResponse cancelMine(Long customerId, Long paymentId) {
        PaymentInstruction payment = load(paymentId);
        assertOwner(customerId, payment);
        if (payment.getStatus() == PaymentStatus.EXECUTED) {
            throw new BusinessRuleViolationException("Executed payments cannot be cancelled");
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        return mapper.toPaymentResponse(payment);
    }

    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE','SUPPORT')")
    @Transactional
    public PaymentResponse approve(Long paymentId) {
        PaymentInstruction payment = load(paymentId);
        if (payment.getStatus() != PaymentStatus.PENDING_APPROVAL && payment.getStatus() != PaymentStatus.PENDING_MFA) {
            throw new BusinessRuleViolationException("Payment is not awaiting approval");
        }
        payment.setStatus(payment.getScheduledFor() == null ? PaymentStatus.EXECUTED : PaymentStatus.SCHEDULED);
        payment.setApprovedAt(Instant.now());
        if (payment.getStatus() == PaymentStatus.EXECUTED) {
            payment.setExecutedAt(Instant.now());
            enqueue(payment, EventNames.PAYMENT_EXECUTED, properties.kafka().topics().paymentExecuted());
        } else {
            enqueue(payment, EventNames.PAYMENT_APPROVED, properties.kafka().topics().paymentApproved());
        }
        return mapper.toPaymentResponse(payment);
    }

    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE','SUPPORT')")
    @Transactional
    public PaymentResponse reject(Long paymentId, PaymentDecisionRequest request) {
        PaymentInstruction payment = load(paymentId);
        payment.setStatus(PaymentStatus.REJECTED);
        payment.setRejectionReason(request.reason());
        enqueue(payment, EventNames.PAYMENT_REJECTED, properties.kafka().topics().paymentRejected());
        return mapper.toPaymentResponse(payment);
    }

    private PaymentResponse createPayment(Long customerId, String idempotencyKey, PaymentInitiationRequest request) {
        Beneficiary beneficiary = beneficiaryRepository.findById(request.getBeneficiaryId())
                .orElseThrow(() -> new EntityNotFoundException("Beneficiary not found: " + request.getBeneficiaryId()));
        if (!beneficiary.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("Beneficiary belongs to another customer");
        }
        BigDecimal riskScore = scoreRisk(request);
        PaymentStatus initialStatus = riskScore.compareTo(properties.risk().highRiskThreshold()) >= 0
                || request.getAmount().compareTo(properties.risk().manualApprovalThreshold()) >= 0
                ? PaymentStatus.PENDING_APPROVAL
                : PaymentStatus.PENDING_MFA;
        PaymentInstruction payment = PaymentInstruction.builder()
                .customerId(customerId)
                .sourceAccountId(request.getSourceAccountId())
                .sourceIban(request.getSourceIban().replace(" ", ""))
                .beneficiary(beneficiary)
                .type(request.getType())
                .status(initialStatus)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentReference(nextPaymentReference())
                .idempotencyKey(idempotencyKey)
                .remittanceInformation(request.getRemittanceInformation())
                .riskScore(riskScore)
                .scheduledFor(request.getScheduledFor())
                .build();
        PaymentInstruction saved = paymentRepository.save(payment);
        enqueue(saved, EventNames.PAYMENT_INITIATED, properties.kafka().topics().paymentInitiated());
        return mapper.toPaymentResponse(saved);
    }

    private PaymentInstruction load(Long paymentId) {
        return paymentRepository.findWithBeneficiaryById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + paymentId));
    }

    private void assertOwner(Long customerId, PaymentInstruction payment) {
        if (!payment.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("Payment belongs to another customer");
        }
    }

    private BigDecimal scoreRisk(PaymentInitiationRequest request) {
        BigDecimal score = BigDecimal.valueOf(10);
        if (request.getAmount().compareTo(properties.risk().manualApprovalThreshold()) >= 0) {
            score = score.add(BigDecimal.valueOf(40));
        }
        if (request.getType().name().equals("SWIFT")) {
            score = score.add(BigDecimal.valueOf(25));
        }
        return score;
    }

    private void enqueue(PaymentInstruction payment, String eventType, String topic) {
        outboxEventService.enqueue(topic, BankingEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .aggregateType("PaymentInstruction")
                .aggregateId(payment.getId().toString())
                .customerId(payment.getCustomerId().toString())
                .occurredAt(Instant.now())
                .payload(Map.of("reference", payment.getPaymentReference(), "amount", payment.getAmount(),
                        "currency", payment.getCurrency(), "status", payment.getStatus().name()))
                .build());
    }

    private String nextPaymentReference() {
        return "PAY-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
