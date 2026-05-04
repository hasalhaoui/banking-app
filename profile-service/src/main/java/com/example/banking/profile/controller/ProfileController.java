package com.example.banking.profile.controller;

import com.example.banking.common.security.SecurityClaims;
import com.example.banking.profile.dto.ConsentRequest;
import com.example.banking.profile.dto.CreateProfileRequest;
import com.example.banking.profile.dto.KycSubmissionRequest;
import com.example.banking.profile.dto.ProfileResponse;
import com.example.banking.profile.dto.UpdateProfileRequest;
import com.example.banking.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/me")
    public ResponseEntity<ProfileResponse> createMine(@Valid @RequestBody CreateProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.create(SecurityClaims.currentUserId(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMine() {
        return ResponseEntity.ok(profileService.getMine(SecurityClaims.currentUserId()));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMine(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateMine(SecurityClaims.currentUserId(), request));
    }

    @PostMapping("/me/kyc")
    public ResponseEntity<ProfileResponse> submitKyc(@Valid @RequestBody KycSubmissionRequest request) {
        return ResponseEntity.ok(profileService.submitKyc(SecurityClaims.currentUserId(), request));
    }

    @PostMapping("/me/consents")
    public ResponseEntity<ProfileResponse> captureConsent(@Valid @RequestBody ConsentRequest request) {
        return ResponseEntity.ok(profileService.captureConsent(SecurityClaims.currentUserId(), request));
    }

    @GetMapping("/admin/{userId}")
    public ResponseEntity<ProfileResponse> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getByUserId(userId));
    }
}
