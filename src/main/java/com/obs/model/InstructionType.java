package com.obs.model;

public enum InstructionType {
    FUND_TRANSFER("Fund Transfer"),
    BILL_PAYMENT("Bill Payment"),  
    EMI_PAYMENT("EMI Payment"),
    LOAN_REPAYMENT("Loan Repayment"),
    INVESTMENT("Investment"),
    INSURANCE_PREMIUM("Insurance Premium"),
    UTILITY_BILL("Utility Bill"),
    RECURRING_DEPOSIT("Recurring Deposit");

    private final String displayName;

    InstructionType(String displayName) {
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