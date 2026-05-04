package com.example.banking.payment.dto;

import com.example.banking.common.validation.Iban;
import com.example.banking.payment.entity.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class PaymentInitiationRequest {

    @NotNull(message = "{validation.sourceAccountId.required}")
    private Long sourceAccountId;

    @Iban
    private String sourceIban;

    @NotNull(message = "{validation.beneficiary.required}")
    private Long beneficiaryId;

    @NotNull(message = "{validation.paymentType.required}")
    private PaymentType type;

    @NotNull(message = "{validation.amount.required}")
    @DecimalMin(value = "0.01", message = "{validation.amount.positive}")
    private BigDecimal amount;

    @Pattern(regexp = "^[A-Z]{3}$", message = "{validation.currency.invalid}")
    private String currency;

    @NotBlank(message = "{validation.remittance.required}")
    private String remittanceInformation;

    @FutureOrPresent(message = "{validation.scheduledFor.future}")
    private Instant scheduledFor;
}
