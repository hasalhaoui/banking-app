package com.example.banking.identity.dto;

import com.example.banking.common.security.BankingRole;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateUserRolesRequest(@NotEmpty(message = "{validation.roles.required}") Set<BankingRole> roles) {
}
