package com.goldtrader.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:h2:./data/goldtrader;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private static DatabaseManager instance;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    full_name VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL DEFAULT 'USER',
                    active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_login TIMESTAMP
                )
            """);

            // Customers table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    phone VARCHAR(20),
                    email VARCHAR(100),
                    address VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Inventory table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventory (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    gold_type VARCHAR(10) NOT NULL UNIQUE,
                    weight_grams DECIMAL(12,3) NOT NULL DEFAULT 0,
                    avg_cost_per_gram DECIMAL(12,2) NOT NULL DEFAULT 0,
                    total_value DECIMAL(15,2) NOT NULL DEFAULT 0,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Transactions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    type VARCHAR(10) NOT NULL,
                    gold_type VARCHAR(10) NOT NULL,
                    weight_grams DECIMAL(12,3) NOT NULL,
                    price_per_gram DECIMAL(12,2) NOT NULL,
                    total_amount DECIMAL(15,2) NOT NULL,
                    tax_amount DECIMAL(12,2) DEFAULT 0,
                    net_amount DECIMAL(15,2) NOT NULL,
                    customer_id INT,
                    customer_name VARCHAR(100),
                    notes VARCHAR(500),
                    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    created_by VARCHAR(50),
                    FOREIGN KEY (customer_id) REFERENCES customers(id)
                )
            """);

            // Gold prices table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS gold_prices (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    gold_type VARCHAR(10) NOT NULL,
                    price_per_gram DECIMAL(12,2) NOT NULL,
                    effective_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_by VARCHAR(50)
                )
            """);

            // Initialize inventory rows if they don't exist
            for (String type : new String[]{"24K", "22K", "18K", "14K"}) {
                stmt.execute(String.format(
                    "MERGE INTO inventory (gold_type, weight_grams, avg_cost_per_gram, total_value) " +
                    "KEY (gold_type) VALUES ('%s', 0, 0, 0)", type));
            }

            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}
