package com.example.banking.profile.repository;

import com.example.banking.profile.entity.CustomerProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {

    @EntityGraph(attributePaths = {"addresses", "consents"})
    Optional<CustomerProfile> findWithAddressesAndConsentsByUserId(Long userId);

    Optional<CustomerProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
