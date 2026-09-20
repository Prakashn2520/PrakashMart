package com.prakash.prakashmart.util;

import com.prakash.prakashmart.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    @DisplayName("Should validate correct and incorrect email addresses")
    void testEmailValidation() {
        assertTrue(ValidationUtil.isValidEmail("user@example.com"));
        assertTrue(ValidationUtil.isValidEmail("prakash.mart@store.org"));
        assertFalse(ValidationUtil.isValidEmail("invalid-email"));
        assertFalse(ValidationUtil.isValidEmail("@domain.com"));
        assertFalse(ValidationUtil.isValidEmail(null));

        assertDoesNotThrow(() -> ValidationUtil.validateEmail("user@example.com"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("invalid-email"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail(""));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail(null));
    }

    @Test
    @DisplayName("Should validate password rules (min 8 chars, letter + digit)")
    void testPasswordValidation() {
        assertTrue(ValidationUtil.isValidPassword("Pass1234"));
        assertTrue(ValidationUtil.isValidPassword("SecureP@ssw0rd"));
        assertFalse(ValidationUtil.isValidPassword("short1"));        // too short
        assertFalse(ValidationUtil.isValidPassword("onlyletters"));   // no digit
        assertFalse(ValidationUtil.isValidPassword("12345678"));      // no letter
        assertFalse(ValidationUtil.isValidPassword(null));

        assertDoesNotThrow(() -> ValidationUtil.validatePassword("ValidPass1"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePassword("weak"));
    }

    @Test
    @DisplayName("Should validate non-empty string and required fields")
    void testRequiredAndNotEmpty() {
        assertTrue(ValidationUtil.isNotEmpty("hello"));
        assertFalse(ValidationUtil.isNotEmpty(""));
        assertFalse(ValidationUtil.isNotEmpty("   "));
        assertFalse(ValidationUtil.isNotEmpty(null));

        assertDoesNotThrow(() -> ValidationUtil.validateRequired("value", "fieldName"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRequired("", "fieldName"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRequired(null, "fieldName"));
    }

    @Test
    @DisplayName("Should validate positive and non-negative numbers")
    void testNumberValidation() {
        assertTrue(ValidationUtil.isPositive(new BigDecimal("10.50")));
        assertFalse(ValidationUtil.isPositive(BigDecimal.ZERO));
        assertFalse(ValidationUtil.isPositive(new BigDecimal("-5.00")));
        assertFalse(ValidationUtil.isPositive((BigDecimal) null));
        assertFalse(ValidationUtil.isPositive((Integer) null));

        assertTrue(ValidationUtil.isNonNegative(BigDecimal.ZERO));
        assertTrue(ValidationUtil.isNonNegative(new BigDecimal("100.00")));
        assertFalse(ValidationUtil.isNonNegative(new BigDecimal("-1.00")));
        assertFalse(ValidationUtil.isNonNegative((BigDecimal) null));

        assertDoesNotThrow(() -> ValidationUtil.validatePrice(new BigDecimal("9.99")));
        assertDoesNotThrow(() -> ValidationUtil.validatePrice(BigDecimal.ZERO));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePrice(new BigDecimal("-1.00")));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePrice(null));

        assertDoesNotThrow(() -> ValidationUtil.validateStockQuantity(0));
        assertDoesNotThrow(() -> ValidationUtil.validateStockQuantity(50));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateStockQuantity(-1));
    }

    @Test
    @DisplayName("Should validate product details, rating, and cart quantity")
    void testDomainSpecificValidations() {
        assertDoesNotThrow(() -> ValidationUtil.validateProductName("Laptop Stand"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateProductName("A"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateProductName(""));

        assertDoesNotThrow(() -> ValidationUtil.validateCategory("Electronics"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateCategory(""));

        assertDoesNotThrow(() -> ValidationUtil.validateCartQuantity(1));
        assertDoesNotThrow(() -> ValidationUtil.validateCartQuantity(10));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateCartQuantity(0));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateCartQuantity(-5));

        assertDoesNotThrow(() -> ValidationUtil.validateRating(1));
        assertDoesNotThrow(() -> ValidationUtil.validateRating(5));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRating(0));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateRating(6));

        assertDoesNotThrow(() -> ValidationUtil.validateId(1L, "productId"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateId(0L, "productId"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateId(-1L, "productId"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateId(null, "productId"));
    }

    @Test
    @DisplayName("Should validate pagination parameters and sanitize keywords")
    void testPaginationAndSearchSanitization() {
        assertEquals(1, ValidationUtil.validatePage(null));
        assertEquals(1, ValidationUtil.validatePage(0));
        assertEquals(3, ValidationUtil.validatePage(3));

        assertEquals(12, ValidationUtil.validateLimit(null));
        assertEquals(12, ValidationUtil.validateLimit(0));
        assertEquals(20, ValidationUtil.validateLimit(20));
        assertEquals(100, ValidationUtil.validateLimit(500)); // capped at 100

        assertEquals("", ValidationUtil.sanitizeSearchKeyword(null));
        assertEquals("keyboard", ValidationUtil.sanitizeSearchKeyword("  keyboard  "));
    }
}
