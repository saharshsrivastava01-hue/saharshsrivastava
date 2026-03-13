package com.goldtrading.model;

/**
 * Represents the type of gold transaction.
 */
public enum TransactionType {
    BUY("BUY", "Purchase from supplier"),
    SELL("SELL", "Sale to customer");

    private final String code;
    private final String description;

    TransactionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TransactionType fromCode(String code) {
        for (TransactionType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type: " + code);
    }
}
