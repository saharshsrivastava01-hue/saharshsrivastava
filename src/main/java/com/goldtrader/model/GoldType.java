package com.goldtrader.model;

public enum GoldType {
    GOLD_24K("24K", 99.9, "Pure Gold"),
    GOLD_22K("22K", 91.6, "Jewelry Grade"),
    GOLD_18K("18K", 75.0, "Standard Jewelry"),
    GOLD_14K("14K", 58.3, "Economy Jewelry");

    private final String label;
    private final double purityPercent;
    private final String description;

    GoldType(String label, double purityPercent, String description) {
        this.label = label;
        this.purityPercent = purityPercent;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public double getPurityPercent() {
        return purityPercent;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return label + " (" + purityPercent + "% pure - " + description + ")";
    }

    public static GoldType fromLabel(String label) {
        for (GoldType type : values()) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown gold type: " + label);
    }
}
