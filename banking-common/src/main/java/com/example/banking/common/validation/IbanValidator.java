package com.example.banking.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IbanValidator implements ConstraintValidator<Iban, String> {

    private static final String IBAN_PATTERN = "^[A-Z]{2}[0-9A-Z]{13,32}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && value.replace(" ", "").matches(IBAN_PATTERN);
    }
}
