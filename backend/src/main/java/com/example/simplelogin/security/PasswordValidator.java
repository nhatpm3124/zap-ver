package com.example.simplelogin.security;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordValidator {
    
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    private static final Pattern CONSECUTIVE_CHARS = Pattern.compile("(.)\\1{2,}");
    
    private static final List<String> COMMON_PASSWORDS = Arrays.asList(
        "password", "123456", "123456789", "qwerty", "abc123", 
        "password123", "admin", "letmein", "welcome", "monkey"
    );
    
    public PasswordValidationResult validatePassword(String password) {
        List<String> errors = new ArrayList<>();
        
        if (password == null || password.isEmpty()) {
            errors.add("Password cannot be empty");
            return new PasswordValidationResult(false, errors);
        }
        
        // Length validation
        if (password.length() < MIN_LENGTH) {
            errors.add("Password must be at least " + MIN_LENGTH + " characters long");
        }
        
        if (password.length() > MAX_LENGTH) {
            errors.add("Password must not exceed " + MAX_LENGTH + " characters");
        }
        
        // Character requirements
        if (!UPPERCASE.matcher(password).find()) {
            errors.add("Password must contain at least one uppercase letter");
        }
        
        if (!LOWERCASE.matcher(password).find()) {
            errors.add("Password must contain at least one lowercase letter");
        }
        
        if (!DIGIT.matcher(password).find()) {
            errors.add("Password must contain at least one digit");
        }
        
        if (!SPECIAL.matcher(password).find()) {
            errors.add("Password must contain at least one special character");
        }
        
        // No whitespace
        if (WHITESPACE.matcher(password).find()) {
            errors.add("Password must not contain whitespace characters");
        }
        
        // No consecutive repeated characters
        if (CONSECUTIVE_CHARS.matcher(password).find()) {
            errors.add("Password must not contain more than 2 consecutive identical characters");
        }
        
        // Check against common passwords
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            errors.add("Password is too common, please choose a stronger password");
        }
        
        return new PasswordValidationResult(errors.isEmpty(), errors);
    }
    
    public static class PasswordValidationResult {
        private final boolean valid;
        private final List<String> messages;
        
        public PasswordValidationResult(boolean valid, List<String> messages) {
            this.valid = valid;
            this.messages = messages;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getMessages() {
            return messages;
        }
        
        public String getErrorMessage() {
            if (valid) {
                return null;
            }
            return "Password does not meet security requirements: " + String.join(", ", messages);
        }
    }
} 