package com.example.banking.identity.controller;

import com.example.banking.identity.dto.MfaSettingsRequest;
import com.example.banking.identity.dto.UpdateUserRolesRequest;
import com.example.banking.identity.dto.UserResponse;
import com.example.banking.identity.service.UserManagementService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userManagementService.listUsers());
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<UserResponse> updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
        return ResponseEntity.ok(userManagementService.updateRoles(id, request.roles()));
    }

    @PatchMapping("/{id}/lock")
    public ResponseEntity<UserResponse> lock(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.lockUser(id));
    }

    @PatchMapping("/{id}/mfa")
    public ResponseEntity<UserResponse> updateMfa(@PathVariable Long id, @RequestBody MfaSettingsRequest request) {
        return ResponseEntity.ok(userManagementService.updateMfa(id, request));
    }
}
