package com.example.banking.account.service;

import com.example.banking.account.config.AccountProperties;
import com.example.banking.account.dto.AccountLimitRequest;
import com.example.banking.account.dto.AccountResponse;
import com.example.banking.account.dto.OpenAccountRequest;
import com.example.banking.account.dto.PostTransactionRequest;
import com.example.banking.account.dto.StatementResponse;
import com.example.banking.account.entity.AccountStatus;
import com.example.banking.account.entity.AccountTransaction;
import com.example.banking.account.entity.BankAccount;
import com.example.banking.account.entity.TransactionType;
import com.example.banking.account.kafka.OutboxEventService;
import com.example.banking.account.mapper.AccountMapper;
import com.example.banking.account.repository.AccountTransactionRepository;
import com.example.banking.account.repository.BankAccountRepository;
import com.example.banking.common.error.BusinessRuleViolationException;
import com.example.banking.common.error.DuplicateRequestException;
import com.example.banking.common.error.EntityNotFoundException;
import com.example.banking.common.kafka.BankingEvent;
import com.example.banking.common.kafka.EventNames;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BankAccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;
    private final AccountMapper mapper;
    private final AccountProperties properties;
    private final OutboxEventService outboxEventService;

    @Transactional
    public AccountResponse openAccount(Long customerId, OpenAccountRequest request) {
        String currency = request.getCurrency() == null ? properties.limits().defaultCurrency() : request.getCurrency();
        BigDecimal openingBalance = request.getOpeningBalance() == null ? BigDecimal.ZERO : request.getOpeningBalance();
        BankAccount account = BankAccount.builder()
                .customerId(customerId)
                .iban(nextIban())
                .type(request.getType())
                .status(AccountStatus.ACTIVE)
                .currency(currency)
                .ledgerBalance(openingBalance)
                .availableBalance(openingBalance)
                .dailyTransferLimit(properties.limits().defaultDailyTransferLimit())
                .build();
        BankAccount saved = accountRepository.save(account);
        enqueueAccountEvent(saved, EventNames.ACCOUNT_OPENED, properties.kafka().topics().accountOpened());
        if (openingBalance.signum() > 0) {
            postTransaction(saved, new PostTransactionRequest(TransactionType.CREDIT, openingBalance,
                    "OPEN-" + saved.getIban(), "Opening balance"));
        }
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listMine(Long customerId) {
        return mapper.toResponses(accountRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
    }

    @Transactional(readOnly = true)
    public AccountResponse getMine(Long customerId, Long accountId) {
        BankAccount account = loadWithTransactions(accountId);
        assertOwner(customerId, account);
        return mapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public StatementResponse statement(Long customerId, Long accountId, Instant from, Instant to) {
        BankAccount account = load(accountId);
        assertOwner(customerId, account);
        return StatementResponse.builder()
                .accountId(accountId)
                .from(from)
                .to(to)
                .transactions(transactionRepository.findByAccountIdAndPostedAtBetweenOrderByPostedAtDesc(accountId, from, to)
                        .stream().map(mapper::toTransactionResponse).toList())
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPPORT')")
    @Transactional
    public AccountResponse freeze(Long accountId) {
        BankAccount account = load(accountId);
        account.setStatus(AccountStatus.FROZEN);
        enqueueAccountEvent(account, EventNames.ACCOUNT_FROZEN, properties.kafka().topics().accountFrozen());
        return mapper.toResponse(account);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPPORT')")
    @Transactional
    public AccountResponse unfreeze(Long accountId) {
        BankAccount account = load(accountId);
        account.setStatus(AccountStatus.ACTIVE);
        return mapper.toResponse(account);
    }

    @Transactional
    public AccountResponse setLimit(Long customerId, Long accountId, AccountLimitRequest request) {
        BankAccount account = load(accountId);
        assertOwner(customerId, account);
        account.setDailyTransferLimit(request.dailyTransferLimit());
        return mapper.toResponse(account);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPPORT')")
    @Transactional
    public AccountResponse postTransaction(Long accountId, PostTransactionRequest request) {
        BankAccount account = load(accountId);
        postTransaction(account, request);
        return mapper.toResponse(account);
    }

    private void postTransaction(BankAccount account, PostTransactionRequest request) {
        if (transactionRepository.existsByReference(request.reference())) {
            throw new DuplicateRequestException("Transaction reference already exists: " + request.reference());
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleViolationException("Account is not active");
        }
        if (request.type() == TransactionType.DEBIT && account.getAvailableBalance().compareTo(request.amount()) < 0) {
            throw new BusinessRuleViolationException("Insufficient available balance");
        }
        BigDecimal signedAmount = request.type() == TransactionType.CREDIT ? request.amount() : request.amount().negate();
        if (request.type() == TransactionType.CREDIT || request.type() == TransactionType.DEBIT) {
            account.setLedgerBalance(account.getLedgerBalance().add(signedAmount));
            account.setAvailableBalance(account.getAvailableBalance().add(signedAmount));
        }
        AccountTransaction transaction = AccountTransaction.builder()
                .account(account)
                .type(request.type())
                .amount(request.amount())
                .currency(account.getCurrency())
                .reference(request.reference())
                .description(request.description())
                .postedAt(Instant.now())
                .build();
        account.getTransactions().add(transaction);
        transactionRepository.save(transaction);
        enqueueTransactionEvent(account, transaction);
    }

    private BankAccount load(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));
    }

    private BankAccount loadWithTransactions(Long accountId) {
        return accountRepository.findWithTransactionsById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));
    }

    private void assertOwner(Long customerId, BankAccount account) {
        if (!account.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("Account belongs to another customer");
        }
    }

    private void enqueueAccountEvent(BankAccount account, String eventType, String topic) {
        outboxEventService.enqueue(topic, BankingEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .aggregateType("BankAccount")
                .aggregateId(account.getId().toString())
                .customerId(account.getCustomerId().toString())
                .occurredAt(Instant.now())
                .payload(Map.of("iban", account.getIban(), "currency", account.getCurrency(), "status", account.getStatus().name()))
                .build());
    }

    private void enqueueTransactionEvent(BankAccount account, AccountTransaction transaction) {
        outboxEventService.enqueue(properties.kafka().topics().transactionPosted(), BankingEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventNames.TRANSACTION_POSTED)
                .aggregateType("AccountTransaction")
                .aggregateId(transaction.getReference())
                .customerId(account.getCustomerId().toString())
                .occurredAt(transaction.getPostedAt())
                .payload(Map.of("accountId", account.getId(), "amount", transaction.getAmount(), "type", transaction.getType().name()))
                .build());
    }

    private String nextIban() {
        return "FR76" + Instant.now().toEpochMilli() + UUID.randomUUID().toString().replace("-", "").substring(0, 13).toUpperCase();
    }
}
