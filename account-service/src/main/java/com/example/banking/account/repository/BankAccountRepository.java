package com.example.banking.account.repository;

import com.example.banking.account.entity.BankAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    @EntityGraph(attributePaths = "transactions")
    Optional<BankAccount> findWithTransactionsById(Long id);

    List<BankAccount> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<BankAccount> findByIban(String iban);
}
