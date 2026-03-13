package com.hust.thailq.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class IbanValidatorTest {

    private IbanValidator ibanValidator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        ibanValidator = new IbanValidator();
    }

    @Test
    void testValidAlbaniaIban() {
        // Albania IBAN: AL35202111090000000001234567
        String albaniaIban = "AL35202111090000000001234567";
        assertTrue(ibanValidator.isValid(albaniaIban, context), "Albania IBAN should be valid");
    }

    @Test
    void testValidAndorraIban() {
        // Andorra IBAN: AD1400080001001234567890
        String andorraIban = "AD1400080001001234567890";
        assertTrue(ibanValidator.isValid(andorraIban, context), "Andorra IBAN should be valid");
    }

    @Test
    void testValidGermanyIban() {
        // Germany IBAN: DE89370400440532013000
        String germanyIban = "DE89370400440532013000";
        assertTrue(ibanValidator.isValid(germanyIban, context), "Germany IBAN should be valid");
    }

    @Test
    void testValidFranceIban() {
        // France IBAN: FR1420041010050500013M02606
        String franceIban = "FR1420041010050500013M02606";
        assertTrue(ibanValidator.isValid(franceIban, context), "France IBAN should be valid");
    }

    @Test
    void testValidNetherlandsIban() {
        // Netherlands IBAN: NL91ABNA0417164300
        String netherlandsIban = "NL91ABNA0417164300";
        assertTrue(ibanValidator.isValid(netherlandsIban, context), "Netherlands IBAN should be valid");
    }

    @Test
    void testValidBelgiumIban() {
        // Belgium IBAN: BE68539007547034
        String belgiumIban = "BE68539007547034";
        assertTrue(ibanValidator.isValid(belgiumIban, context), "Belgium IBAN should be valid");
    }

    @Test
    void testValidItalyIban() {
        // Italy IBAN: IT60X0542811101000000123456
        String italyIban = "IT60X0542811101000000123456";
        assertTrue(ibanValidator.isValid(italyIban, context), "Italy IBAN should be valid");
    }

    @Test
    void testValidSpainIban() {
        // Spain IBAN: ES9121000418450200051332
        String spainIban = "ES9121000418450200051332";
        assertTrue(ibanValidator.isValid(spainIban, context), "Spain IBAN should be valid");
    }

    @Test
    void testInvalidIban() {
        // Invalid IBAN
        String invalidIban = "INVALID123456";
        assertFalse(ibanValidator.isValid(invalidIban, context), "Invalid IBAN should be rejected");
    }

    @Test
    void testEmptyIban() {
        assertFalse(ibanValidator.isValid("", context), "Empty IBAN should be rejected");
    }

    @Test
    void testNullIban() {
        assertFalse(ibanValidator.isValid(null, context), "Null IBAN should be rejected");
    }

    @Test
    void testIbanWithSpaces() {
        // IBAN with spaces should be trimmed and validated
        String ibanWithSpaces = "  DE89370400440532013000  ";
        assertTrue(ibanValidator.isValid(ibanWithSpaces, context), "IBAN with spaces should be valid after trimming");
    }

    @Test
    void testIbanWithLowercase() {
        // IBAN with lowercase should be converted to uppercase and validated
        String lowercaseIban = "de89370400440532013000";
        assertTrue(ibanValidator.isValid(lowercaseIban, context), "Lowercase IBAN should be valid after conversion");
    }

    @Test
    void testUserReportedIbanIssues() {
        // Test the specific IBANs that the user reported as failing
        System.out.println("Testing user-reported IBAN issues:");
        
        String albaniaIban = "AL35202111090000000001234567";
        boolean albaniaValid = ibanValidator.isValid(albaniaIban, context);
        System.out.println("Albania IBAN: " + albaniaIban + " -> " + (albaniaValid ? "VALID" : "INVALID"));
        assertTrue(albaniaValid, "Albania IBAN should be valid");
        
        String andorraIban = "AD1400080001001234567890";
        boolean andorraValid = ibanValidator.isValid(andorraIban, context);
        System.out.println("Andorra IBAN: " + andorraIban + " -> " + (andorraValid ? "VALID" : "INVALID"));
        assertTrue(andorraValid, "Andorra IBAN should be valid");
    }
} 