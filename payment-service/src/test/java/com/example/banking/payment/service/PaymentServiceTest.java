package com.example.banking.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.banking.payment.config.PaymentProperties;
import com.example.banking.payment.dto.BeneficiaryRequest;
import com.example.banking.payment.dto.BeneficiaryResponse;
import com.example.banking.payment.entity.Beneficiary;
import com.example.banking.payment.entity.BeneficiaryStatus;
import com.example.banking.payment.kafka.OutboxEventService;
import com.example.banking.payment.mapper.PaymentMapper;
import com.example.banking.payment.repository.BeneficiaryRepository;
import com.example.banking.payment.repository.PaymentInstructionRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private BeneficiaryRepository beneficiaryRepository;
    @Mock
    private PaymentInstructionRepository paymentRepository;
    @Mock
    private PaymentMapper mapper;
    @Mock
    private OutboxEventService outboxEventService;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        PaymentProperties properties = new PaymentProperties(
                new PaymentProperties.Security(new PaymentProperties.Jwt("issuer", "test-secret-change-me-at-least-32-bytes", Duration.ofHours(1)),
                        List.of("http://localhost")),
                new PaymentProperties.Kafka(new PaymentProperties.Topics("payment-initiated", "payment-approved", "payment-executed", "payment-rejected"),
                        new PaymentProperties.Outbox(Duration.ofSeconds(10), 50, 10)),
                new PaymentProperties.Risk(new BigDecimal("10000.00"), new BigDecimal("70.00")));
        service = new PaymentService(beneficiaryRepository, paymentRepository, mapper, properties, outboxEventService);
    }

    @Test
    void createsActiveBeneficiary() {
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenAnswer(invocation -> {
            Beneficiary beneficiary = invocation.getArgument(0);
            beneficiary.setId(7L);
            return beneficiary;
        });
        when(mapper.toBeneficiaryResponse(any(Beneficiary.class))).thenReturn(BeneficiaryResponse.builder()
                .id(7L)
                .status(BeneficiaryStatus.ACTIVE)
                .build());

        BeneficiaryResponse response = service.createBeneficiary(42L, BeneficiaryRequest.builder()
                .nickname("Rent")
                .accountHolderName("Landlord")
                .iban("FR7630006000011234567890189")
                .countryCode("FR")
                .build());

        assertThat(response.getStatus()).isEqualTo(BeneficiaryStatus.ACTIVE);
    }
}
