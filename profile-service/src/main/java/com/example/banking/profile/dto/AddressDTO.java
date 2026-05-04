package com.example.banking.profile.dto;

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
public class AddressDTO {

    private Long id;

    @NotBlank(message = "{validation.address.type.required}")
    private String type;

    @NotBlank(message = "{validation.address.line1.required}")
    @Size(max = 180, message = "{validation.address.line1.size}")
    private String line1;

    @Size(max = 180, message = "{validation.address.line2.size}")
    private String line2;

    @NotBlank(message = "{validation.address.city.required}")
    private String city;

    @NotBlank(message = "{validation.address.postalCode.required}")
    private String postalCode;

    @NotBlank(message = "{validation.address.countryCode.required}")
    @Size(min = 2, max = 2, message = "{validation.address.countryCode.size}")
    private String countryCode;
}
