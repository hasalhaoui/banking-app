package com.example.banking.payment.controller;

import com.example.banking.common.security.SecurityClaims;
import com.example.banking.payment.dto.BeneficiaryRequest;
import com.example.banking.payment.dto.BeneficiaryResponse;
import com.example.banking.payment.dto.PaymentDecisionRequest;
import com.example.banking.payment.dto.PaymentInitiationRequest;
import com.example.banking.payment.dto.PaymentResponse;
import com.example.banking.payment.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/beneficiaries")
    public ResponseEntity<BeneficiaryResponse> createBeneficiary(@Valid @RequestBody BeneficiaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createBeneficiary(SecurityClaims.currentUserId(), request));
    }

    @GetMapping("/beneficiaries")
    public ResponseEntity<List<BeneficiaryResponse>> listBeneficiaries() {
        return ResponseEntity.ok(paymentService.listBeneficiaries(SecurityClaims.currentUserId()));
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                    @Valid @RequestBody PaymentInitiationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiate(SecurityClaims.currentUserId(), idempotencyKey, request));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> listMine() {
        return ResponseEntity.ok(paymentService.listMine(SecurityClaims.currentUserId()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.cancelMine(SecurityClaims.currentUserId(), id));
    }

    @PostMapping("/admin/{id}/approve")
    public ResponseEntity<PaymentResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.approve(id));
    }

    @PostMapping("/admin/{id}/reject")
    public ResponseEntity<PaymentResponse> reject(@PathVariable Long id,
                                                  @Valid @RequestBody PaymentDecisionRequest request) {
        return ResponseEntity.ok(paymentService.reject(id, request));
    }
}
