package com.example.banking.common.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class SecurityClaims {

    private SecurityClaims() {
    }

    public static Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated principal found");
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            Object claim = jwt.getClaim("userId");
            if (claim instanceof Number number) {
                return number.longValue();
            }
            if (claim instanceof String value) {
                return Long.parseLong(value);
            }
        }
        return Long.parseLong(authentication.getName());
    }

    public static Optional<String> currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getClaimAsString("email"));
        }
        return Optional.empty();
    }
}
