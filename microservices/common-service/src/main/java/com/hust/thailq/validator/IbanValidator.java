package com.hust.thailq.validator;

import com.hust.thailq.common.Constants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigInteger;

public class IbanValidator implements ConstraintValidator<ValidIban, String> {

    @Override
    public boolean isValid(String iban, ConstraintValidatorContext context) {
        if (iban == null || iban.trim().isEmpty()) {
            return false;
        }

        String trimmed = iban.trim().toUpperCase();

        if (trimmed.length() < Constants.IBAN_MIN_SIZE || trimmed.length() > Constants.IBAN_MAX_SIZE) {
            return false;
        }

        String reformat = trimmed.substring(4) + trimmed.substring(0, 4);

        StringBuilder numericString = new StringBuilder();
        for (char c : reformat.toCharArray()) {
            if (Character.isLetter(c)) {
                numericString.append(Character.getNumericValue(c));
            } else if (Character.isDigit(c)) {
                numericString.append(c);
            } else {
                return false;
            }
        }

        BigInteger ibanNumber = new BigInteger(numericString.toString());
        BigInteger modulus = BigInteger.valueOf(Constants.IBAN_MODULUS);

        return ibanNumber.mod(modulus).equals(BigInteger.ONE);
    }
}
