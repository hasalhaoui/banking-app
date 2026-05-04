package com.example.banking.profile.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsentRequest(@NotBlank(message = "{validation.consentType.required}") String consentType,
                             boolean granted) {
}
