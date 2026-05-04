package com.example.banking.payment.entity;

public enum PaymentStatus {
    DRAFT,
    PENDING_MFA,
    PENDING_APPROVAL,
    APPROVED,
    SCHEDULED,
    EXECUTED,
    REJECTED,
    CANCELLED
}
