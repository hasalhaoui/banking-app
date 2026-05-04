package com.example.banking.identity.service;

import com.example.banking.common.error.BusinessRuleViolationException;
import com.example.banking.common.error.EntityNotFoundException;
import com.example.banking.common.kafka.BankingEvent;
import com.example.banking.common.kafka.EventNames;
import com.example.banking.common.security.BankingRole;
import com.example.banking.identity.dto.MfaSettingsRequest;
import com.example.banking.identity.dto.RegisterCustomerRequest;
import com.example.banking.identity.dto.UserResponse;
import com.example.banking.identity.entity.Role;
import com.example.banking.identity.entity.UserAccount;
import com.example.banking.identity.entity.UserStatus;
import com.example.banking.identity.kafka.IdentityEventPublisher;
import com.example.banking.identity.mapper.UserMapper;
import com.example.banking.identity.repository.RoleRepository;
import com.example.banking.identity.repository.UserAccountRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final IdentityEventPublisher eventPublisher;

    @Transactional
    public UserResponse registerCustomer(RegisterCustomerRequest request) {
        if (userAccountRepository.existsByEmail(request.getEmail()) || userAccountRepository.existsByUsername(request.getUsername())) {
            throw new BusinessRuleViolationException("User already exists");
        }
        Role customerRole = roleRepository.findByName(BankingRole.CUSTOMER)
                .orElseThrow(() -> new EntityNotFoundException("CUSTOMER role is not configured"));
        UserAccount user = UserAccount.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .mfaEnabled(true)
                .roles(Set.of(customerRole))
                .build();
        UserAccount saved = userAccountRepository.save(user);
        eventPublisher.publishCustomerRegistered(BankingEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventNames.CUSTOMER_REGISTERED)
                .aggregateType("UserAccount")
                .aggregateId(saved.getId().toString())
                .customerId(saved.getId().toString())
                .occurredAt(Instant.now())
                .payload(Map.of("email", saved.getEmail(), "username", saved.getUsername()))
                .build());
        return userMapper.toResponse(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userAccountRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse updateRoles(Long userId, Set<BankingRole> roles) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Set<Role> resolvedRoles = roles.stream()
                .map(role -> roleRepository.findByName(role)
                        .orElseThrow(() -> new EntityNotFoundException("Role not found: " + role)))
                .collect(Collectors.toSet());
        user.setRoles(resolvedRoles);
        return userMapper.toResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse lockUser(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        user.setStatus(UserStatus.LOCKED);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateMfa(Long userId, MfaSettingsRequest request) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        user.setMfaEnabled(request.enabled());
        return userMapper.toResponse(user);
    }
}
