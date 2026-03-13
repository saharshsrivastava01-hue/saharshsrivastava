package com.goldtrader.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Inventory {
    private int id;
    private GoldType goldType;
    private BigDecimal weightGrams;
    private BigDecimal avgCostPerGram;
    private BigDecimal totalValue;
    private LocalDateTime lastUpdated;

    public Inventory() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Inventory(GoldType goldType, BigDecimal weightGrams, BigDecimal avgCostPerGram) {
        this();
        this.goldType = goldType;
        this.weightGrams = weightGrams;
        this.avgCostPerGram = avgCostPerGram;
        this.totalValue = weightGrams.multiply(avgCostPerGram);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public GoldType getGoldType() { return goldType; }
    public void setGoldType(GoldType goldType) { this.goldType = goldType; }

    public BigDecimal getWeightGrams() { return weightGrams; }
    public void setWeightGrams(BigDecimal weightGrams) { this.weightGrams = weightGrams; }

    public BigDecimal getAvgCostPerGram() { return avgCostPerGram; }
    public void setAvgCostPerGram(BigDecimal avgCostPerGram) { this.avgCostPerGram = avgCostPerGram; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public void recalculateTotalValue() {
        if (weightGrams != null && avgCostPerGram != null) {
            this.totalValue = weightGrams.multiply(avgCostPerGram);
        }
    }
}
