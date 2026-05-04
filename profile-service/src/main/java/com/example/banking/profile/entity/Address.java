package com.example.banking.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "address")
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private CustomerProfile profile;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 180)
    private String line1;

    @Column(length = 180)
    private String line2;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 2)
    private String countryCode;
}
