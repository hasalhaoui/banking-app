package com.example.banking.account.dto;

import com.example.banking.account.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAccountRequest {

    @NotNull(message = "{validation.accountType.required}")
    private AccountType type;

    @Pattern(regexp = "^[A-Z]{3}$", message = "{validation.currency.invalid}")
    private String currency;

    @DecimalMin(value = "0.00", message = "{validation.openingBalance.positive}")
    private BigDecimal openingBalance;
}
