package com.example.banking.profile.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentDTO {

    private Long id;
    private String consentType;
    private boolean granted;
    private Instant capturedAt;
}
