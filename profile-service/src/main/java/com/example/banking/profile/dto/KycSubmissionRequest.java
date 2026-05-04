package com.example.banking.profile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycSubmissionRequest {

    @NotBlank(message = "{validation.kyc.documentType.required}")
    private String documentType;

    @NotBlank(message = "{validation.kyc.documentReference.required}")
    private String documentReference;
}
