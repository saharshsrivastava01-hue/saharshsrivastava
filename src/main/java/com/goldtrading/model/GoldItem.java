package com.goldtrading.model;

import java.time.LocalDateTime;

/**
 * Represents a gold inventory item in the system.
 */
public class GoldItem {

    private int id;
    private GoldType goldType;
    private double weightGrams;
    private double purchasePricePerGram;
    private double sellingPricePerGram;
    private String description;
    private String status; // AVAILABLE, SOLD, RESERVED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GoldItem() {
        this.status = "AVAILABLE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public GoldItem(GoldType goldType, double weightGrams, double purchasePricePerGram,
                    double sellingPricePerGram, String description) {
        this();
        this.goldType = goldType;
        this.weightGrams = weightGrams;
        this.purchasePricePerGram = purchasePricePerGram;
        this.sellingPricePerGram = sellingPricePerGram;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public GoldType getGoldType() { return goldType; }
    public void setGoldType(GoldType goldType) { this.goldType = goldType; }

    public double getWeightGrams() { return weightGrams; }
    public void setWeightGrams(double weightGrams) { this.weightGrams = weightGrams; }

    public double getPurchasePricePerGram() { return purchasePricePerGram; }
    public void setPurchasePricePerGram(double purchasePricePerGram) { this.purchasePricePerGram = purchasePricePerGram; }

    public double getSellingPricePerGram() { return sellingPricePerGram; }
    public void setSellingPricePerGram(double sellingPricePerGram) { this.sellingPricePerGram = sellingPricePerGram; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public double getTotalPurchaseValue() {
        return weightGrams * purchasePricePerGram;
    }

    public double getTotalSellingValue() {
        return weightGrams * sellingPricePerGram;
    }

    public double getPotentialProfit() {
        return getTotalSellingValue() - getTotalPurchaseValue();
    }

    @Override
    public String toString() {
        return String.format("GoldItem[id=%d, type=%s, weight=%.2fg, buyPrice=%.2f/g, sellPrice=%.2f/g, status=%s]",
                id, goldType.getLabel(), weightGrams, purchasePricePerGram, sellingPricePerGram, status);
    }
}
