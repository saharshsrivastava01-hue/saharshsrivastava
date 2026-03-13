package com.goldtrader.service;

import com.goldtrader.dao.GoldPriceDao;
import com.goldtrader.dao.InventoryDao;
import com.goldtrader.dao.TransactionDao;
import com.goldtrader.model.*;
import com.goldtrader.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class GoldTradingService {
    private static final Logger logger = LoggerFactory.getLogger(GoldTradingService.class);
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("3.0");

    private final TransactionDao transactionDao = new TransactionDao();
    private final InventoryDao inventoryDao = new InventoryDao();
    private final GoldPriceDao goldPriceDao = new GoldPriceDao();

    public Transaction buyGold(GoldType goldType, BigDecimal weightGrams, BigDecimal pricePerGram,
                                int customerId, String customerName, String notes) {
        // Create transaction
        Transaction tx = new Transaction();
        tx.setType(TransactionType.BUY);
        tx.setGoldType(goldType);
        tx.setWeightGrams(weightGrams);
        tx.setPricePerGram(pricePerGram);
        tx.calculateTotals(BigDecimal.ZERO); // No tax on purchases
        tx.setCustomerId(customerId);
        tx.setCustomerName(customerName);
        tx.setNotes(notes);
        tx.setCreatedBy(SessionManager.getInstance().getCurrentUsername());

        // Update inventory - add stock
        Inventory inv = inventoryDao.findByGoldType(goldType).orElse(new Inventory(goldType, BigDecimal.ZERO, BigDecimal.ZERO));
        BigDecimal oldWeight = inv.getWeightGrams();
        BigDecimal oldCost = inv.getAvgCostPerGram();
        BigDecimal newWeight = oldWeight.add(weightGrams);

        // Weighted average cost
        BigDecimal newAvgCost;
        if (newWeight.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal oldTotal = oldWeight.multiply(oldCost);
            BigDecimal newTotal = weightGrams.multiply(pricePerGram);
            newAvgCost = oldTotal.add(newTotal).divide(newWeight, 2, RoundingMode.HALF_UP);
        } else {
            newAvgCost = pricePerGram;
        }

        inventoryDao.updateInventory(goldType, newWeight, newAvgCost);
        Transaction saved = transactionDao.save(tx);
        logger.info("Gold purchased: {} grams of {} @ {}/g from {}", weightGrams, goldType.getLabel(), pricePerGram, customerName);
        return saved;
    }

    public Transaction sellGold(GoldType goldType, BigDecimal weightGrams, BigDecimal pricePerGram,
                                 int customerId, String customerName, String notes) throws InsufficientStockException {
        // Check inventory
        Inventory inv = inventoryDao.findByGoldType(goldType)
                .orElseThrow(() -> new InsufficientStockException("No inventory for " + goldType.getLabel()));

        if (inv.getWeightGrams().compareTo(weightGrams) < 0) {
            throw new InsufficientStockException(
                String.format("Insufficient stock. Available: %.3f g, Requested: %.3f g",
                    inv.getWeightGrams().doubleValue(), weightGrams.doubleValue()));
        }

        // Create transaction
        Transaction tx = new Transaction();
        tx.setType(TransactionType.SELL);
        tx.setGoldType(goldType);
        tx.setWeightGrams(weightGrams);
        tx.setPricePerGram(pricePerGram);
        tx.calculateTotals(DEFAULT_TAX_RATE);
        tx.setCustomerId(customerId);
        tx.setCustomerName(customerName);
        tx.setNotes(notes);
        tx.setCreatedBy(SessionManager.getInstance().getCurrentUsername());

        // Update inventory - remove stock
        BigDecimal newWeight = inv.getWeightGrams().subtract(weightGrams);
        inventoryDao.updateInventory(goldType, newWeight, inv.getAvgCostPerGram());

        Transaction saved = transactionDao.save(tx);
        logger.info("Gold sold: {} grams of {} @ {}/g to {}", weightGrams, goldType.getLabel(), pricePerGram, customerName);
        return saved;
    }

    public BigDecimal getCurrentPrice(GoldType goldType) {
        return goldPriceDao.getCurrentPrice(goldType);
    }

    public void updateGoldPrice(GoldType goldType, BigDecimal pricePerGram) {
        String updatedBy = SessionManager.getInstance().getCurrentUsername();
        goldPriceDao.save(new GoldPrice(goldType, pricePerGram, updatedBy));
    }

    public static class InsufficientStockException extends Exception {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}
