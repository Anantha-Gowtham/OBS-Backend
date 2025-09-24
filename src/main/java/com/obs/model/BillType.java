package com.obs.model;

public enum BillType {
    ELECTRICITY("Electricity Bill"),
    WATER("Water Bill"),
    GAS("Gas Bill"),
    INTERNET("Internet Bill"),
    MOBILE("Mobile Bill"),
    DTH("DTH/Cable Bill"),
    INSURANCE("Insurance Premium"),
    LOAN_EMI("Loan EMI"),
    CREDIT_CARD("Credit Card Bill"),
    MUNICIPAL_TAX("Municipal Tax"),
    PROPERTY_TAX("Property Tax"),
    EDUCATION_FEE("Education Fee"),
    HOSPITAL("Hospital Bill"),
    OTHER("Other Bills");

    private final String description;

    BillType(String description) {
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