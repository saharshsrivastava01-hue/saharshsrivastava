package com.goldtrading.model;

import java.time.LocalDateTime;

/**
 * Represents current market gold prices per gram for different karat types.
 */
public class GoldPrice {

    private int id;
    private GoldType goldType;
    private double buyPricePerGram;
    private double sellPricePerGram;
    private LocalDateTime effectiveDate;
    private LocalDateTime updatedAt;

    public GoldPrice() {
        this.effectiveDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public GoldPrice(GoldType goldType, double buyPricePerGram, double sellPricePerGram) {
        this();
        this.goldType = goldType;
        this.buyPricePerGram = buyPricePerGram;
        this.sellPricePerGram = sellPricePerGram;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public GoldType getGoldType() { return goldType; }
    public void setGoldType(GoldType goldType) { this.goldType = goldType; }

    public double getBuyPricePerGram() { return buyPricePerGram; }
    public void setBuyPricePerGram(double buyPricePerGram) { this.buyPricePerGram = buyPricePerGram; }

    public double getSellPricePerGram() { return sellPricePerGram; }
    public void setSellPricePerGram(double sellPricePerGram) { this.sellPricePerGram = sellPricePerGram; }

    public LocalDateTime getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDateTime effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public double getSpread() {
        return sellPricePerGram - buyPricePerGram;
    }

    public double getSpreadPercent() {
        if (buyPricePerGram == 0) return 0;
        return (getSpread() / buyPricePerGram) * 100;
    }

    @Override
    public String toString() {
        return String.format("GoldPrice[type=%s, buy=%.2f/g, sell=%.2f/g, spread=%.2f%%]",
                goldType.getLabel(), buyPricePerGram, sellPricePerGram, getSpreadPercent());
    }
}
