package com.example.banking.account.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record StatementResponse(Long accountId, Instant from, Instant to, List<TransactionResponse> transactions) {
}
