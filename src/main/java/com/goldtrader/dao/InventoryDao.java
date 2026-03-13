package com.goldtrader.dao;

import com.goldtrader.model.GoldType;
import com.goldtrader.model.Inventory;
import com.goldtrader.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryDao {
    private static final Logger logger = LoggerFactory.getLogger(InventoryDao.class);
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<Inventory> findByGoldType(GoldType goldType) {
        String sql = "SELECT * FROM inventory WHERE gold_type = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, goldType.getLabel());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapInventory(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding inventory for gold type: {}", goldType, e);
        }
        return Optional.empty();
    }

    public List<Inventory> findAll() {
        List<Inventory> inventories = new ArrayList<>();
        String sql = "SELECT * FROM inventory ORDER BY gold_type";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                inventories.add(mapInventory(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all inventory", e);
        }
        return inventories;
    }

    public void updateInventory(GoldType goldType, BigDecimal weightGrams, BigDecimal avgCostPerGram) {
        String sql = "UPDATE inventory SET weight_grams=?, avg_cost_per_gram=?, total_value=?, last_updated=CURRENT_TIMESTAMP WHERE gold_type=?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, weightGrams);
            stmt.setBigDecimal(2, avgCostPerGram);
            stmt.setBigDecimal(3, weightGrams.multiply(avgCostPerGram));
            stmt.setString(4, goldType.getLabel());
            stmt.executeUpdate();
            logger.info("Inventory updated for {}: {} grams @ {}/g", goldType.getLabel(), weightGrams, avgCostPerGram);
        } catch (SQLException e) {
            logger.error("Error updating inventory for gold type: {}", goldType, e);
        }
    }

    public BigDecimal getTotalInventoryValue() {
        String sql = "SELECT COALESCE(SUM(total_value), 0) FROM inventory";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            logger.error("Error calculating total inventory value", e);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalInventoryWeight() {
        String sql = "SELECT COALESCE(SUM(weight_grams), 0) FROM inventory";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            logger.error("Error calculating total inventory weight", e);
        }
        return BigDecimal.ZERO;
    }

    private Inventory mapInventory(ResultSet rs) throws SQLException {
        Inventory inv = new Inventory();
        inv.setId(rs.getInt("id"));
        inv.setGoldType(GoldType.fromLabel(rs.getString("gold_type")));
        inv.setWeightGrams(rs.getBigDecimal("weight_grams"));
        inv.setAvgCostPerGram(rs.getBigDecimal("avg_cost_per_gram"));
        inv.setTotalValue(rs.getBigDecimal("total_value"));
        Timestamp lastUpdated = rs.getTimestamp("last_updated");
        if (lastUpdated != null) inv.setLastUpdated(lastUpdated.toLocalDateTime());
        return inv;
    }
}
