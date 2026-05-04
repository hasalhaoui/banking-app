package com.example.banking.account.dto;

import com.example.banking.account.entity.TransactionType;
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
public class TransactionResponse {

    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String currency;
    private String reference;
    private String description;
    private Instant postedAt;
}
