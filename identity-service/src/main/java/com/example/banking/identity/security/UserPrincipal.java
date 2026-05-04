package com.example.banking.identity.security;

import com.example.banking.identity.entity.UserAccount;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record UserPrincipal(UserAccount user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != com.example.banking.identity.entity.UserStatus.LOCKED;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == com.example.banking.identity.entity.UserStatus.ACTIVE;
    }
}
