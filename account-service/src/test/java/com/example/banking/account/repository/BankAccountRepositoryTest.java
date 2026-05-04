package com.example.banking.account.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.banking.account.entity.AccountStatus;
import com.example.banking.account.entity.AccountType;
import com.example.banking.account.entity.BankAccount;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@DataJpaTest
@ActiveProfiles("tst")
@Testcontainers(disabledWithoutDocker = true)
class BankAccountRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
            .withDatabaseName("account_test")
            .withUsername("account")
            .withPassword("account");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private BankAccountRepository repository;

    @Test
    void findsAccountsForCustomer() {
        repository.save(BankAccount.builder()
                .customerId(42L)
                .iban("FR7630006000011234567890189")
                .type(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .currency("EUR")
                .ledgerBalance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .dailyTransferLimit(new BigDecimal("5000.00"))
                .build());

        assertThat(repository.findByCustomerIdOrderByCreatedAtDesc(42L)).hasSize(1);
    }
}
