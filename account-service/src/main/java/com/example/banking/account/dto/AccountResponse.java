package com.example.banking.account.dto;

import com.example.banking.account.entity.AccountStatus;
import com.example.banking.account.entity.AccountType;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private Long customerId;
    private String iban;
    private AccountType type;
    private AccountStatus status;
    private String currency;
    private BigDecimal ledgerBalance;
    private BigDecimal availableBalance;
    private BigDecimal dailyTransferLimit;
    private List<TransactionResponse> transactions;
}
