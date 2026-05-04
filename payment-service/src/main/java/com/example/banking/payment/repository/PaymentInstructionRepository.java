package com.example.banking.payment.repository;

import com.example.banking.payment.entity.PaymentInstruction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentInstructionRepository extends JpaRepository<PaymentInstruction, Long> {

    @EntityGraph(attributePaths = "beneficiary")
    Optional<PaymentInstruction> findWithBeneficiaryById(Long id);

    @EntityGraph(attributePaths = "beneficiary")
    Optional<PaymentInstruction> findByCustomerIdAndIdempotencyKey(Long customerId, String idempotencyKey);

    @EntityGraph(attributePaths = "beneficiary")
    List<PaymentInstruction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
