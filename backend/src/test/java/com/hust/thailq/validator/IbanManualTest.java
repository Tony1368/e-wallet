package com.hust.thailq.validator;

import com.hust.thailq.common.Constants;
import java.math.BigInteger;

public class IbanManualTest {
    
    public static void main(String[] args) {
        testIban("AL35202111090000000001234567", "Albania");
        testIban("AD1400080001001234567890", "Andorra");
        testIban("DE89370400440532013000", "Germany");
        testIban("FR1420041010050500013M02606", "France");
    }
    
    private static void testIban(String iban, String country) {
        System.out.println("Testing " + country + " IBAN: " + iban);
        
        if (iban == null || iban.trim().isEmpty()) {
            System.out.println("  Result: INVALID (null or empty)");
            return;
        }

        String trimmed = iban.trim().toUpperCase();
        System.out.println("  Trimmed: " + trimmed + " (length: " + trimmed.length() + ")");
        
        if (trimmed.length() < Constants.IBAN_MIN_SIZE || trimmed.length() > Constants.IBAN_MAX_SIZE) {
            System.out.println("  Result: INVALID (length: " + trimmed.length() + ", min: " + Constants.IBAN_MIN_SIZE + ", max: " + Constants.IBAN_MAX_SIZE + ")");
            return;
        }
        
        // Move first 4 characters to end
        String reformat = trimmed.substring(4) + trimmed.substring(0, 4);
        System.out.println("  Reformatted: " + reformat);
        
        // Convert letters to numbers (A=10, B=11, ..., Z=35)
        StringBuilder numericString = new StringBuilder();
        for (char c : reformat.toCharArray()) {
            if (Character.isLetter(c)) {
                int value = Character.getNumericValue(c);
                numericString.append(value);
                System.out.println("    " + c + " -> " + value);
            } else if (Character.isDigit(c)) {
                numericString.append(c);
                System.out.println("    " + c + " -> " + c);
            } else {
                System.out.println("  Result: INVALID (invalid character: " + c + ")");
                return;
            }
        }
        
        String numericIban = numericString.toString();
        System.out.println("  Numeric IBAN: " + numericIban);
        
        // Use BigInteger for large number calculations
        BigInteger ibanNumber = new BigInteger(numericIban);
        BigInteger modulus = BigInteger.valueOf(Constants.IBAN_MODULUS);
        BigInteger remainder = ibanNumber.mod(modulus);
        
        boolean isValid = remainder.equals(BigInteger.ONE);
        System.out.println("  Result: " + (isValid ? "VALID" : "INVALID") + " (remainder: " + remainder + ")");
        System.out.println();
    }
} 