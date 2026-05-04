package com.example.banking.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "beneficiary")
public class Beneficiary extends BaseEntity {

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, length = 120)
    private String nickname;

    @Column(nullable = false, length = 180)
    private String accountHolderName;

    @Column(nullable = false, length = 40)
    private String iban;

    @Column(length = 20)
    private String bic;

    @Column(nullable = false, length = 2)
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BeneficiaryStatus status;
}
