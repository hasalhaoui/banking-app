package com.example.banking.identity.service;

import com.example.banking.identity.config.IdentityProperties;
import com.example.banking.identity.dto.LoginRequest;
import com.example.banking.identity.dto.TokenResponse;
import com.example.banking.identity.entity.UserAccount;
import com.example.banking.identity.entity.UserStatus;
import com.example.banking.identity.repository.UserAccountRepository;
import com.example.banking.identity.security.JwtTokenService;
import com.example.banking.identity.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final JwtTokenService jwtTokenService;
    private final LoginAuditService loginAuditService;
    private final IdentityProperties properties;

    @Transactional
    public TokenResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            UserAccount user = resolveUser(authentication, request.getUsername());
            user.setFailedLoginAttempts(0);
            user.setLastLoginAt(Instant.now());
            userAccountRepository.save(user);
            loginAuditService.record(request.getUsername(), true, servletRequest.getRemoteAddr(),
                    servletRequest.getHeader("User-Agent"), null);
            return jwtTokenService.issueToken(user);
        } catch (RuntimeException exception) {
            userAccountRepository.findByUsername(request.getUsername())
                    .or(() -> userAccountRepository.findByEmail(request.getUsername()))
                    .ifPresent(user -> registerFailedLogin(user, exception.getMessage()));
            loginAuditService.record(request.getUsername(), false, servletRequest.getRemoteAddr(),
                    servletRequest.getHeader("User-Agent"), "Bad credentials");
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    private UserAccount resolveUser(Authentication authentication, String username) {
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.user();
        }
        return userAccountRepository.findByUsername(username)
                .or(() -> userAccountRepository.findByEmail(username))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }

    private void registerFailedLogin(UserAccount user, String reason) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= properties.risk().maxFailedLoginAttempts()) {
            user.setStatus(UserStatus.LOCKED);
        }
        userAccountRepository.save(user);
    }
}
