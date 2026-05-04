package com.example.banking.payment.dto;

import com.example.banking.common.validation.Iban;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryRequest {

    @NotBlank(message = "{validation.nickname.required}")
    private String nickname;

    @NotBlank(message = "{validation.accountHolder.required}")
    private String accountHolderName;

    @Iban
    private String iban;

    @Size(max = 20, message = "{validation.bic.size}")
    private String bic;

    @NotBlank(message = "{validation.countryCode.required}")
    @Size(min = 2, max = 2, message = "{validation.countryCode.size}")
    private String countryCode;
}
