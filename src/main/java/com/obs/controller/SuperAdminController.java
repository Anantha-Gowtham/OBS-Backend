package com.obs.controller;

import com.obs.model.User;
import com.obs.model.UserProfile;
import com.obs.model.Role;
import com.obs.service.SuperAdminService;
import com.obs.service.UserService;
import com.obs.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@CrossOrigin(origins = "http://localhost:5173")
public class SuperAdminController {

    @Autowired
    private SuperAdminService superAdminService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    /**
     * Get all users in the system
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = superAdminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user statistics dashboard data
     */
    @GetMapping("/stats")
    public ResponseEntity<SuperAdminService.UserStats> getUserStats() {
        try {
            SuperAdminService.UserStats stats = superAdminService.getUserStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Promote user to next higher role
     */
    @PutMapping("/users/{userId}/promote")
    public ResponseEntity<Map<String, String>> promoteUser(@PathVariable Long userId) {
        Map<String, String> response = new HashMap<>();
        
        try {
            boolean success = superAdminService.promoteUser(userId);
            if (success) {
                response.put("message", "User promoted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to promote user");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("error", "Error promoting user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Demote user to next lower role
     */
    @PutMapping("/users/{userId}/demote")
    public ResponseEntity<Map<String, String>> demoteUser(@PathVariable Long userId) {
        Map<String, String> response = new HashMap<>();
        
        try {
            boolean success = superAdminService.demoteUser(userId);
            if (success) {
                response.put("message", "User demoted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to demote user");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("error", "Error demoting user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Set specific role for a user
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Map<String, String>> setUserRole(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String roleStr = request.get("role");
            if (roleStr == null || roleStr.isEmpty()) {
                response.put("error", "Role is required");
                return ResponseEntity.badRequest().body(response);
            }

            Role role;
            try {
                role = Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                response.put("error", "Invalid role: " + roleStr);
                return ResponseEntity.badRequest().body(response);
            }

            boolean success = superAdminService.setUserRole(userId, role);
            if (success) {
                response.put("message", "User role updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to update user role");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("error", "Error updating user role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete a user from the system
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        Map<String, String> response = new HashMap<>();
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            User currentUser = userService.findByUsername(currentUsername);
            
            if (currentUser == null) {
                response.put("error", "Current user not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            boolean success = superAdminService.deleteUser(userId, currentUser.getId());
            if (success) {
                response.put("message", "User deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to delete user");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("error", "Error deleting user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Reset user password
     */
    @PutMapping("/users/{userId}/reset-password")
    public ResponseEntity<Map<String, String>> resetUserPassword(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String newPassword = request.get("password");
            if (newPassword == null || newPassword.length() < 6) {
                response.put("error", "Password must be at least 6 characters");
                return ResponseEntity.badRequest().body(response);
            }

            boolean success = superAdminService.resetUserPassword(userId, newPassword);
            if (success) {
                response.put("message", "Password reset successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to reset password");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("error", "Error resetting password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Toggle user lock status
     */
    @PutMapping("/users/{userId}/toggle-lock")
    public ResponseEntity<Map<String, String>> toggleUserLock(@PathVariable Long userId) {
        Map<String, String> response = new HashMap<>();
        
        try {
            boolean success = superAdminService.toggleUserLock(userId);
            if (success) {
                response.put("message", "User lock status updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to update lock status");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("error", "Error updating lock status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Create or update user profile
     */
    @PutMapping("/users/{userId}/profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = request.get("phoneNumber");
            String aadhaarNumber = request.get("aadhaarNumber");

            UserProfile profile = superAdminService.createOrUpdateProfile(userId, phoneNumber, aadhaarNumber);
            if (profile != null) {
                response.put("message", "Profile updated successfully");
                response.put("profile", profile);
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to update profile");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("error", "Error updating profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get available roles for assignment
     */
    @GetMapping("/roles")
    public ResponseEntity<Role[]> getAvailableRoles() {
        return ResponseEntity.ok(Role.values());
    }

    /**
     * Create new user with profile
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                response.put("error", "Username is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                response.put("error", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                response.put("error", "Password must be at least 6 characters");
                return ResponseEntity.badRequest().body(response);
            }

            // Create user using UserService
            User newUser = userService.createUser(request.getUsername(), request.getEmail(), request.getPassword(), request.getRole());
            
            // Create profile if phone or aadhaar provided
            if ((request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) ||
                (request.getAadhaarNumber() != null && !request.getAadhaarNumber().trim().isEmpty())) {
                UserProfile profile = superAdminService.createOrUpdateProfile(
                    newUser.getId(), 
                    request.getPhoneNumber(), 
                    request.getAadhaarNumber()
                );
                response.put("profile", profile);
            }

            response.put("message", "User created successfully");
            response.put("user", newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            response.put("error", "Error creating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Test email service connectivity and configuration
     */
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                response.put("error", "Email address is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Test sending a welcome email
            emailService.sendWelcomeEmail(email, "Test User");
            
            response.put("success", true);
            response.put("message", "Test email sent successfully to " + email);
            response.put("note", "Please check your email (including spam folder) and server logs for details");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to send test email: " + e.getMessage());
            response.put("details", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Request class for creating users
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String password;
        private Role role = Role.USER; // Default role
        private String phoneNumber;
        private String aadhaarNumber;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getAadhaarNumber() { return aadhaarNumber; }
        public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }
    }
}