package com.goldtrading.service;

import com.goldtrading.dao.UserDao;
import com.goldtrading.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Service for user authentication and authorization.
 */
public class AuthService {

    private final UserDao userDao;
    private User currentUser;

    public AuthService() {
        this.userDao = new UserDao();
    }

    public boolean login(String username, String password) {
        try {
            String passwordHash = hashPassword(password);
            Optional<User> userOpt = userDao.findByUsername(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getPasswordHash().equals(passwordHash) && user.isActive()) {
                    currentUser = user;
                    userDao.updateLastLogin(user.getId());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[AUTH] Login error: " + e.getMessage());
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    public boolean isManager() {
        return currentUser != null &&
                ("ADMIN".equals(currentUser.getRole()) || "MANAGER".equals(currentUser.getRole()));
    }

    public boolean registerUser(String username, String password, String fullName, String role) {
        try {
            String passwordHash = hashPassword(password);
            User user = new User(username, passwordHash, fullName, role);
            int id = userDao.insert(user);
            return id > 0;
        } catch (SQLException e) {
            System.err.println("[AUTH] Registration error: " + e.getMessage());
            return false;
        }
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser == null) return false;

        try {
            String oldHash = hashPassword(oldPassword);
            if (currentUser.getPasswordHash().equals(oldHash)) {
                String newHash = hashPassword(newPassword);
                userDao.updatePassword(currentUser.getId(), newHash);
                currentUser.setPasswordHash(newHash);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[AUTH] Password change error: " + e.getMessage());
        }
        return false;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
