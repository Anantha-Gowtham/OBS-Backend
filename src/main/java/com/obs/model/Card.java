package com.obs.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;

@Entity
public class Card {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    private Account account;
    
    @Column(unique = true, nullable = false, length = 16)
    private String cardNumber;
    
    @Column(nullable = false, length = 30)
    private String cardHolderName;
    
    @Column(nullable = false)
    private LocalDate expiryDate;
    
    @Column(nullable = false, length = 3)
    private String cvv;
    
    @Enumerated(EnumType.STRING)
    private CardType cardType = CardType.DEBIT;
    
    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.ACTIVE;
    
    @Column(nullable = false, length = 4)
    private String pin; // Encrypted PIN
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal dailyLimit = new BigDecimal("50000");
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyLimit = new BigDecimal("500000");
    
    @Column(nullable = false)
    private boolean contactlessEnabled = true;
    
    @Column(nullable = false)
    private boolean onlineTransactionEnabled = true;
    
    @Column(nullable = false)
    private boolean internationalUsageEnabled = false;
    
    @Column(length = 200)
    private String blockReason;
    
    @Column(name = "last_used")
    private LocalDateTime lastUsed;
    
    private Instant createdAt = Instant.now();
    
    // Constructors
    public Card() {}
    
    public Card(Account account, String cardHolderName) {
        this.account = account;
        this.cardHolderName = cardHolderName;
        this.cardNumber = generateCardNumber();
        this.expiryDate = LocalDate.now().plusYears(5);
        this.cvv = generateCVV();
        this.pin = "1234"; // Default PIN - should be encrypted in production
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    
    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }
    
    public CardStatus getStatus() { return status; }
    public void setStatus(CardStatus status) { this.status = status; }
    
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }
    
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
    
    public boolean isContactlessEnabled() { return contactlessEnabled; }
    public void setContactlessEnabled(boolean contactlessEnabled) { this.contactlessEnabled = contactlessEnabled; }
    
    public boolean isOnlineTransactionEnabled() { return onlineTransactionEnabled; }
    public void setOnlineTransactionEnabled(boolean onlineTransactionEnabled) { this.onlineTransactionEnabled = onlineTransactionEnabled; }
    
    public boolean isInternationalUsageEnabled() { return internationalUsageEnabled; }
    public void setInternationalUsageEnabled(boolean internationalUsageEnabled) { this.internationalUsageEnabled = internationalUsageEnabled; }
    
    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }
    
    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
    
    // Business methods
    public void blockCard(String reason) {
        this.status = CardStatus.BLOCKED;
        this.blockReason = reason;
    }
    
    public void activateCard() {
        this.status = CardStatus.ACTIVE;
        this.blockReason = null;
    }
    
    public void markAsUsed() {
        this.lastUsed = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return this.status == CardStatus.ACTIVE;
    }
    
    public boolean isExpired() {
        return this.expiryDate.isBefore(LocalDate.now());
    }
    
    public String getLast4Digits() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
    
    // Helper methods
    private String generateCardNumber() {
        // Generate a 16-digit card number starting with 4520 (OBS Bank prefix)
        StringBuilder cardNumber = new StringBuilder("4520");
        for (int i = 0; i < 12; i++) {
            cardNumber.append((int)(Math.random() * 10));
        }
        return cardNumber.toString();
    }
    
    private String generateCVV() {
        return String.format("%03d", (int)(Math.random() * 1000));
    }
    
    // Utility method to get masked card number for display
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****-****-****-****";
        }
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }
}
