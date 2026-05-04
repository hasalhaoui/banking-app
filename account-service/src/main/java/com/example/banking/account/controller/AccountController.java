package com.example.banking.account.controller;

import com.example.banking.account.dto.AccountLimitRequest;
import com.example.banking.account.dto.AccountResponse;
import com.example.banking.account.dto.OpenAccountRequest;
import com.example.banking.account.dto.PostTransactionRequest;
import com.example.banking.account.dto.StatementResponse;
import com.example.banking.account.service.AccountService;
import com.example.banking.common.security.SecurityClaims;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.openAccount(SecurityClaims.currentUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> listMine() {
        return ResponseEntity.ok(accountService.listMine(SecurityClaims.currentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getMine(SecurityClaims.currentUserId(), id));
    }

    @GetMapping("/{id}/statement")
    public ResponseEntity<StatementResponse> statement(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ResponseEntity.ok(accountService.statement(SecurityClaims.currentUserId(), id, from, to));
    }

    @PutMapping("/{id}/limits")
    public ResponseEntity<AccountResponse> setLimit(@PathVariable Long id, @Valid @RequestBody AccountLimitRequest request) {
        return ResponseEntity.ok(accountService.setLimit(SecurityClaims.currentUserId(), id, request));
    }

    @PatchMapping("/admin/{id}/freeze")
    public ResponseEntity<AccountResponse> freeze(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.freeze(id));
    }

    @PatchMapping("/admin/{id}/unfreeze")
    public ResponseEntity<AccountResponse> unfreeze(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.unfreeze(id));
    }

    @PostMapping("/admin/{id}/transactions")
    public ResponseEntity<AccountResponse> postTransaction(@PathVariable Long id,
                                                           @Valid @RequestBody PostTransactionRequest request) {
        return ResponseEntity.ok(accountService.postTransaction(id, request));
    }
}
