package com.goldtrading.model;

import java.time.LocalDateTime;

/**
 * Represents a gold buy/sell transaction.
 */
public class Transaction {

    private int id;
    private TransactionType type;
    private int goldItemId;
    private int customerId;   // For SELL transactions
    private int supplierId;   // For BUY transactions
    private GoldType goldType;
    private double weightGrams;
    private double pricePerGram;
    private double totalAmount;
    private double taxAmount;
    private double finalAmount;
    private String paymentMethod;
    private String invoiceNumber;
    private String notes;
    private LocalDateTime transactionDate;

    public Transaction() {
        this.transactionDate = LocalDateTime.now();
    }

    public Transaction(TransactionType type, int goldItemId, GoldType goldType,
                       double weightGrams, double pricePerGram, String paymentMethod) {
        this();
        this.type = type;
        this.goldItemId = goldItemId;
        this.goldType = goldType;
        this.weightGrams = weightGrams;
        this.pricePerGram = pricePerGram;
        this.paymentMethod = paymentMethod;
        this.totalAmount = weightGrams * pricePerGram;
        this.taxAmount = totalAmount * 0.03; // 3% GST default
        this.finalAmount = totalAmount + taxAmount;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public int getGoldItemId() { return goldItemId; }
    public void setGoldItemId(int goldItemId) { this.goldItemId = goldItemId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public GoldType getGoldType() { return goldType; }
    public void setGoldType(GoldType goldType) { this.goldType = goldType; }

    public double getWeightGrams() { return weightGrams; }
    public void setWeightGrams(double weightGrams) { this.weightGrams = weightGrams; }

    public double getPricePerGram() { return pricePerGram; }
    public void setPricePerGram(double pricePerGram) { this.pricePerGram = pricePerGram; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    @Override
    public String toString() {
        return String.format("Transaction[id=%d, type=%s, goldType=%s, weight=%.2fg, total=%.2f, date=%s]",
                id, type.getCode(), goldType.getLabel(), weightGrams, finalAmount, transactionDate);
    }
}
