package com.obs.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User user;
    @Column(nullable = false, length = 20, unique = true)
    private String accountNumber;
    @Column(nullable = false, length = 30)
    private String type; // SAVINGS, CURRENT, etc.
    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.PENDING;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Long getUserId() { return user != null ? user.getId() : null; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAccountType() { return type; } // Alias for compatibility
    public void setAccountType(String accountType) { this.type = accountType; } // Setter for compatibility
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public java.math.BigDecimal getBalance() { return balance; }
    public void setBalance(java.math.BigDecimal balance) { this.balance = balance; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
