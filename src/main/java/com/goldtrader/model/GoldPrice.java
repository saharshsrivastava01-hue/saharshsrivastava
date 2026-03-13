package com.goldtrader.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GoldPrice {
    private int id;
    private GoldType goldType;
    private BigDecimal pricePerGram;
    private LocalDateTime effectiveDate;
    private String updatedBy;

    public GoldPrice() {
        this.effectiveDate = LocalDateTime.now();
    }

    public GoldPrice(GoldType goldType, BigDecimal pricePerGram, String updatedBy) {
        this();
        this.goldType = goldType;
        this.pricePerGram = pricePerGram;
        this.updatedBy = updatedBy;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public GoldType getGoldType() { return goldType; }
    public void setGoldType(GoldType goldType) { this.goldType = goldType; }

    public BigDecimal getPricePerGram() { return pricePerGram; }
    public void setPricePerGram(BigDecimal pricePerGram) { this.pricePerGram = pricePerGram; }

    public LocalDateTime getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDateTime effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
