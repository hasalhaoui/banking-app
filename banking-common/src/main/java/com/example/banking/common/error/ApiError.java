package com.example.banking.common.error;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String correlationId,
        List<FieldViolation> violations) {

    @Builder
    public record FieldViolation(String field, String message, Object rejectedValue) {
    }
}
