package com.example.banking.common.error;

public class KafkaProcessingException extends RuntimeException {

    public KafkaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
