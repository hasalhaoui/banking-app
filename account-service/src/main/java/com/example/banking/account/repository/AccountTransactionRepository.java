package com.example.banking.account.repository;

import com.example.banking.account.entity.AccountTransaction;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    List<AccountTransaction> findByAccountIdAndPostedAtBetweenOrderByPostedAtDesc(Long accountId, Instant from, Instant to);

    boolean existsByReference(String reference);
}
