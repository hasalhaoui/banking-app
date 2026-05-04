package com.example.banking.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AccountLimitRequest(@NotNull(message = "{validation.limit.required}")
                                  @DecimalMin(value = "1.00", message = "{validation.limit.positive}")
                                  BigDecimal dailyTransferLimit) {
}
