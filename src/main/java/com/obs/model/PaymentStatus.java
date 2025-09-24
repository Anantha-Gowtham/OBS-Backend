package com.obs.model;

public enum PaymentStatus {
    PENDING("Payment Pending"),
    PROCESSING("Payment Processing"),
    COMPLETED("Payment Completed"),
    FAILED("Payment Failed"),
    CANCELLED("Payment Cancelled"),
    REFUNDED("Payment Refunded");

    private final String description;

    PaymentStatus(String description) {
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