package com.obs.model;

public enum BeneficiaryType {
    INTERNAL("Internal Account"),
    EXTERNAL("External Bank Account"),
    UPI("UPI Account"),
    IMPS("IMPS Transfer"),
    NEFT("NEFT Transfer"),
    RTGS("RTGS Transfer");

    private final String description;

    BeneficiaryType(String description) {
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