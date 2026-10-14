package com.printxpress.app.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidationTest {
    @Test
    public void emailValidation() {
        assertTrue(Validation.isValidEmail("customer@example.lk"));
        assertFalse(Validation.isValidEmail("bad@"));
        assertFalse(Validation.isValidEmail(""));
    }

    @Test
    public void phoneValidation() {
        assertTrue(Validation.isValidPhone("0771234567"));
        assertTrue(Validation.isValidPhone("+94771234567"));
        assertFalse(Validation.isValidPhone("0112345678"));
    }

    @Test
    public void passwordAndQuantity() {
        assertTrue(Validation.isValidPassword("secret1"));
        assertFalse(Validation.isValidPassword("123"));
        assertTrue(Validation.isValidQuantity("100"));
        assertFalse(Validation.isValidQuantity("0"));
    }
}
