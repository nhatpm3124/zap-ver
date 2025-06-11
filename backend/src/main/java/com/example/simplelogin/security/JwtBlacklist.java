package com.example.simplelogin.security;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtBlacklist {
    
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Date> tokenExpirations = new ConcurrentHashMap<>();
    
    /**
     * Add token to blacklist
     */
    public void blacklistToken(String token, Date expiration) {
        blacklistedTokens.add(token);
        tokenExpirations.put(token, expiration);
    }
    
    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        cleanupExpiredTokens();
        return blacklistedTokens.contains(token);
    }
    
    /**
     * Clean up expired tokens from blacklist to prevent memory leak
     */
    private void cleanupExpiredTokens() {
        Date now = new Date();
        tokenExpirations.entrySet().removeIf(entry -> {
            if (entry.getValue().before(now)) {
                blacklistedTokens.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Get total blacklisted tokens count (for monitoring)
     */
    public int getBlacklistedTokensCount() {
        cleanupExpiredTokens();
        return blacklistedTokens.size();
    }
    
    /**
     * Force cleanup of all expired tokens
     */
    public void forceCleanup() {
        cleanupExpiredTokens();
    }
} 