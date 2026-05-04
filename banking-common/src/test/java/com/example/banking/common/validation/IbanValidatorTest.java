package com.example.banking.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IbanValidatorTest {

    private final IbanValidator validator = new IbanValidator();

    @Test
    void validatesBasicInternationalIbanShape() {
        assertThat(validator.isValid("FR7630006000011234567890189", null)).isTrue();
        assertThat(validator.isValid("bad-iban", null)).isFalse();
    }
}
