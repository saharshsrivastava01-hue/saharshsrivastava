package com.goldtrader.dao;

import com.goldtrader.model.GoldPrice;
import com.goldtrader.model.GoldType;
import com.goldtrader.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GoldPriceDao {
    private static final Logger logger = LoggerFactory.getLogger(GoldPriceDao.class);
    private final DatabaseManager db = DatabaseManager.getInstance();

    public GoldPrice save(GoldPrice price) {
        String sql = "INSERT INTO gold_prices (gold_type, price_per_gram, updated_by) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, price.getGoldType().getLabel());
            stmt.setBigDecimal(2, price.getPricePerGram());
            stmt.setString(3, price.getUpdatedBy());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                price.setId(keys.getInt(1));
            }
            logger.info("Gold price saved: {} @ {}/g", price.getGoldType().getLabel(), price.getPricePerGram());
        } catch (SQLException e) {
            logger.error("Error saving gold price", e);
        }
        return price;
    }

    public Optional<GoldPrice> getLatestPrice(GoldType goldType) {
        String sql = "SELECT * FROM gold_prices WHERE gold_type = ? ORDER BY effective_date DESC LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, goldType.getLabel());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapGoldPrice(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting latest price for: {}", goldType, e);
        }
        return Optional.empty();
    }

    public BigDecimal getCurrentPrice(GoldType goldType) {
        return getLatestPrice(goldType).map(GoldPrice::getPricePerGram).orElse(BigDecimal.ZERO);
    }

    public List<GoldPrice> getPriceHistory(GoldType goldType, int limit) {
        List<GoldPrice> prices = new ArrayList<>();
        String sql = "SELECT * FROM gold_prices WHERE gold_type = ? ORDER BY effective_date DESC LIMIT ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, goldType.getLabel());
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                prices.add(mapGoldPrice(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting price history for: {}", goldType, e);
        }
        return prices;
    }

    public void initializeDefaultPrices() {
        if (getCurrentPrice(GoldType.GOLD_24K).compareTo(BigDecimal.ZERO) == 0) {
            save(new GoldPrice(GoldType.GOLD_24K, new BigDecimal("65.00"), "system"));
            save(new GoldPrice(GoldType.GOLD_22K, new BigDecimal("59.50"), "system"));
            save(new GoldPrice(GoldType.GOLD_18K, new BigDecimal("48.75"), "system"));
            save(new GoldPrice(GoldType.GOLD_14K, new BigDecimal("37.90"), "system"));
            logger.info("Default gold prices initialized");
        }
    }

    private GoldPrice mapGoldPrice(ResultSet rs) throws SQLException {
        GoldPrice p = new GoldPrice();
        p.setId(rs.getInt("id"));
        p.setGoldType(GoldType.fromLabel(rs.getString("gold_type")));
        p.setPricePerGram(rs.getBigDecimal("price_per_gram"));
        Timestamp effectiveDate = rs.getTimestamp("effective_date");
        if (effectiveDate != null) p.setEffectiveDate(effectiveDate.toLocalDateTime());
        p.setUpdatedBy(rs.getString("updated_by"));
        return p;
    }
}
