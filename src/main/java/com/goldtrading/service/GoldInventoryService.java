package com.goldtrading.service;

import com.goldtrading.dao.GoldItemDao;
import com.goldtrading.dao.GoldPriceDao;
import com.goldtrading.model.GoldItem;
import com.goldtrading.model.GoldPrice;
import com.goldtrading.model.GoldType;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing gold inventory and prices.
 */
public class GoldInventoryService {

    private final GoldItemDao goldItemDao;
    private final GoldPriceDao goldPriceDao;

    public GoldInventoryService() {
        this.goldItemDao = new GoldItemDao();
        this.goldPriceDao = new GoldPriceDao();
    }

    // ---- Gold Item Management ----

    public int addGoldItem(GoldType goldType, double weightGrams, double purchasePrice,
                           double sellingPrice, String description) {
        try {
            GoldItem item = new GoldItem(goldType, weightGrams, purchasePrice, sellingPrice, description);
            return goldItemDao.insert(item);
        } catch (SQLException e) {
            System.err.println("[INVENTORY] Error adding gold item: " + e.getMessage());
            return -1;
        }
    }

    public Optional<GoldItem> getGoldItem(int id) {
        try {
            return goldItemDao.findById(id);
        } catch (SQLException e) {
            System.err.println("[INVENTORY] Error retrieving gold item: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<GoldItem> getAllGoldItems() {
        try {
            return goldItemDao.findAll();
        } catch (SQLException e) {
            System.err.println("[INVENTORY] Error listing gold items: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<GoldItem> getAvailableGoldItems() {
        try {
            return goldItemDao.findByStatus("AVAILABLE");
        } catch (SQLException e) {
            System.err.println("[INVENTORY] Error listing available items: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<GoldItem> getGoldItemsByType(GoldType goldType) {
        try {
            return goldItemDao.findByGoldType(goldType);
        } catch (SQLException e) {
            System.err.println("[INVENTORY] Error listing items by type: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean updateGoldItem(GoldItem item) {
        try {
            goldItemDao.update(item);
            return true;
        } catch (SQLException e) {
            System.err.println("[INVENTORY] Error updating gold item: " + e.getMessage());
            return false;
        }
    }

    public boolean markAsSold(int itemId) {
        try {
            goldItemDao.updateStatus(itemId, "SOLD");
            return true;
        } catch (SQLException e) {
            System.err.println("[INVENTORY] Error marking item as sold: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteGoldItem(int id) {
        try {
            goldItemDao.delete(id);
            return true;
        } catch (SQLException e) {
            System.err.println("[INVENTORY] Error deleting gold item: " + e.getMessage());
            return false;
        }
    }

    public double getTotalInventoryWeight() {
        try {
            return goldItemDao.getTotalInventoryWeight();
        } catch (SQLException e) {
            System.err.println("[INVENTORY] Error getting total weight: " + e.getMessage());
            return 0;
        }
    }

    public double getTotalInventoryValue() {
        try {
            return goldItemDao.getTotalInventoryValue();
        } catch (SQLException e) {
            System.err.println("[INVENTORY] Error getting total value: " + e.getMessage());
            return 0;
        }
    }

    public int getAvailableCount() {
        try {
            return goldItemDao.getAvailableCount();
        } catch (SQLException e) {
            System.err.println("[INVENTORY] Error getting count: " + e.getMessage());
            return 0;
        }
    }

    // ---- Gold Price Management ----

    public List<GoldPrice> getAllPrices() {
        try {
            return goldPriceDao.findAll();
        } catch (SQLException e) {
            System.err.println("[PRICE] Error listing prices: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<GoldPrice> getPrice(GoldType goldType) {
        try {
            return goldPriceDao.findByType(goldType);
        } catch (SQLException e) {
            System.err.println("[PRICE] Error getting price: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean updatePrice(GoldType goldType, double buyPrice, double sellPrice) {
        try {
            goldPriceDao.updatePrice(goldType, buyPrice, sellPrice);
            return true;
        } catch (SQLException e) {
            System.err.println("[PRICE] Error updating price: " + e.getMessage());
            return false;
        }
    }
}
