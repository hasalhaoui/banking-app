package com.example.banking.payment.repository;

import com.example.banking.payment.entity.Beneficiary;
import com.example.banking.payment.entity.BeneficiaryStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, BeneficiaryStatus status);
}
