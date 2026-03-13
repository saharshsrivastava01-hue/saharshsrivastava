package com.goldtrading.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Manages SQLite database connections and initialization.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:gold_trading.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        // Private constructor for singleton
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            // Enable WAL mode for better concurrent performance
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA foreign_keys=ON");
            }
        }
        return connection;
    }

    public void initializeDatabase() {
        try {
            Connection conn = getConnection();
            String schema = loadSchema();
            String[] statements = schema.split(";");

            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(trimmed);
                    }
                }
            }
            System.out.println("[DB] Database initialized successfully.");
        } catch (SQLException | IOException e) {
            System.err.println("[DB] Error initializing database: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private String loadSchema() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
            if (is == null) {
                throw new IOException("schema.sql not found in resources");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines()
                        .filter(line -> !line.trim().startsWith("--"))
                        .collect(Collectors.joining("\n"));
            }
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}
