package com.goldtrading.dao;

import com.goldtrading.model.Customer;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Customer operations.
 */
public class CustomerDao {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final DatabaseManager dbManager;

    public CustomerDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, phone, email, address, id_proof, id_number, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, customer.getIdProof());
            pstmt.setString(6, customer.getIdNumber());
            pstmt.setString(7, customer.getCreatedAt().format(DTF));
            pstmt.setString(8, customer.getUpdatedAt().format(DTF));
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    customer.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Optional<Customer> findById(int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ?";
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

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customers ORDER BY name ASC";
        List<Customer> customers = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapResultSet(rs));
            }
        }
        return customers;
    }

    public List<Customer> searchByName(String name) throws SQLException {
        String sql = "SELECT * FROM customers WHERE name LIKE ? ORDER BY name ASC";
        List<Customer> customers = new ArrayList<>();
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSet(rs));
                }
            }
        }
        return customers;
    }

    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET name = ?, phone = ?, email = ?, address = ?, " +
                "id_proof = ?, id_number = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, customer.getIdProof());
            pstmt.setString(6, customer.getIdNumber());
            pstmt.setString(7, LocalDateTime.now().format(DTF));
            pstmt.setInt(8, customer.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Customer mapResultSet(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setName(rs.getString("name"));
        customer.setPhone(rs.getString("phone"));
        customer.setEmail(rs.getString("email"));
        customer.setAddress(rs.getString("address"));
        customer.setIdProof(rs.getString("id_proof"));
        customer.setIdNumber(rs.getString("id_number"));

        String createdStr = rs.getString("created_at");
        if (createdStr != null) {
            customer.setCreatedAt(LocalDateTime.parse(createdStr, DTF));
        }
        String updatedStr = rs.getString("updated_at");
        if (updatedStr != null) {
            customer.setUpdatedAt(LocalDateTime.parse(updatedStr, DTF));
        }
        return customer;
    }
}
