package com.example.banking.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import java.time.Instant;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.KafkaException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                 HttpServletRequest request) {
        List<ApiError.FieldViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> ApiError.FieldViolation.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                resolve("error.validation", "Validation failed"), request, violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception,
                                                              HttpServletRequest request) {
        List<ApiError.FieldViolation> violations = exception.getConstraintViolations().stream()
                .map(violation -> ApiError.FieldViolation.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                resolve("error.validation", "Validation failed"), request, violations);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler({BusinessRuleViolationException.class, DuplicateRequestException.class})
    public ResponseEntity<ApiError> handleBusiness(RuntimeException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                resolve("error.accessDenied", "Access denied"), request, List.of());
    }

    @ExceptionHandler({KafkaException.class, KafkaProcessingException.class})
    public ResponseEntity<ApiError> handleKafka(RuntimeException exception, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "KAFKA_ERROR",
                resolve("error.kafka", "Messaging service is temporarily unavailable"), request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException exception,
                                                        HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                resolve("error.dataIntegrity", "Data integrity violation"), request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                resolve("error.internal", "Internal server error"), request, List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message, HttpServletRequest request,
                                           List<ApiError.FieldViolation> violations) {
        ApiError error = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .correlationId(request.getHeader(CORRELATION_HEADER))
                .violations(violations)
                .build();
        return ResponseEntity.status(status).body(error);
    }

    private String resolve(String code, String fallback) {
        return messageSource.getMessage(code, null, fallback, LocaleContextHolder.getLocale());
    }
}
