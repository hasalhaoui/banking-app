package com.example.banking.profile.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
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
public class CreateProfileRequest {

    @NotBlank(message = "{validation.firstName.required}")
    private String firstName;

    @NotBlank(message = "{validation.lastName.required}")
    private String lastName;

    @Email(message = "{validation.email.invalid}")
    @NotBlank(message = "{validation.email.required}")
    private String email;

    @Size(max = 40, message = "{validation.phone.size}")
    private String phone;

    @Past(message = "{validation.dateOfBirth.past}")
    private LocalDate dateOfBirth;

    @NotBlank(message = "{validation.nationality.required}")
    private String nationality;

    private String taxResidencyCountry;

    @Valid
    private List<AddressDTO> addresses;
}
