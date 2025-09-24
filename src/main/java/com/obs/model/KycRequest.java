package com.obs.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class KycRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User user;
    @Enumerated(EnumType.STRING)
    private KycStatus status = KycStatus.PENDING;
    private String comments;
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public KycStatus getStatus() { return status; }
    public void setStatus(KycStatus status) { this.status = status; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
