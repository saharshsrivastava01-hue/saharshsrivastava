package com.goldtrading.dao;

import com.goldtrading.model.GoldType;
import com.goldtrading.model.Transaction;
import com.goldtrading.model.TransactionType;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Transaction operations.
 */
public class TransactionDao {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final DatabaseManager dbManager;

    public TransactionDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (type, gold_item_id, customer_id, supplier_id, gold_type, " +
                "weight_grams, price_per_gram, total_amount, tax_amount, final_amount, payment_method, " +
                "invoice_number, notes, transaction_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, transaction.getType().getCode());
            pstmt.setInt(2, transaction.getGoldItemId());
            pstmt.setInt(3, transaction.getCustomerId());
            pstmt.setInt(4, transaction.getSupplierId());
            pstmt.setString(5, transaction.getGoldType().name());
            pstmt.setDouble(6, transaction.getWeightGrams());
            pstmt.setDouble(7, transaction.getPricePerGram());
            pstmt.setDouble(8, transaction.getTotalAmount());
            pstmt.setDouble(9, transaction.getTaxAmount());
            pstmt.setDouble(10, transaction.getFinalAmount());
            pstmt.setString(11, transaction.getPaymentMethod());
            pstmt.setString(12, transaction.getInvoiceNumber());
            pstmt.setString(13, transaction.getNotes());
            pstmt.setString(14, transaction.getTransactionDate().format(DTF));
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    transaction.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Optional<Transaction> findById(int id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
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

    public List<Transaction> findAll() throws SQLException {
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                transactions.add(mapResultSet(rs));
            }
        }
        return transactions;
    }

    public List<Transaction> findByType(TransactionType type) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE type = ? ORDER BY transaction_date DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, type.getCode());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSet(rs));
                }
            }
        }
        return transactions;
    }

    public List<Transaction> findByDateRange(LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE transaction_date BETWEEN ? AND ? ORDER BY transaction_date DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, start.format(DTF));
            pstmt.setString(2, end.format(DTF));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSet(rs));
                }
            }
        }
        return transactions;
    }

    public List<Transaction> findByCustomer(int customerId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE customer_id = ? ORDER BY transaction_date DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSet(rs));
                }
            }
        }
        return transactions;
    }

    public List<Transaction> findBySupplier(int supplierId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE supplier_id = ? ORDER BY transaction_date DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSet(rs));
                }
            }
        }
        return transactions;
    }

    public double getTotalBuyAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(final_amount), 0) FROM transactions WHERE type = 'BUY'";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    public double getTotalSellAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(final_amount), 0) FROM transactions WHERE type = 'SELL'";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    public int getTransactionCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public double getTotalBuyWeight() throws SQLException {
        String sql = "SELECT COALESCE(SUM(weight_grams), 0) FROM transactions WHERE type = 'BUY'";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    public double getTotalSellWeight() throws SQLException {
        String sql = "SELECT COALESCE(SUM(weight_grams), 0) FROM transactions WHERE type = 'SELL'";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }

    public Optional<Transaction> findByInvoiceNumber(String invoiceNumber) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE invoice_number = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, invoiceNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Transaction mapResultSet(ResultSet rs) throws SQLException {
        Transaction txn = new Transaction();
        txn.setId(rs.getInt("id"));
        txn.setType(TransactionType.fromCode(rs.getString("type")));
        txn.setGoldItemId(rs.getInt("gold_item_id"));
        txn.setCustomerId(rs.getInt("customer_id"));
        txn.setSupplierId(rs.getInt("supplier_id"));
        txn.setGoldType(GoldType.valueOf(rs.getString("gold_type")));
        txn.setWeightGrams(rs.getDouble("weight_grams"));
        txn.setPricePerGram(rs.getDouble("price_per_gram"));
        txn.setTotalAmount(rs.getDouble("total_amount"));
        txn.setTaxAmount(rs.getDouble("tax_amount"));
        txn.setFinalAmount(rs.getDouble("final_amount"));
        txn.setPaymentMethod(rs.getString("payment_method"));
        txn.setInvoiceNumber(rs.getString("invoice_number"));
        txn.setNotes(rs.getString("notes"));

        String dateStr = rs.getString("transaction_date");
        if (dateStr != null) {
            txn.setTransactionDate(LocalDateTime.parse(dateStr, DTF));
        }
        return txn;
    }
}
