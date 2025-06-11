package com.example.simplelogin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SecurityMonitoringService {
    
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    
    // Thresholds for suspicious activity detection
    private static final int MAX_FAILED_LOGINS_PER_IP = 10;
    private static final int MAX_FAILED_LOGINS_PER_USER = 5;
    private static final int TIME_WINDOW_MINUTES = 15;
    private static final int MAX_REGISTRATIONS_PER_IP = 3;
    
    // Tracking maps
    private final ConcurrentHashMap<String, FailedAttemptTracker> ipFailedLogins = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FailedAttemptTracker> userFailedLogins = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FailedAttemptTracker> ipRegistrations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SecurityEvent> recentSecurityEvents = new ConcurrentHashMap<>();
    
    /**
     * Record failed login attempt
     */
    public void recordFailedLogin(String clientIp, String username) {
        LocalDateTime now = LocalDateTime.now();
        
        // Track by IP
        ipFailedLogins.compute(clientIp, (ip, tracker) -> {
            if (tracker == null || isExpired(tracker.getLastAttempt(), now)) {
                tracker = new FailedAttemptTracker(now);
            }
            tracker.increment();
            
            if (tracker.getCount() >= MAX_FAILED_LOGINS_PER_IP) {
                raiseSecurityAlert("SUSPICIOUS_IP_ACTIVITY", 
                    "IP " + ip + " exceeded maximum failed login attempts: " + tracker.getCount());
            }
            
            return tracker;
        });
        
        // Track by username
        if (username != null && !username.isEmpty()) {
            userFailedLogins.compute(username, (user, tracker) -> {
                if (tracker == null || isExpired(tracker.getLastAttempt(), now)) {
                    tracker = new FailedAttemptTracker(now);
                }
                tracker.increment();
                
                if (tracker.getCount() >= MAX_FAILED_LOGINS_PER_USER) {
                    raiseSecurityAlert("SUSPICIOUS_USER_ACTIVITY", 
                        "User " + user + " exceeded maximum failed login attempts: " + tracker.getCount());
                }
                
                return tracker;
            });
        }
    }
    
    /**
     * Record successful login
     */
    public void recordSuccessfulLogin(String clientIp, String username) {
        // Reset failed login counters on successful login
        ipFailedLogins.remove(clientIp);
        userFailedLogins.remove(username);
        
        securityLogger.info("SUCCESSFUL_LOGIN_CLEARED_COUNTERS - IP: {}, Username: {}", clientIp, username);
    }
    
    /**
     * Record registration attempt
     */
    public boolean recordRegistrationAttempt(String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        
        AtomicInteger registrationCount = new AtomicInteger(0);
        
        ipRegistrations.compute(clientIp, (ip, tracker) -> {
            if (tracker == null || isExpired(tracker.getLastAttempt(), now)) {
                tracker = new FailedAttemptTracker(now);
            }
            tracker.increment();
            registrationCount.set(tracker.getCount());
            
            if (tracker.getCount() >= MAX_REGISTRATIONS_PER_IP) {
                raiseSecurityAlert("SUSPICIOUS_REGISTRATION_ACTIVITY", 
                    "IP " + ip + " exceeded maximum registration attempts: " + tracker.getCount());
                return tracker; // Block registration
            }
            
            return tracker;
        });
        
        return registrationCount.get() < MAX_REGISTRATIONS_PER_IP;
    }
    
    /**
     * Check if IP is currently suspicious
     */
    public boolean isSuspiciousIp(String clientIp) {
        FailedAttemptTracker tracker = ipFailedLogins.get(clientIp);
        if (tracker == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        if (isExpired(tracker.getLastAttempt(), now)) {
            ipFailedLogins.remove(clientIp);
            return false;
        }
        
        return tracker.getCount() >= MAX_FAILED_LOGINS_PER_IP;
    }
    
    /**
     * Check if user account is currently locked
     */
    public boolean isUserTemporarilyLocked(String username) {
        FailedAttemptTracker tracker = userFailedLogins.get(username);
        if (tracker == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        if (isExpired(tracker.getLastAttempt(), now)) {
            userFailedLogins.remove(username);
            return false;
        }
        
        return tracker.getCount() >= MAX_FAILED_LOGINS_PER_USER;
    }
    
    /**
     * Raise security alert
     */
    private void raiseSecurityAlert(String alertType, String message) {
        String alertId = alertType + "_" + System.currentTimeMillis();
        SecurityEvent event = new SecurityEvent(alertType, message, LocalDateTime.now());
        
        recentSecurityEvents.put(alertId, event);
        
        securityLogger.warn("SECURITY_ALERT - Type: {}, Message: {}, AlertId: {}", 
            alertType, message, alertId);
        
        // TODO: Integrate with external security monitoring systems
        // TODO: Send notifications to security team
        // TODO: Implement automated response (IP blocking, etc.)
    }
    
    /**
     * Get security metrics for monitoring dashboard
     */
    public SecurityMetrics getSecurityMetrics() {
        cleanupExpiredEntries();
        
        return new SecurityMetrics(
            ipFailedLogins.size(),
            userFailedLogins.size(),
            ipRegistrations.size(),
            recentSecurityEvents.size()
        );
    }
    
    /**
     * Clean up expired entries
     */
    private void cleanupExpiredEntries() {
        LocalDateTime now = LocalDateTime.now();
        
        ipFailedLogins.entrySet().removeIf(entry -> 
            isExpired(entry.getValue().getLastAttempt(), now));
        
        userFailedLogins.entrySet().removeIf(entry -> 
            isExpired(entry.getValue().getLastAttempt(), now));
        
        ipRegistrations.entrySet().removeIf(entry -> 
            isExpired(entry.getValue().getLastAttempt(), now));
        
        recentSecurityEvents.entrySet().removeIf(entry -> 
            isExpired(entry.getValue().getTimestamp(), now));
    }
    
    private boolean isExpired(LocalDateTime lastAttempt, LocalDateTime now) {
        return ChronoUnit.MINUTES.between(lastAttempt, now) > TIME_WINDOW_MINUTES;
    }
    
    // Inner classes
    private static class FailedAttemptTracker {
        private AtomicInteger count = new AtomicInteger(0);
        private volatile LocalDateTime lastAttempt;
        
        FailedAttemptTracker(LocalDateTime timestamp) {
            this.lastAttempt = timestamp;
        }
        
        void increment() {
            count.incrementAndGet();
            lastAttempt = LocalDateTime.now();
        }
        
        int getCount() {
            return count.get();
        }
        
        LocalDateTime getLastAttempt() {
            return lastAttempt;
        }
    }
    
    private static class SecurityEvent {
        private final String type;
        private final String message;
        private final LocalDateTime timestamp;
        
        SecurityEvent(String type, String message, LocalDateTime timestamp) {
            this.type = type;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
    
    public static class SecurityMetrics {
        private final int suspiciousIps;
        private final int lockedUsers;
        private final int registrationAttempts;
        private final int recentAlerts;
        
        SecurityMetrics(int suspiciousIps, int lockedUsers, int registrationAttempts, int recentAlerts) {
            this.suspiciousIps = suspiciousIps;
            this.lockedUsers = lockedUsers;
            this.registrationAttempts = registrationAttempts;
            this.recentAlerts = recentAlerts;
        }
        
        // Getters
        public int getSuspiciousIps() { return suspiciousIps; }
        public int getLockedUsers() { return lockedUsers; }
        public int getRegistrationAttempts() { return registrationAttempts; }
        public int getRecentAlerts() { return recentAlerts; }
    }
} 