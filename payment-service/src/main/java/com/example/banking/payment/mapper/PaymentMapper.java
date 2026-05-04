package com.example.banking.payment.mapper;

import com.example.banking.payment.dto.BeneficiaryResponse;
import com.example.banking.payment.dto.PaymentResponse;
import com.example.banking.payment.entity.Beneficiary;
import com.example.banking.payment.entity.PaymentInstruction;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    BeneficiaryResponse toBeneficiaryResponse(Beneficiary beneficiary);

    List<BeneficiaryResponse> toBeneficiaryResponses(List<Beneficiary> beneficiaries);

    @Mapping(source = "beneficiary", target = "beneficiary")
    PaymentResponse toPaymentResponse(PaymentInstruction payment);

    List<PaymentResponse> toPaymentResponses(List<PaymentInstruction> payments);
}
