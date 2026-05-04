package com.example.banking.payment.dto;

import jakarta.validation.constraints.Size;

public record PaymentDecisionRequest(@Size(max = 500, message = "{validation.reason.size}") String reason) {
}
