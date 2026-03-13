package com.goldtrading.model;

import java.time.LocalDateTime;

/**
 * Represents a system user for authentication.
 */
public class User {

    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String role; // ADMIN, MANAGER, STAFF
    private boolean active;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    public User() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String passwordHash, String fullName, String role) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("User[id=%d, username=%s, role=%s, active=%b]",
                id, username, role, active);
    }
}
