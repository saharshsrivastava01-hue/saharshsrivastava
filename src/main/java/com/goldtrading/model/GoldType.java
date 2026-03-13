package com.goldtrading.model;

/**
 * Represents the purity/karat types of gold available for trading.
 */
public enum GoldType {
    GOLD_24K("24K", 99.9, "Pure Gold (999)"),
    GOLD_22K("22K", 91.6, "Jewellery Gold (916)"),
    GOLD_18K("18K", 75.0, "Standard Gold (750)"),
    GOLD_14K("14K", 58.5, "Economy Gold (585)");

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

    public static GoldType fromLabel(String label) {
        for (GoldType type : values()) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown gold type: " + label);
    }

    @Override
    public String toString() {
        return label + " - " + description + " (" + purityPercent + "% purity)";
    }
}
