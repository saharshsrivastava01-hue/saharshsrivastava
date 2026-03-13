package com.goldtrader.model;

public enum TransactionType {
    BUY("Purchase"),
    SELL("Sale");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
