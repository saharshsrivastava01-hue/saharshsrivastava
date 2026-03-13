package com.goldtrader;

import com.formdev.flatlaf.FlatDarkLaf;
import com.goldtrader.dao.GoldPriceDao;
import com.goldtrader.ui.LoginDialog;
import com.goldtrader.ui.MainFrame;
import com.goldtrader.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Gold Trading Management System - Main Application Entry Point.
 *
 * A professional-grade gold buying and selling management software
 * featuring inventory management, customer management, transaction tracking,
 * reporting, and gold price management.
 */
public class GoldTraderApp {
    private static final Logger logger = LoggerFactory.getLogger(GoldTraderApp.class);

    public static void main(String[] args) {
        logger.info("Starting Gold Trading Management System...");

        // Set modern dark look and feel
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 5);
        } catch (Exception e) {
            logger.warn("Failed to set FlatLaf look and feel, using system default", e);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                logger.error("Failed to set system look and feel", ex);
            }
        }

        // Initialize database
        DatabaseManager.getInstance();

        // Initialize default gold prices
        new GoldPriceDao().initializeDefaultPrices();

        // Launch UI
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);

            if (loginDialog.isAuthenticated()) {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
                logger.info("Application started successfully");
            } else {
                logger.info("Login cancelled. Exiting application.");
                System.exit(0);
            }
        });
    }
}
