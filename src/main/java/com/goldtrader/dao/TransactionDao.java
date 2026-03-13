package com.goldtrader.dao;

import com.goldtrader.model.GoldType;
import com.goldtrader.model.Transaction;
import com.goldtrader.model.TransactionType;
import com.goldtrader.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {
    private static final Logger logger = LoggerFactory.getLogger(TransactionDao.class);
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Transaction save(Transaction transaction) {
        String sql = """
            INSERT INTO transactions (type, gold_type, weight_grams, price_per_gram, total_amount, 
            tax_amount, net_amount, customer_id, customer_name, notes, transaction_date, created_by) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, transaction.getType().name());
            stmt.setString(2, transaction.getGoldType().getLabel());
            stmt.setBigDecimal(3, transaction.getWeightGrams());
            stmt.setBigDecimal(4, transaction.getPricePerGram());
            stmt.setBigDecimal(5, transaction.getTotalAmount());
            stmt.setBigDecimal(6, transaction.getTaxAmount());
            stmt.setBigDecimal(7, transaction.getNetAmount());
            stmt.setInt(8, transaction.getCustomerId());
            stmt.setString(9, transaction.getCustomerName());
            stmt.setString(10, transaction.getNotes());
            stmt.setTimestamp(11, Timestamp.valueOf(transaction.getTransactionDate()));
            stmt.setString(12, transaction.getCreatedBy());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                transaction.setId(keys.getInt(1));
            }
            logger.info("Transaction saved: {} {} {} grams", transaction.getType(), transaction.getGoldType().getLabel(), transaction.getWeightGrams());
        } catch (SQLException e) {
            logger.error("Error saving transaction", e);
        }
        return transaction;
    }

    public List<Transaction> findAll() {
        return findByFilter(null, null, null);
    }

    public List<Transaction> findByFilter(TransactionType type, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (type != null) {
            sql.append(" AND type = ?");
            params.add(type.name());
        }
        if (startDate != null) {
            sql.append(" AND transaction_date >= ?");
            params.add(Timestamp.valueOf(startDate.atStartOfDay()));
        }
        if (endDate != null) {
            sql.append(" AND transaction_date <= ?");
            params.add(Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
        }
        sql.append(" ORDER BY transaction_date DESC");

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String s) {
                    stmt.setString(i + 1, s);
                } else if (param instanceof Timestamp ts) {
                    stmt.setTimestamp(i + 1, ts);
                }
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapTransaction(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching transactions", e);
        }
        return transactions;
    }

    public List<Transaction> findByCustomerId(int customerId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE customer_id = ? ORDER BY transaction_date DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapTransaction(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching transactions for customer: {}", customerId, e);
        }
        return transactions;
    }

    public int getTodayTransactionCount() {
        String sql = "SELECT COUNT(*) FROM transactions WHERE CAST(transaction_date AS DATE) = CURRENT_DATE";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting today's transactions", e);
        }
        return 0;
    }

    public BigDecimal getTodayTotalAmount() {
        String sql = "SELECT COALESCE(SUM(net_amount), 0) FROM transactions WHERE CAST(transaction_date AS DATE) = CURRENT_DATE AND type = 'SELL'";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            logger.error("Error calculating today's total", e);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalSales(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(net_amount), 0) FROM transactions WHERE type='SELL' AND transaction_date >= ? AND transaction_date < ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            logger.error("Error calculating total sales", e);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalPurchases(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(net_amount), 0) FROM transactions WHERE type='BUY' AND transaction_date >= ? AND transaction_date < ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            logger.error("Error calculating total purchases", e);
        }
        return BigDecimal.ZERO;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setType(TransactionType.valueOf(rs.getString("type")));
        t.setGoldType(GoldType.fromLabel(rs.getString("gold_type")));
        t.setWeightGrams(rs.getBigDecimal("weight_grams"));
        t.setPricePerGram(rs.getBigDecimal("price_per_gram"));
        t.setTotalAmount(rs.getBigDecimal("total_amount"));
        t.setTaxAmount(rs.getBigDecimal("tax_amount"));
        t.setNetAmount(rs.getBigDecimal("net_amount"));
        t.setCustomerId(rs.getInt("customer_id"));
        t.setCustomerName(rs.getString("customer_name"));
        t.setNotes(rs.getString("notes"));
        Timestamp txDate = rs.getTimestamp("transaction_date");
        if (txDate != null) t.setTransactionDate(txDate.toLocalDateTime());
        t.setCreatedBy(rs.getString("created_by"));
        return t;
    }
}
