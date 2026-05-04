package com.example.banking.identity.dto;

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
public class TokenResponse {

    private String accessToken;
    private String tokenType;
    private Instant expiresAt;
    private boolean mfaRequired;
    private Set<String> roles;
}
