package com.example.banking.payment.dto;

import com.example.banking.payment.entity.BeneficiaryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryResponse {

    private Long id;
    private String nickname;
    private String accountHolderName;
    private String iban;
    private String bic;
    private String countryCode;
    private BeneficiaryStatus status;
}
