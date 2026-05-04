package com.example.banking.profile.service;

import com.example.banking.common.error.BusinessRuleViolationException;
import com.example.banking.common.error.EntityNotFoundException;
import com.example.banking.common.kafka.BankingEvent;
import com.example.banking.common.kafka.EventNames;
import com.example.banking.profile.dto.ConsentRequest;
import com.example.banking.profile.dto.CreateProfileRequest;
import com.example.banking.profile.dto.KycSubmissionRequest;
import com.example.banking.profile.dto.ProfileResponse;
import com.example.banking.profile.dto.UpdateProfileRequest;
import com.example.banking.profile.entity.Address;
import com.example.banking.profile.entity.CustomerConsent;
import com.example.banking.profile.entity.CustomerProfile;
import com.example.banking.profile.entity.KycStatus;
import com.example.banking.profile.entity.RiskRating;
import com.example.banking.profile.kafka.ProfileEventPublisher;
import com.example.banking.profile.mapper.ProfileMapper;
import com.example.banking.profile.repository.CustomerProfileRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CustomerProfileRepository repository;
    private final ProfileMapper mapper;
    private final ProfileEventPublisher eventPublisher;

    @Transactional
    public ProfileResponse create(Long userId, CreateProfileRequest request) {
        if (repository.existsByUserId(userId)) {
            throw new BusinessRuleViolationException("Profile already exists for user: " + userId);
        }
        CustomerProfile profile = CustomerProfile.builder()
                .userId(userId)
                .customerNumber(nextCustomerNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .nationality(request.getNationality())
                .taxResidencyCountry(request.getTaxResidencyCountry())
                .kycStatus(KycStatus.NOT_STARTED)
                .riskRating(RiskRating.MEDIUM)
                .build();
        if (request.getAddresses() != null) {
            request.getAddresses().forEach(dto -> {
                Address address = mapper.toAddress(dto);
                address.setProfile(profile);
                profile.getAddresses().add(address);
            });
        }
        return mapper.toResponse(repository.save(profile));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getMine(Long userId) {
        return mapper.toResponse(loadByUserId(userId));
    }

    @Transactional
    public ProfileResponse updateMine(Long userId, UpdateProfileRequest request) {
        CustomerProfile profile = loadByUserId(userId);
        mapper.update(request, profile);
        if (request.getAddresses() != null) {
            profile.getAddresses().clear();
            request.getAddresses().forEach(dto -> {
                Address address = mapper.toAddress(dto);
                address.setProfile(profile);
                profile.getAddresses().add(address);
            });
        }
        return mapper.toResponse(profile);
    }

    @Transactional
    public ProfileResponse submitKyc(Long userId, KycSubmissionRequest request) {
        CustomerProfile profile = loadByUserId(userId);
        profile.setKycStatus(KycStatus.PENDING_REVIEW);
        eventPublisher.publishKycSubmitted(BankingEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventNames.PROFILE_KYC_SUBMITTED)
                .aggregateType("CustomerProfile")
                .aggregateId(profile.getId().toString())
                .customerId(profile.getUserId().toString())
                .occurredAt(Instant.now())
                .payload(Map.of("documentType", request.getDocumentType(), "documentReference", request.getDocumentReference()))
                .build());
        return mapper.toResponse(profile);
    }

    @Transactional
    public ProfileResponse captureConsent(Long userId, ConsentRequest request) {
        CustomerProfile profile = loadByUserId(userId);
        profile.getConsents().add(CustomerConsent.builder()
                .profile(profile)
                .consentType(request.consentType())
                .granted(request.granted())
                .capturedAt(Instant.now())
                .build());
        return mapper.toResponse(profile);
    }

    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE','SUPPORT')")
    @Transactional(readOnly = true)
    public ProfileResponse getByUserId(Long userId) {
        return mapper.toResponse(loadByUserId(userId));
    }

    private CustomerProfile loadByUserId(Long userId) {
        return repository.findWithAddressesAndConsentsByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user: " + userId));
    }

    private String nextCustomerNumber() {
        return "CUST-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
