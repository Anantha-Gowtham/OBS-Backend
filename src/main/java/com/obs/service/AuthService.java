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

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private EmailService emailService;
    
    public Map<String, Object> login(Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
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
                "role", user.getRole().toString()
            ));
            
            return response;
        } catch (BadCredentialsException e) {
            log.warn("Login failed for {}: invalid credentials", username);
            throw new RuntimeException("Invalid credentials");
        } catch (Exception e) {
            log.error("Unexpected error during login for {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public Map<String, Object> register(Map<String, Object> registerRequest) {
        String username = (String) registerRequest.get("username");
        String email = (String) registerRequest.get("email");
        String password = (String) registerRequest.get("password");
        String roleStr = (String) registerRequest.get("role");
        
        Role role = Role.USER; // default
        if (roleStr != null) {
            try {
                role = Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Keep default role
            }
        }
        
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        
        user = userRepository.save(user);
        
        // Send welcome email
        boolean emailSent = false;
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
            emailSent = true;
            log.info("Welcome email sent successfully to {}", user.getEmail());
        } catch (Exception e) {
            // Log error but don't fail registration
            log.warn("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }
        
        Map<String, Object> response = new HashMap<>();
        if (emailSent) {
            response.put("message", "Your account has been created successfully! Please check your email for a welcome message and then login to your new OBS account.");
        } else {
            response.put("message", "Your account has been created successfully! Please login to your new OBS account. (Note: Welcome email could not be sent)");
        }
        response.put("success", true);
        response.put("user", Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "role", user.getRole().toString()
        ));
        response.put("emailSent", emailSent);
        
        return response;
    }
    
    public Map<String, Object> forgotPassword(Map<String, String> request) {
        String email = request.get("email");
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Generate reset token (simplified - in production use secure tokens)
        String resetToken = "reset_" + System.currentTimeMillis();
        
        // Send reset email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetToken);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reset email");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password reset email sent");
        return response;
    }
    
    @Transactional
    public Map<String, Object> resetPassword(String token, String newPassword) {
        // Simplified token validation - in production use proper token storage and validation
        if (!token.startsWith("reset_")) {
            throw new RuntimeException("Invalid reset token");
        }
        
        // For demo purposes, extract email from a more sophisticated token system
        // In production, store tokens in database with expiration
        String email = "demo@example.com"; // This should be extracted from token
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        return response;
    }
    
    public Map<String, Object> changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Invalid current password");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return response;
    }
    
    @Transactional
    public Map<String, Object> refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }
        
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token has expired");
        }
        
        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenUtil.generateTokenFromUsername(user.getUsername());
        String newRefreshToken = generateRefreshToken(user);
        
        // Revoke the old refresh token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        response.put("user", Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "role", user.getRole().toString()
        ));
        
        return response;
    }
    
    @Transactional
    public Map<String, Object> logout(String refreshTokenStr, String username) {
        if (refreshTokenStr != null) {
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(refreshTokenStr);
            if (refreshToken.isPresent()) {
                refreshToken.get().setRevoked(true);
                refreshTokenRepository.save(refreshToken.get());
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return response;
    }
    
    private String generateRefreshToken(User user) {
        // Revoke any existing refresh tokens for this user
        List<RefreshToken> existingTokens = refreshTokenRepository.findByUser(user);
        for (RefreshToken token : existingTokens) {
            if (!token.isRevoked()) {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            }
        }
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60)); // 7 days
        refreshToken.setRevoked(false);
        
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
}