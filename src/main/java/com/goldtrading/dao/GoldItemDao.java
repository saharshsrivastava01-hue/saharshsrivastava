package com.goldtrading.dao;

import com.goldtrading.model.GoldItem;
import com.goldtrading.model.GoldType;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for GoldItem operations.
 */
public class GoldItemDao {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final DatabaseManager dbManager;

    public GoldItemDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(GoldItem item) throws SQLException {
        String sql = "INSERT INTO gold_items (gold_type, weight_grams, purchase_price_per_gram, " +
                "selling_price_per_gram, description, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getGoldType().name());
            pstmt.setDouble(2, item.getWeightGrams());
            pstmt.setDouble(3, item.getPurchasePricePerGram());
            pstmt.setDouble(4, item.getSellingPricePerGram());
            pstmt.setString(5, item.getDescription());
            pstmt.setString(6, item.getStatus());
            pstmt.setString(7, item.getCreatedAt().format(DTF));
            pstmt.setString(8, item.getUpdatedAt().format(DTF));
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    item.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Optional<GoldItem> findById(int id) throws SQLException {
        String sql = "SELECT * FROM gold_items WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<GoldItem> findAll() throws SQLException {
        String sql = "SELECT * FROM gold_items ORDER BY created_at DESC";
        List<GoldItem> items = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapResultSet(rs));
            }
        }
        return items;
    }

    public List<GoldItem> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM gold_items WHERE status = ? ORDER BY created_at DESC";
        List<GoldItem> items = new ArrayList<>();
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSet(rs));
                }
            }
        }
        return items;
    }

    public List<GoldItem> findByGoldType(GoldType goldType) throws SQLException {
        String sql = "SELECT * FROM gold_items WHERE gold_type = ? ORDER BY created_at DESC";
        List<GoldItem> items = new ArrayList<>();
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, goldType.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSet(rs));
                }
            }
        }
        return items;
    }

    public void update(GoldItem item) throws SQLException {
        String sql = "UPDATE gold_items SET gold_type = ?, weight_grams = ?, purchase_price_per_gram = ?, " +
                "selling_price_per_gram = ?, description = ?, status = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, item.getGoldType().name());
            pstmt.setDouble(2, item.getWeightGrams());
            pstmt.setDouble(3, item.getPurchasePricePerGram());
            pstmt.setDouble(4, item.getSellingPricePerGram());
            pstmt.setString(5, item.getDescription());
            pstmt.setString(6, item.getStatus());
            pstmt.setString(7, LocalDateTime.now().format(DTF));
            pstmt.setInt(8, item.getId());
            pstmt.executeUpdate();
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE gold_items SET status = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, LocalDateTime.now().format(DTF));
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM gold_items WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public double getTotalInventoryWeight() throws SQLException {
        String sql = "SELECT COALESCE(SUM(weight_grams), 0) FROM gold_items WHERE status = 'AVAILABLE'";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    public double getTotalInventoryValue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(weight_grams * selling_price_per_gram), 0) FROM gold_items WHERE status = 'AVAILABLE'";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    public int getAvailableCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM gold_items WHERE status = 'AVAILABLE'";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private GoldItem mapResultSet(ResultSet rs) throws SQLException {
        GoldItem item = new GoldItem();
        item.setId(rs.getInt("id"));
        item.setGoldType(GoldType.valueOf(rs.getString("gold_type")));
        item.setWeightGrams(rs.getDouble("weight_grams"));
        item.setPurchasePricePerGram(rs.getDouble("purchase_price_per_gram"));
        item.setSellingPricePerGram(rs.getDouble("selling_price_per_gram"));
        item.setDescription(rs.getString("description"));
        item.setStatus(rs.getString("status"));

        String createdStr = rs.getString("created_at");
        if (createdStr != null) {
            item.setCreatedAt(LocalDateTime.parse(createdStr, DTF));
        }
        String updatedStr = rs.getString("updated_at");
        if (updatedStr != null) {
            item.setUpdatedAt(LocalDateTime.parse(updatedStr, DTF));
        }
        return item;
    }
}
