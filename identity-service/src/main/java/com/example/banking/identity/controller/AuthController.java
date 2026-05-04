package com.example.banking.identity.controller;

import com.example.banking.identity.dto.LoginRequest;
import com.example.banking.identity.dto.RegisterCustomerRequest;
import com.example.banking.identity.dto.TokenResponse;
import com.example.banking.identity.dto.UserResponse;
import com.example.banking.identity.service.AuthenticationService;
import com.example.banking.identity.service.UserManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserManagementService userManagementService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userManagementService.registerCustomer(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authenticationService.login(request, servletRequest));
    }
}
