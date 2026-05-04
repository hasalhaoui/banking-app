package com.example.banking.identity.mapper;

import com.example.banking.common.security.BankingRole;
import com.example.banking.identity.dto.UserResponse;
import com.example.banking.identity.entity.Role;
import com.example.banking.identity.entity.UserAccount;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(toRoleNames(user.getRoles()))")
    UserResponse toResponse(UserAccount user);

    default Set<BankingRole> toRoleNames(Set<Role> roles) {
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
