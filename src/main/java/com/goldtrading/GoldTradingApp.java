package com.goldtrading;

import com.goldtrading.dao.DatabaseManager;
import com.goldtrading.ui.MenuHandler;

import java.util.Scanner;

/**
 * Gold Trading Management System - Main Application Entry Point.
 *
 * A professional-grade gold buy/sell management application featuring:
 * - Gold inventory management with multiple karat types (24K, 22K, 18K, 14K)
 * - Buy gold from suppliers with full transaction tracking
 * - Sell gold to customers with invoice generation
 * - Customer and supplier management (CRUD)
 * - Real-time gold price management
 * - Comprehensive reporting (P&L, Inventory, Transactions)
 * - User authentication with role-based access (Admin/Manager/Staff)
 * - SQLite database for persistent storage
 * - GST tax calculation on transactions
 *
 * @author Gold Trading System
 * @version 1.0.0
 */
public class GoldTradingApp {

    private static final String APP_NAME = "Gold Trading Management System";
    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        printBanner();

        // Initialize database
        DatabaseManager dbManager = DatabaseManager.getInstance();
        dbManager.initializeDatabase();

        // Start application
        Scanner scanner = new Scanner(System.in);
        MenuHandler menuHandler = new MenuHandler(scanner);

        try {
            if (menuHandler.handleLogin()) {
                menuHandler.showMainMenu();
            }
        } finally {
            scanner.close();
            dbManager.closeConnection();
            System.out.println("\n  Application terminated. Data saved successfully.");
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                          ║");
        System.out.println("  ║          GOLD TRADING MANAGEMENT SYSTEM                   ║");
        System.out.println("  ║          ─────────────────────────────                    ║");
        System.out.println("  ║                                                          ║");
        System.out.println("  ║   Buy • Sell • Manage • Report                           ║");
        System.out.println("  ║                                                          ║");
        System.out.println("  ║   Version: " + String.format("%-45s", VERSION) + " ║");
        System.out.println("  ║   Professional Gold Trading & Inventory System           ║");
        System.out.println("  ║                                                          ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}
