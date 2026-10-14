package com.printxpress.app.util

object Validation {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val phoneRegex = Regex("^(?:\\+94|0)?7\\d{8}$")

    fun isValidEmail(value: String): Boolean = emailRegex.matches(value.trim())

    fun isValidPhone(value: String): Boolean {
        val normalized = value.replace(" ", "").replace("-", "")
        return phoneRegex.matches(normalized)
    }

    fun isValidPassword(value: String): Boolean = value.length >= 6

    fun isValidName(value: String): Boolean = value.trim().length >= 2

    fun isValidQuantity(value: String): Boolean {
        val qty = value.toIntOrNull() ?: return false
        return qty in 1..10000
    }

    fun isNonBlank(value: String): Boolean = value.trim().isNotEmpty()

    fun normalizePhone(value: String): String = value.replace(" ", "").replace("-", "")

    fun passwordHash(password: String): String {
        // Coursework-local store — not a production crypto scheme.
        return password.trim().toByteArray().joinToString("") { "%02x".format(it) }
    }
}
