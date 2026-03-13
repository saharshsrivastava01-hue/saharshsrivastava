package com.goldtrading.dao;

import com.goldtrading.model.GoldPrice;
import com.goldtrading.model.GoldType;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for GoldPrice operations.
 */
public class GoldPriceDao {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final DatabaseManager dbManager;

    public GoldPriceDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public Optional<GoldPrice> findByType(GoldType goldType) throws SQLException {
        String sql = "SELECT * FROM gold_prices WHERE gold_type = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, goldType.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<GoldPrice> findAll() throws SQLException {
        String sql = "SELECT * FROM gold_prices ORDER BY gold_type";
        List<GoldPrice> prices = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                prices.add(mapResultSet(rs));
            }
        }
        return prices;
    }

    public void updatePrice(GoldType goldType, double buyPrice, double sellPrice) throws SQLException {
        String sql = "INSERT OR REPLACE INTO gold_prices (gold_type, buy_price_per_gram, sell_price_per_gram, " +
                "effective_date, updated_at) VALUES (?, ?, ?, ?, ?)";

        String now = LocalDateTime.now().format(DTF);
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, goldType.name());
            pstmt.setDouble(2, buyPrice);
            pstmt.setDouble(3, sellPrice);
            pstmt.setString(4, now);
            pstmt.setString(5, now);
            pstmt.executeUpdate();
        }
    }

    private GoldPrice mapResultSet(ResultSet rs) throws SQLException {
        GoldPrice price = new GoldPrice();
        price.setId(rs.getInt("id"));
        price.setGoldType(GoldType.valueOf(rs.getString("gold_type")));
        price.setBuyPricePerGram(rs.getDouble("buy_price_per_gram"));
        price.setSellPricePerGram(rs.getDouble("sell_price_per_gram"));

        String effectiveStr = rs.getString("effective_date");
        if (effectiveStr != null) {
            price.setEffectiveDate(LocalDateTime.parse(effectiveStr, DTF));
        }
        String updatedStr = rs.getString("updated_at");
        if (updatedStr != null) {
            price.setUpdatedAt(LocalDateTime.parse(updatedStr, DTF));
        }
        return price;
    }
}
