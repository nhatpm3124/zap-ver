package com.example.simplelogin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class SecurityLoggingFilter extends OncePerRequestFilter {
    
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String clientIp = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        // Set MDC for correlation
        MDC.put("requestId", requestId);
        MDC.put("clientIp", clientIp);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Log incoming request for sensitive endpoints
            if (isSensitiveEndpoint(uri)) {
                securityLogger.info("INCOMING_REQUEST - RequestId: {}, IP: {}, Method: {}, URI: {}, UserAgent: {}, Timestamp: {}", 
                    requestId, clientIp, method, uri, userAgent, LocalDateTime.now().format(formatter));
            }
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            securityLogger.error("REQUEST_ERROR - RequestId: {}, IP: {}, URI: {}, Error: {}", 
                requestId, clientIp, uri, e.getMessage());
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            
            // Log response for sensitive endpoints or error responses
            if (isSensitiveEndpoint(uri) || status >= 400) {
                String logLevel = status >= 400 ? "WARN" : "INFO";
                securityLogger.info("RESPONSE - RequestId: {}, IP: {}, URI: {}, Status: {}, Duration: {}ms, Timestamp: {}", 
                    requestId, clientIp, uri, status, duration, LocalDateTime.now().format(formatter));
                
                // Log specific security events
                if (status == 401) {
                    securityLogger.warn("UNAUTHORIZED_ACCESS - RequestId: {}, IP: {}, URI: {}", requestId, clientIp, uri);
                } else if (status == 403) {
                    securityLogger.warn("FORBIDDEN_ACCESS - RequestId: {}, IP: {}, URI: {}", requestId, clientIp, uri);
                } else if (status == 429) {
                    securityLogger.warn("RATE_LIMIT_EXCEEDED - RequestId: {}, IP: {}, URI: {}", requestId, clientIp, uri);
                }
            }
            
            // Clear MDC
            MDC.clear();
        }
    }

    private boolean isSensitiveEndpoint(String uri) {
        return uri.startsWith("/api/auth/") || 
               uri.startsWith("/api/admin/") || 
               uri.contains("password") ||
               uri.contains("token");
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