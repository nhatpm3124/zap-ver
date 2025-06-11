package com.example.simplelogin.controller;

import com.example.simplelogin.dto.JwtResponse;
import com.example.simplelogin.dto.LoginRequest;
import com.example.simplelogin.dto.SignupRequest;
import com.example.simplelogin.model.User;
import com.example.simplelogin.repository.UserRepository;
import com.example.simplelogin.security.JwtUtils;
import com.example.simplelogin.security.PasswordValidator;
import com.example.simplelogin.security.SecurityMonitoringService;
import com.example.simplelogin.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PasswordValidator passwordValidator;

    @Autowired
    SecurityMonitoringService securityMonitoringService;

    @Value("${app.validation.max-username-length:50}")
    private int maxUsernameLength;

    @Value("${app.validation.max-email-length:100}")
    private int maxEmailLength;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, 
                                              HttpServletRequest request) {
        try {
            String clientIp = getClientIP(request);
            
            // Enhanced input validation
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                securityLogger.warn("LOGIN_ATTEMPT_EMPTY_USERNAME - IP: {}", clientIp);
                return ResponseEntity.badRequest().body("Username cannot be empty");
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                securityLogger.warn("LOGIN_ATTEMPT_EMPTY_PASSWORD - IP: {}, Username: {}", 
                    clientIp, loginRequest.getUsername());
                return ResponseEntity.badRequest().body("Password cannot be empty");
            }

            // Sanitize username
            String sanitizedUsername = sanitizeInput(loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(sanitizedUsername, loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // Record successful login for security monitoring
            securityMonitoringService.recordSuccessfulLogin(clientIp, userDetails.getUsername());
            
            securityLogger.info("SUCCESSFUL_LOGIN - IP: {}, Username: {}, UserId: {}", 
                clientIp, userDetails.getUsername(), userDetails.getId());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail()));
                    
        } catch (BadCredentialsException e) {
            String clientIp = getClientIP(request);
            
            // Record failed login for security monitoring
            securityMonitoringService.recordFailedLogin(clientIp, loginRequest.getUsername());
            
            securityLogger.warn("FAILED_LOGIN_ATTEMPT - IP: {}, Username: {}, Reason: Invalid credentials", 
                clientIp, loginRequest.getUsername());
            return ResponseEntity.badRequest().body("Invalid username or password");
        } catch (Exception e) {
            String clientIp = getClientIP(request);
            securityLogger.error("LOGIN_ERROR - IP: {}, Username: {}, Error: {}", 
                clientIp, loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body("Authentication failed");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest,
                                          HttpServletRequest request) {
        try {
            String clientIp = getClientIP(request);
            
            // Check registration rate limiting
            if (!securityMonitoringService.recordRegistrationAttempt(clientIp)) {
                securityLogger.warn("REGISTRATION_RATE_LIMITED - IP: {}", clientIp);
                return ResponseEntity.badRequest().body("Too many registration attempts. Please try again later.");
            }
            
            // Enhanced input validation
            if (signUpRequest.getUsername() == null || signUpRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Username cannot be empty");
            }
            
            if (signUpRequest.getEmail() == null || signUpRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email cannot be empty");
            }
            
            // Length validation
            if (signUpRequest.getUsername().length() > maxUsernameLength) {
                return ResponseEntity.badRequest()
                    .body("Username must not exceed " + maxUsernameLength + " characters");
            }
            
            if (signUpRequest.getEmail().length() > maxEmailLength) {
                return ResponseEntity.badRequest()
                    .body("Email must not exceed " + maxEmailLength + " characters");
            }

            // Sanitize inputs
            String sanitizedUsername = sanitizeInput(signUpRequest.getUsername());
            String sanitizedEmail = sanitizeInput(signUpRequest.getEmail());

            // Validate password strength
            PasswordValidator.PasswordValidationResult passwordResult = 
                passwordValidator.validatePassword(signUpRequest.getPassword());
                
            if (!passwordResult.isValid()) {
                securityLogger.warn("WEAK_PASSWORD_ATTEMPT - IP: {}, Username: {}", clientIp, sanitizedUsername);
                return ResponseEntity.badRequest().body(passwordResult.getErrorMessage());
            }

            // Check if username exists
            if (userRepository.existsByUsername(sanitizedUsername)) {
                securityLogger.warn("DUPLICATE_USERNAME_ATTEMPT - IP: {}, Username: {}", clientIp, sanitizedUsername);
                return ResponseEntity.badRequest().body("Error: Username is already taken!");
            }

            // Check if email exists
            if (userRepository.existsByEmail(sanitizedEmail)) {
                securityLogger.warn("DUPLICATE_EMAIL_ATTEMPT - IP: {}, Email: {}", clientIp, sanitizedEmail);
                return ResponseEntity.badRequest().body("Error: Email is already in use!");
            }

            // Create new user's account
            User user = new User(sanitizedUsername,
                    sanitizedEmail,
                    encoder.encode(signUpRequest.getPassword()));

            userRepository.save(user);
            
            securityLogger.info("SUCCESSFUL_REGISTRATION - IP: {}, Username: {}, Email: {}", 
                clientIp, sanitizedUsername, sanitizedEmail);

            return ResponseEntity.ok("User registered successfully!");
            
        } catch (Exception e) {
            String clientIp = getClientIP(request);
            securityLogger.error("REGISTRATION_ERROR - IP: {}, Error: {}", clientIp, e.getMessage());
            return ResponseEntity.badRequest().body("Registration failed");
        }
    }

    private String sanitizeInput(String input) {
        if (input == null) return null;
        
        // Remove potential XSS and injection characters
        return input.trim()
                   .replaceAll("[<>\"'%;()&+]", "")
                   .replaceAll("\\s+", " ");
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