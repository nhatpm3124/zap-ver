package com.example.simplelogin.security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TwoFactorAuthService {
    
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRATION_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    
    private final ConcurrentHashMap<String, TwoFactorCode> activeCodes = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Generate 2FA code for user
     */
    public String generateTwoFactorCode(String identifier) {
        String code = generateRandomCode();
        LocalDateTime expiration = LocalDateTime.now().plus(CODE_EXPIRATION_MINUTES, ChronoUnit.MINUTES);
        
        activeCodes.put(identifier, new TwoFactorCode(code, expiration, 0));
        
        // Clean up expired codes
        cleanupExpiredCodes();
        
        return code;
    }
    
    /**
     * Verify 2FA code
     */
    public TwoFactorVerificationResult verifyTwoFactorCode(String identifier, String code) {
        TwoFactorCode storedCode = activeCodes.get(identifier);
        
        if (storedCode == null) {
            return new TwoFactorVerificationResult(false, "No active code found");
        }
        
        if (LocalDateTime.now().isAfter(storedCode.expiration)) {
            activeCodes.remove(identifier);
            return new TwoFactorVerificationResult(false, "Code has expired");
        }
        
        if (storedCode.attempts >= MAX_ATTEMPTS) {
            activeCodes.remove(identifier);
            return new TwoFactorVerificationResult(false, "Too many failed attempts");
        }
        
        if (storedCode.code.equals(code)) {
            activeCodes.remove(identifier); // Remove used code
            return new TwoFactorVerificationResult(true, "Code verified successfully");
        } else {
            // Increment attempt count
            activeCodes.put(identifier, new TwoFactorCode(
                storedCode.code, 
                storedCode.expiration, 
                storedCode.attempts + 1
            ));
            return new TwoFactorVerificationResult(false, "Invalid code");
        }
    }
    
    /**
     * Check if user has active 2FA code
     */
    public boolean hasActiveTwoFactorCode(String identifier) {
        TwoFactorCode code = activeCodes.get(identifier);
        if (code == null) return false;
        
        if (LocalDateTime.now().isAfter(code.expiration)) {
            activeCodes.remove(identifier);
            return false;
        }
        
        return true;
    }
    
    /**
     * Invalidate 2FA code
     */
    public void invalidateTwoFactorCode(String identifier) {
        activeCodes.remove(identifier);
    }
    
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }
    
    private void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        activeCodes.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiration));
    }
    
    /**
     * Get active codes count (for monitoring)
     */
    public int getActiveCodesCount() {
        cleanupExpiredCodes();
        return activeCodes.size();
    }
    
    // Inner classes
    private static class TwoFactorCode {
        final String code;
        final LocalDateTime expiration;
        final int attempts;
        
        TwoFactorCode(String code, LocalDateTime expiration, int attempts) {
            this.code = code;
            this.expiration = expiration;
            this.attempts = attempts;
        }
    }
    
    public static class TwoFactorVerificationResult {
        private final boolean valid;
        private final String message;
        
        public TwoFactorVerificationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
} 