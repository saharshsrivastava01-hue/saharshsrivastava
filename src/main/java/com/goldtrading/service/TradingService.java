package com.goldtrading.service;

import com.goldtrading.dao.TransactionDao;
import com.goldtrading.model.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for handling gold buy/sell transactions.
 */
public class TradingService {

    private static final double GST_RATE = 0.03; // 3% GST
    private static final AtomicInteger invoiceCounter = new AtomicInteger(1000);

    private final TransactionDao transactionDao;
    private final GoldInventoryService inventoryService;

    public TradingService(GoldInventoryService inventoryService) {
        this.transactionDao = new TransactionDao();
        this.inventoryService = inventoryService;
    }

    /**
     * Buy gold from a supplier - adds to inventory.
     */
    public Optional<Transaction> buyGold(int supplierId, GoldType goldType, double weightGrams,
                                         double pricePerGram, String paymentMethod, String description, String notes) {
        try {
            // Add item to inventory
            int itemId = inventoryService.addGoldItem(goldType, weightGrams, pricePerGram,
                    pricePerGram * 1.05, description); // 5% markup default

            if (itemId <= 0) {
                System.err.println("[TRADE] Failed to add gold item to inventory");
                return Optional.empty();
            }

            // Create transaction
            Transaction txn = new Transaction(TransactionType.BUY, itemId, goldType, weightGrams,
                    pricePerGram, paymentMethod);
            txn.setSupplierId(supplierId);
            txn.setNotes(notes);
            txn.setInvoiceNumber(generateInvoiceNumber("BUY"));

            int txnId = transactionDao.insert(txn);
            if (txnId > 0) {
                return Optional.of(txn);
            }
        } catch (SQLException e) {
            System.err.println("[TRADE] Error buying gold: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Sell gold to a customer - removes from inventory.
     */
    public Optional<Transaction> sellGold(int customerId, int goldItemId, double pricePerGram,
                                          String paymentMethod, String notes) {
        try {
            Optional<GoldItem> itemOpt = inventoryService.getGoldItem(goldItemId);
            if (itemOpt.isEmpty()) {
                System.err.println("[TRADE] Gold item not found: " + goldItemId);
                return Optional.empty();
            }

            GoldItem item = itemOpt.get();
            if (!"AVAILABLE".equals(item.getStatus())) {
                System.err.println("[TRADE] Gold item is not available for sale: " + item.getStatus());
                return Optional.empty();
            }

            // Create transaction
            Transaction txn = new Transaction(TransactionType.SELL, goldItemId, item.getGoldType(),
                    item.getWeightGrams(), pricePerGram, paymentMethod);
            txn.setCustomerId(customerId);
            txn.setNotes(notes);
            txn.setInvoiceNumber(generateInvoiceNumber("SELL"));

            int txnId = transactionDao.insert(txn);
            if (txnId > 0) {
                // Mark item as sold
                inventoryService.markAsSold(goldItemId);
                return Optional.of(txn);
            }
        } catch (SQLException e) {
            System.err.println("[TRADE] Error selling gold: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Transaction> getAllTransactions() {
        try {
            return transactionDao.findAll();
        } catch (SQLException e) {
            System.err.println("[TRADE] Error listing transactions: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Transaction> getBuyTransactions() {
        try {
            return transactionDao.findByType(TransactionType.BUY);
        } catch (SQLException e) {
            System.err.println("[TRADE] Error listing buy transactions: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Transaction> getSellTransactions() {
        try {
            return transactionDao.findByType(TransactionType.SELL);
        } catch (SQLException e) {
            System.err.println("[TRADE] Error listing sell transactions: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Transaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        try {
            return transactionDao.findByDateRange(start, end);
        } catch (SQLException e) {
            System.err.println("[TRADE] Error listing transactions by date: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Transaction> getCustomerTransactions(int customerId) {
        try {
            return transactionDao.findByCustomer(customerId);
        } catch (SQLException e) {
            System.err.println("[TRADE] Error listing customer transactions: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<Transaction> findByInvoice(String invoiceNumber) {
        try {
            return transactionDao.findByInvoiceNumber(invoiceNumber);
        } catch (SQLException e) {
            System.err.println("[TRADE] Error finding transaction: " + e.getMessage());
            return Optional.empty();
        }
    }

    private String generateInvoiceNumber(String prefix) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int counter = invoiceCounter.getAndIncrement();
        return String.format("GTS-%s-%s-%04d", prefix, dateStr, counter);
    }
}
