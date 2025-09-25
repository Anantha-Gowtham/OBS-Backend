package com.obs.service;

import com.obs.model.User;
import com.obs.model.Role;
import com.obs.model.RefreshToken;
import com.obs.repository.UserRepository;
import com.obs.repository.RefreshTokenRepository;
import com.obs.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

/**
 * AuthService - DEVELOPMENT VERSION with Plain Text Passwords
 * WARNING: This version stores passwords in plain text for testing purposes only
 * DO NOT use in production environment
 */
@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private EmailService emailService;
    
    // Development login with plain text password comparison
    public Map<String, Object> login(Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Plain text password comparison for development
            if (!user.getPassword().equals(password)) {
                throw new RuntimeException("Invalid username or password");
            }
            
            String token = jwtTokenUtil.generateTokenFromUsername(user.getUsername());
            String refreshToken = null;
            try {
                refreshToken = generateRefreshToken(user);
            } catch (Exception rtEx) {
                log.error("Failed to generate refresh token for user {}: {}", username, rtEx.getMessage());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            if (refreshToken != null) {
                response.put("refreshToken", refreshToken);
            }
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                "lastName", user.getLastName() != null ? user.getLastName() : "",
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "role", user.getRole().toString()
            ));
            
            log.info("User {} logged in successfully", username);
            return response;
            
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Invalid username or password");
        }
    }
    
    // Development registration with plain text password storage
    @Transactional
    public Map<String, Object> register(Map<String, Object> registerRequest) {
        String username = (String) registerRequest.get("username");
        String password = (String) registerRequest.get("password");
        String email = (String) registerRequest.get("email");
        String firstName = (String) registerRequest.get("firstName");
        String lastName = (String) registerRequest.get("lastName");
        String phone = (String) registerRequest.get("phone");
        String dateOfBirth = (String) registerRequest.get("dateOfBirth");
        String address = (String) registerRequest.get("address");
        
        // Default role is USER
        Role role = Role.USER;
        
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        // Store password as plain text for development
        user.setPassword(password);
        user.setRole(role);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setAddress(address);
        user.setActive(true);
        
        // Parse date of birth if provided
        if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
            try {
                // Assuming date format is YYYY-MM-DD
                user.setDateOfBirth(java.sql.Date.valueOf(dateOfBirth));
            } catch (Exception e) {
                log.warn("Invalid date of birth format for user {}: {}", username, dateOfBirth);
            }
        }
        
        user = userRepository.save(user);
        
        // Send welcome email
        boolean emailSent = false;
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
            emailSent = true;
            log.info("Welcome email sent successfully to {}", user.getEmail());
        } catch (Exception e) {
            log.warn("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful");
        response.put("emailSent", emailSent);
        response.put("user", Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "firstName", user.getFirstName() != null ? user.getFirstName() : "",
            "lastName", user.getLastName() != null ? user.getLastName() : "",
            "role", user.getRole().toString()
        ));
        
        log.info("User {} registered successfully with plain text password", username);
        return response;
    }
    
    private String generateRefreshToken(User user) {
        // Clean up existing refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60)); // 7 days
        
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
    
    public Map<String, Object> refreshToken(Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);
        if (!tokenOpt.isPresent()) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        RefreshToken token = tokenOpt.get();
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }
        
        User user = token.getUser();
        String newJwtToken = jwtTokenUtil.generateTokenFromUsername(user.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", newJwtToken);
        response.put("refreshToken", refreshToken);
        
        return response;
    }
    
    // Get all users for admin purposes
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // Reset password (plain text for development)
    public void resetPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Store new password as plain text
        user.setPassword(newPassword);
        userRepository.save(user);
        
        log.info("Password reset for user {} with plain text storage", username);
    }
}