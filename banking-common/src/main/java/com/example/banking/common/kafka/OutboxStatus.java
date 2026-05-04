package com.example.banking.common.kafka;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
