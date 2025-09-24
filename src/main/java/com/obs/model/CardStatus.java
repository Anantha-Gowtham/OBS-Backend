package com.obs.model;

public enum CardStatus {
    PENDING("Card Application Pending"),
    ACTIVE("Card Active"),
    INACTIVE("Card Inactive"),
    BLOCKED("Card Blocked"),
    EXPIRED("Card Expired"),
    CANCELLED("Card Cancelled");

    private final String description;

    CardStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
