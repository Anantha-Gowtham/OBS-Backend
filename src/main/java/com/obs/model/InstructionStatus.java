package com.obs.model;

public enum InstructionStatus {
    ACTIVE("Active"),
    PAUSED("Paused"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    FAILED("Failed");

    private final String displayName;

    InstructionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}