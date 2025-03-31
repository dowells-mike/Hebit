package com.hebit.app.util

import android.util.Patterns
import java.util.regex.Pattern

/**
 * Utility class for input validation functions
 */
object ValidationHelper {
    
    /**
     * Validates if the string is a valid email address
     */
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Password strength enum
     */
    enum class PasswordStrength(val score: Int) {
        WEAK(0),
        MEDIUM(1),
        STRONG(2)
    }
    
    /**
     * Checks if password is considered strong
     * 
     * Requirements:
     * - At least 8 characters
     * - Contains at least one uppercase letter
     * - Contains at least one lowercase letter 
     * - Contains at least one number
     * - Contains at least one special character
     */
    fun isStrongPassword(password: String): Boolean {
        val passwordPattern = Pattern.compile(
            "^" +
                    "(?=.*[0-9])" +  // at least 1 digit
                    "(?=.*[a-z])" +  // at least 1 lower case letter
                    "(?=.*[A-Z])" +  // at least 1 upper case letter
                    "(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])" +  // at least 1 special character
                    ".{8,}" +  // at least 8 characters
                    "$"
        )
        return passwordPattern.matcher(password).matches()
    }
    
    /**
     * Determines password strength
     * 
     * @return PasswordStrength indicating password quality
     */
    fun getPasswordStrength(password: String): PasswordStrength {
        if (password.length < 8) {
            return PasswordStrength.WEAK
        }
        
        var score = 0
        
        // Check for lowercase letter
        if (password.matches(Regex(".*[a-z].*"))) {
            score++
        }
        
        // Check for uppercase letter
        if (password.matches(Regex(".*[A-Z].*"))) {
            score++
        }
        
        // Check for digit
        if (password.matches(Regex(".*\\d.*"))) {
            score++
        }
        
        // Check for special character
        if (password.matches(Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"))) {
            score++
        }
        
        return when {
            score < 2 -> PasswordStrength.WEAK
            score < 4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
    
    /**
     * Checks if the name contains only valid characters (letters, spaces, hyphens, apostrophes)
     */
    fun isValidName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z\\s'-]+$"))
    }
} 