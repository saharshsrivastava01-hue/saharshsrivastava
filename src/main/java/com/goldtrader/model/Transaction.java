package com.goldtrader.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private TransactionType type;
    private GoldType goldType;
    private BigDecimal weightGrams;
    private BigDecimal pricePerGram;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private int customerId;
    private String customerName;
    private String notes;
    private LocalDateTime transactionDate;
    private String createdBy;

    public Transaction() {
        this.transactionDate = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public GoldType getGoldType() { return goldType; }
    public void setGoldType(GoldType goldType) { this.goldType = goldType; }

    public BigDecimal getWeightGrams() { return weightGrams; }
    public void setWeightGrams(BigDecimal weightGrams) { this.weightGrams = weightGrams; }

    public BigDecimal getPricePerGram() { return pricePerGram; }
    public void setPricePerGram(BigDecimal pricePerGram) { this.pricePerGram = pricePerGram; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public void calculateTotals(BigDecimal taxRate) {
        if (weightGrams != null && pricePerGram != null) {
            this.totalAmount = weightGrams.multiply(pricePerGram);
            this.taxAmount = totalAmount.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            this.netAmount = totalAmount.add(taxAmount);
        }
    }
}
