package com.obs.controller;

import com.obs.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication Controller - User authentication and registration endpoints
 * Enhanced with comprehensive security features including JWT token management
 * Public endpoints for authentication operations
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            Map<String, Object> response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (org.springframework.security.core.AuthenticationException ae) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid username or password"));
        } catch (RuntimeException re) {
            // Common business errors like Invalid credentials
            return ResponseEntity.status(401).body(Map.of("message", re.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Login failed: " + e.getMessage()));
        }
    }

    // Lightweight probe to confirm unauthenticated access under context path
    @GetMapping("/probe")
    public ResponseEntity<?> probe() {
        return ResponseEntity.ok(Map.of("status", "ok", "path", "/auth/probe"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> registerRequest) {
        try {
            Map<String, Object> response = authService.register(registerRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException re) {
            return ResponseEntity.badRequest().body(Map.of("message", re.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Registration failed"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, Authentication auth) {
        String username = auth.getName();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        Map<String, Object> response = authService.changePassword(username, oldPassword, newPassword);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            Map<String, Object> response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException re) {
            return ResponseEntity.badRequest().body(Map.of("message", re.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token refresh failed"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request, Authentication auth) {
        String refreshToken = request.get("refreshToken");
        String username = auth != null ? auth.getName() : null;
        Map<String, Object> response = authService.logout(refreshToken, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        return ResponseEntity.ok(Map.of(
            "username", auth.getName(),
            "authorities", auth.getAuthorities()
        ));
    }
}
