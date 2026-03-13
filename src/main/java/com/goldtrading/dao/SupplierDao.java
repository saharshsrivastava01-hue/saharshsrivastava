package com.goldtrading.dao;

import com.goldtrading.model.Supplier;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Supplier operations.
 */
public class SupplierDao {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final DatabaseManager dbManager;

    public SupplierDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO suppliers (name, phone, email, address, company_name, license_number, rating, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getPhone());
            pstmt.setString(3, supplier.getEmail());
            pstmt.setString(4, supplier.getAddress());
            pstmt.setString(5, supplier.getCompanyName());
            pstmt.setString(6, supplier.getLicenseNumber());
            pstmt.setDouble(7, supplier.getRating());
            pstmt.setString(8, supplier.getCreatedAt().format(DTF));
            pstmt.setString(9, supplier.getUpdatedAt().format(DTF));
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    supplier.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Optional<Supplier> findById(int id) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
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

    public List<Supplier> findAll() throws SQLException {
        String sql = "SELECT * FROM suppliers ORDER BY name ASC";
        List<Supplier> suppliers = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                suppliers.add(mapResultSet(rs));
            }
        }
        return suppliers;
    }

    public List<Supplier> searchByName(String name) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE name LIKE ? OR company_name LIKE ? ORDER BY name ASC";
        List<Supplier> suppliers = new ArrayList<>();
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            pstmt.setString(2, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSet(rs));
                }
            }
        }
        return suppliers;
    }

    public void update(Supplier supplier) throws SQLException {
        String sql = "UPDATE suppliers SET name = ?, phone = ?, email = ?, address = ?, " +
                "company_name = ?, license_number = ?, rating = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getPhone());
            pstmt.setString(3, supplier.getEmail());
            pstmt.setString(4, supplier.getAddress());
            pstmt.setString(5, supplier.getCompanyName());
            pstmt.setString(6, supplier.getLicenseNumber());
            pstmt.setDouble(7, supplier.getRating());
            pstmt.setString(8, LocalDateTime.now().format(DTF));
            pstmt.setInt(9, supplier.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM suppliers";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Supplier mapResultSet(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setId(rs.getInt("id"));
        supplier.setName(rs.getString("name"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setEmail(rs.getString("email"));
        supplier.setAddress(rs.getString("address"));
        supplier.setCompanyName(rs.getString("company_name"));
        supplier.setLicenseNumber(rs.getString("license_number"));
        supplier.setRating(rs.getDouble("rating"));

        String createdStr = rs.getString("created_at");
        if (createdStr != null) {
            supplier.setCreatedAt(LocalDateTime.parse(createdStr, DTF));
        }
        String updatedStr = rs.getString("updated_at");
        if (updatedStr != null) {
            supplier.setUpdatedAt(LocalDateTime.parse(updatedStr, DTF));
        }
        return supplier;
    }
}
