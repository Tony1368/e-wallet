package com.hust.thailq.validator;

import com.hust.thailq.common.Constants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * Used for validating IBAN numbers.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class IbanValidator implements ConstraintValidator<ValidIban, String> {

    @Override
    public boolean isValid(String iban, ConstraintValidatorContext context) {
        if (iban == null || iban.trim().isEmpty()) {
            log.debug("IBAN is null or empty");
            return false;
        }

        String trimmed = iban.trim().toUpperCase();
        log.debug("Validating IBAN: '{}' (length: {})", trimmed, trimmed.length());
        
        if (trimmed.length() < Constants.IBAN_MIN_SIZE || trimmed.length() > Constants.IBAN_MAX_SIZE) {
            log.debug("IBAN length validation failed: {} (min: {}, max: {})", trimmed.length(), Constants.IBAN_MIN_SIZE, Constants.IBAN_MAX_SIZE);
            return false;
        }
        
        // Move first 4 characters to end
        String reformat = trimmed.substring(4) + trimmed.substring(0, 4);
        log.debug("Reformatted IBAN: {}", reformat);
        
        // Convert letters to numbers (A=10, B=11, ..., Z=35)
        StringBuilder numericString = new StringBuilder();
        for (char c : reformat.toCharArray()) {
            if (Character.isLetter(c)) {
                int value = Character.getNumericValue(c);
                numericString.append(value);
                log.debug("Converted letter '{}' to number: {}", c, value);
            } else if (Character.isDigit(c)) {
                numericString.append(c);
                log.debug("Kept digit '{}' as: {}", c, c);
            } else {
                log.debug("Invalid character in IBAN: '{}'", c);
                return false;
            }
        }
        
        String numericIban = numericString.toString();
        log.debug("Numeric IBAN: {}", numericIban);
        
        // Use BigInteger for large number calculations
        BigInteger ibanNumber = new BigInteger(numericIban);
        BigInteger modulus = BigInteger.valueOf(Constants.IBAN_MODULUS);
        
        boolean isValid = ibanNumber.mod(modulus).equals(BigInteger.ONE);
        log.debug("IBAN validation result: {} (remainder: {})", isValid, ibanNumber.mod(modulus));
        
        return isValid;
    }
}
