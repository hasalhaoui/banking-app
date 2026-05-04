package com.example.banking.account.dto;

import com.example.banking.account.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PostTransactionRequest(@NotNull(message = "{validation.transactionType.required}") TransactionType type,
                                     @NotNull(message = "{validation.amount.required}")
                                     @DecimalMin(value = "0.01", message = "{validation.amount.positive}") BigDecimal amount,
                                     @NotBlank(message = "{validation.reference.required}") String reference,
                                     @NotBlank(message = "{validation.description.required}") String description) {
}
