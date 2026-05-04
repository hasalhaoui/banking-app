package com.example.banking.profile.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;

    @Email(message = "{validation.email.invalid}")
    private String email;

    @Size(max = 40, message = "{validation.phone.size}")
    private String phone;

    @Past(message = "{validation.dateOfBirth.past}")
    private LocalDate dateOfBirth;

    private String nationality;
    private String taxResidencyCountry;

    @Valid
    private List<AddressDTO> addresses;
}
