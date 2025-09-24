package com.obs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/debug")
@CrossOrigin(origins = "*")
public class DebugController {

    @GetMapping("/public")
    public ResponseEntity<?> publicEndpoint() {
        return ResponseEntity.ok(Map.of(
            "message", "This is a public endpoint",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/auth-test")
    public ResponseEntity<?> authTest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(Map.of(
            "authenticated", auth != null && auth.isAuthenticated(),
            "principal", auth != null ? auth.getPrincipal() : "null",
            "authorities", auth != null ? auth.getAuthorities() : "null",
            "name", auth != null ? auth.getName() : "null"
        ));
    }

    @GetMapping("/admin-test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminTest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(Map.of(
            "message", "Admin access successful",
            "user", auth.getName(),
            "authorities", auth.getAuthorities()
        ));
    }
}