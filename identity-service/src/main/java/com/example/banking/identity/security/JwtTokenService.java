package com.example.banking.identity.security;

import com.example.banking.identity.config.IdentityProperties;
import com.example.banking.identity.dto.TokenResponse;
import com.example.banking.identity.entity.Role;
import com.example.banking.identity.entity.UserAccount;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final IdentityProperties properties;

    public TokenResponse issueToken(UserAccount user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.security().jwt().ttl());
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.security().jwt().issuer())
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("mfa", user.isMfaEnabled())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .mfaRequired(user.isMfaEnabled())
                .roles(roles)
                .build();
    }
}
