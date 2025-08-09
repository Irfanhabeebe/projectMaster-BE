package com.projectmaster.app.common.enums;

public enum PaymentMethod {
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    CHEQUE("Cheque"),
    PAYPAL("PayPal"),
    STRIPE("Stripe"),
    OTHER("Other");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}