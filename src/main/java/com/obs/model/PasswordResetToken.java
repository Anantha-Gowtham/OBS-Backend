package com.obs.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String token;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;
    @Column(nullable = false)
    private Instant expiresAt;
    @Column(nullable = false)
    private boolean used = false;

    public Long getId() { return id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}
