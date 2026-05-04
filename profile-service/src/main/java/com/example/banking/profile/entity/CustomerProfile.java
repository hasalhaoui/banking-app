package com.example.banking.profile.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_profile")
public class CustomerProfile extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, unique = true, length = 40)
    private String customerNumber;

    @Column(nullable = false, length = 120)
    private String firstName;

    @Column(nullable = false, length = 120)
    private String lastName;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 40)
    private String phone;

    private LocalDate dateOfBirth;

    @Column(length = 80)
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KycStatus kycStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RiskRating riskRating;

    @Column(length = 120)
    private String taxResidencyCountry;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CustomerConsent> consents = new ArrayList<>();
}
