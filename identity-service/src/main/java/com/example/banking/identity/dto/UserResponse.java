package com.example.banking.identity.dto;

import com.example.banking.common.security.BankingRole;
import com.example.banking.identity.entity.UserStatus;
import java.time.Instant;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private UserStatus status;
    private boolean mfaEnabled;
    private Instant lastLoginAt;
    private Set<BankingRole> roles;
}
