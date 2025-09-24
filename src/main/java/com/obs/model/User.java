package com.obs.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean locked = false;
    // Map to existing DB column 'failed_attempts'. If you rename the column in DB to 'failed_login_attempts', adjust annotation.
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
    private Instant lastLogin;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    public Instant getLastLogin() { return lastLogin; }
    public void setLastLogin(Instant lastLogin) { this.lastLogin = lastLogin; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    // No legacy dual-field now; ensure future migrations drop/rename only if needed.

    @PrePersist
    void prePersist() {
        if (failedAttempts < 0) failedAttempts = 0; // defensive default
    }
}
