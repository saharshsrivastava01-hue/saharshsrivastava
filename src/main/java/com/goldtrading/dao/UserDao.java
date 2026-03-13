package com.goldtrading.dao;

import com.goldtrading.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User operations.
 */
public class UserDao {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final DatabaseManager dbManager;

    public UserDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, active, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole());
            pstmt.setInt(5, user.isActive() ? 1 : 0);
            pstmt.setString(6, user.getCreatedAt().format(DTF));
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    user.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND active = 1";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
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

    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY username ASC";
        List<User> users = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSet(rs));
            }
        }
        return users;
    }

    public void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, LocalDateTime.now().format(DTF));
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    public void updatePassword(int userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    public void deactivate(int userId) throws SQLException {
        String sql = "UPDATE users SET active = 0 WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    private User mapResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getInt("active") == 1);

        String lastLoginStr = rs.getString("last_login");
        if (lastLoginStr != null) {
            user.setLastLogin(LocalDateTime.parse(lastLoginStr, DTF));
        }
        String createdStr = rs.getString("created_at");
        if (createdStr != null) {
            user.setCreatedAt(LocalDateTime.parse(createdStr, DTF));
        }
        return user;
    }
}
