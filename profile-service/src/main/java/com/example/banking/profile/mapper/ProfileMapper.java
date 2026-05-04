package com.example.banking.profile.mapper;

import com.example.banking.profile.dto.AddressDTO;
import com.example.banking.profile.dto.ConsentDTO;
import com.example.banking.profile.dto.ProfileResponse;
import com.example.banking.profile.dto.UpdateProfileRequest;
import com.example.banking.profile.entity.Address;
import com.example.banking.profile.entity.CustomerConsent;
import com.example.banking.profile.entity.CustomerProfile;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProfileMapper {

    @Mapping(target = "addresses", expression = "java(toAddressDtos(profile.getAddresses()))")
    @Mapping(target = "consents", expression = "java(toConsentDtos(profile.getConsents()))")
    ProfileResponse toResponse(CustomerProfile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(UpdateProfileRequest request, @MappingTarget CustomerProfile profile);

    Address toAddress(AddressDTO dto);

    AddressDTO toAddressDto(Address address);

    ConsentDTO toConsentDto(CustomerConsent consent);

    List<AddressDTO> toAddressDtos(List<Address> addresses);

    List<ConsentDTO> toConsentDtos(List<CustomerConsent> consents);
}
