package com.printxpress.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {
    @Test
    fun emailValidation() {
        assertTrue(Validation.isValidEmail("customer@example.lk"))
        assertFalse(Validation.isValidEmail("bad@"))
        assertFalse(Validation.isValidEmail(""))
    }

    @Test
    fun phoneValidation() {
        assertTrue(Validation.isValidPhone("0771234567"))
        assertTrue(Validation.isValidPhone("+94771234567"))
        assertFalse(Validation.isValidPhone("0112345678"))
    }

    @Test
    fun passwordAndQuantity() {
        assertTrue(Validation.isValidPassword("secret1"))
        assertFalse(Validation.isValidPassword("123"))
        assertTrue(Validation.isValidQuantity("100"))
        assertFalse(Validation.isValidQuantity("0"))
    }
}
