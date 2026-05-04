package com.example.banking.profile.dto;

import com.example.banking.profile.entity.KycStatus;
import com.example.banking.profile.entity.RiskRating;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private Long id;
    private Long userId;
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String nationality;
    private KycStatus kycStatus;
    private RiskRating riskRating;
    private String taxResidencyCountry;
    private List<AddressDTO> addresses;
    private List<ConsentDTO> consents;
}
