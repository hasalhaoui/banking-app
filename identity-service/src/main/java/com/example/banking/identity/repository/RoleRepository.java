package com.example.banking.identity.repository;

import com.example.banking.common.security.BankingRole;
import com.example.banking.identity.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(BankingRole name);
}
