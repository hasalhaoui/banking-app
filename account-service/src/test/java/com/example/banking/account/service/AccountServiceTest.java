package com.example.banking.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.config.AccountProperties;
import com.example.banking.account.dto.AccountResponse;
import com.example.banking.account.dto.OpenAccountRequest;
import com.example.banking.account.entity.AccountStatus;
import com.example.banking.account.entity.AccountType;
import com.example.banking.account.entity.BankAccount;
import com.example.banking.account.kafka.OutboxEventService;
import com.example.banking.account.mapper.AccountMapper;
import com.example.banking.account.repository.AccountTransactionRepository;
import com.example.banking.account.repository.BankAccountRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private BankAccountRepository accountRepository;
    @Mock
    private AccountTransactionRepository transactionRepository;
    @Mock
    private AccountMapper mapper;
    @Mock
    private OutboxEventService outboxEventService;

    private AccountService service;

    @BeforeEach
    void setUp() {
        AccountProperties properties = new AccountProperties(
                new AccountProperties.Security(new AccountProperties.Jwt("issuer", "test-secret-change-me-at-least-32-bytes", Duration.ofHours(1)),
                        List.of("http://localhost")),
                new AccountProperties.Kafka(new AccountProperties.Topics("account-opened", "account-frozen", "transaction-posted"),
                        new AccountProperties.Outbox(Duration.ofSeconds(10), 50, 10)),
                new AccountProperties.Limits("EUR", new BigDecimal("5000.00")));
        service = new AccountService(accountRepository, transactionRepository, mapper, properties, outboxEventService);
    }

    @Test
    void opensActiveAccountWithDefaultLimit() {
        when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> {
            BankAccount account = invocation.getArgument(0);
            account.setId(10L);
            return account;
        });
        when(mapper.toResponse(any(BankAccount.class))).thenReturn(AccountResponse.builder()
                .id(10L)
                .status(AccountStatus.ACTIVE)
                .currency("EUR")
                .dailyTransferLimit(new BigDecimal("5000.00"))
                .build());

        AccountResponse response = service.openAccount(42L, OpenAccountRequest.builder()
                .type(AccountType.CHECKING)
                .build());

        assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.getDailyTransferLimit()).isEqualByComparingTo("5000.00");
        verify(outboxEventService).enqueue(any(), any());
    }
}
