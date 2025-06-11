package com.example.simplelogin.controller;

import com.example.simplelogin.security.JwtBlacklist;
import com.example.simplelogin.security.SecurityMonitoringService;
import com.example.simplelogin.security.TwoFactorAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityController {
    
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    @Autowired
    private SecurityMonitoringService securityMonitoringService;

    @Autowired
    private JwtBlacklist jwtBlacklist;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    /**
     * Get security metrics (Admin only)
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSecurityMetrics(HttpServletRequest request) {
        try {
            String clientIp = getClientIP(request);
            securityLogger.info("SECURITY_METRICS_REQUESTED - IP: {}", clientIp);
            
            SecurityMonitoringService.SecurityMetrics metrics = securityMonitoringService.getSecurityMetrics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("metrics", metrics);
            response.put("jwtBlacklistSize", jwtBlacklist.getBlacklistedTokensCount());
            response.put("active2FACodes", twoFactorAuthService.getActiveCodesCount());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            securityLogger.error("ERROR_GETTING_SECURITY_METRICS - Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to get security metrics");
        }
    }

    /**
     * Force cleanup of expired security data
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> forceCleanup(HttpServletRequest request) {
        try {
            String clientIp = getClientIP(request);
            securityLogger.info("SECURITY_CLEANUP_REQUESTED - IP: {}", clientIp);
            
            jwtBlacklist.forceCleanup();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Security cleanup completed successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            securityLogger.error("ERROR_SECURITY_CLEANUP - Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to perform cleanup");
        }
    }

    /**
     * Check if IP is currently flagged as suspicious
     */
    @GetMapping("/check-ip/{ip}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> checkIpStatus(@PathVariable String ip, HttpServletRequest request) {
        try {
            String clientIp = getClientIP(request);
            securityLogger.info("IP_STATUS_CHECK_REQUESTED - RequestIP: {}, CheckIP: {}", clientIp, ip);
            
            boolean isSuspicious = securityMonitoringService.isSuspiciousIp(ip);
            
            Map<String, Object> response = new HashMap<>();
            response.put("ip", ip);
            response.put("suspicious", isSuspicious);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            securityLogger.error("ERROR_CHECKING_IP_STATUS - Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to check IP status");
        }
    }

    /**
     * Generate 2FA code for testing (Admin only)
     */
    @PostMapping("/2fa/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generate2FACode(@RequestBody Map<String, String> request, 
                                             HttpServletRequest httpRequest) {
        try {
            String identifier = request.get("identifier");
            if (identifier == null || identifier.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Identifier is required");
            }
            
            String clientIp = getClientIP(httpRequest);
            securityLogger.info("2FA_CODE_GENERATION_REQUESTED - IP: {}, Identifier: {}", clientIp, identifier);
            
            String code = twoFactorAuthService.generateTwoFactorCode(identifier);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", code);
            response.put("identifier", identifier);
            response.put("expirationMinutes", 5);
            response.put("message", "2FA code generated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            securityLogger.error("ERROR_GENERATING_2FA_CODE - Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to generate 2FA code");
        }
    }

    /**
     * Verify 2FA code for testing (Admin only)
     */
    @PostMapping("/2fa/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> verify2FACode(@RequestBody Map<String, String> request, 
                                           HttpServletRequest httpRequest) {
        try {
            String identifier = request.get("identifier");
            String code = request.get("code");
            
            if (identifier == null || code == null) {
                return ResponseEntity.badRequest().body("Identifier and code are required");
            }
            
            String clientIp = getClientIP(httpRequest);
            securityLogger.info("2FA_CODE_VERIFICATION_REQUESTED - IP: {}, Identifier: {}", clientIp, identifier);
            
            TwoFactorAuthService.TwoFactorVerificationResult result = 
                twoFactorAuthService.verifyTwoFactorCode(identifier, code);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", result.isValid());
            response.put("message", result.getMessage());
            response.put("identifier", identifier);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            securityLogger.error("ERROR_VERIFYING_2FA_CODE - Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to verify 2FA code");
        }
    }

    /**
     * Health check endpoint for security services
     */
    @GetMapping("/health")
    public ResponseEntity<?> securityHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("services", Map.of(
            "securityMonitoring", "active",
            "jwtBlacklist", "active",
            "twoFactorAuth", "active"
        ));
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty() && !"unknown".equalsIgnoreCase(xfHeader)) {
            return xfHeader.split(",")[0].trim();
        }
        
        String xrHeader = request.getHeader("X-Real-IP");
        if (xrHeader != null && !xrHeader.isEmpty() && !"unknown".equalsIgnoreCase(xrHeader)) {
            return xrHeader;
        }
        
        return request.getRemoteAddr();
    }
} 