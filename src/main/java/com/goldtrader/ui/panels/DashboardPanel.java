package com.goldtrader.ui.panels;

import com.goldtrader.dao.GoldPriceDao;
import com.goldtrader.dao.InventoryDao;
import com.goldtrader.dao.TransactionDao;
import com.goldtrader.model.GoldType;
import com.goldtrader.model.Inventory;
import com.goldtrader.util.CurrencyUtil;
import com.goldtrader.util.SessionManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final InventoryDao inventoryDao = new InventoryDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final GoldPriceDao goldPriceDao = new GoldPriceDao();

    private JLabel totalWeightLabel;
    private JLabel totalValueLabel;
    private JLabel todayTxCountLabel;
    private JLabel todaySalesLabel;
    private JPanel pricesPanel;
    private JPanel inventoryPanel;

    public DashboardPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    private void initComponents() {
        // Welcome header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel welcomeLabel = new JLabel("Welcome, " + SessionManager.getInstance().getCurrentUser().getFullName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(255, 215, 0));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center - Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);

        totalWeightLabel = new JLabel("0.000 g", SwingConstants.CENTER);
        statsPanel.add(createStatCard("Total Inventory Weight", totalWeightLabel, new Color(46, 125, 50)));

        totalValueLabel = new JLabel("$0.00", SwingConstants.CENTER);
        statsPanel.add(createStatCard("Total Inventory Value", totalValueLabel, new Color(21, 101, 192)));

        todayTxCountLabel = new JLabel("0", SwingConstants.CENTER);
        statsPanel.add(createStatCard("Today's Transactions", todayTxCountLabel, new Color(230, 81, 0)));

        todaySalesLabel = new JLabel("$0.00", SwingConstants.CENTER);
        statsPanel.add(createStatCard("Today's Sales", todaySalesLabel, new Color(142, 36, 170)));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(statsPanel, BorderLayout.NORTH);

        // Current prices and inventory tables
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        tablesPanel.setOpaque(false);

        pricesPanel = new JPanel(new BorderLayout());
        pricesPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0)), "Current Gold Prices",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(255, 215, 0)));

        inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 181, 246)), "Inventory Summary",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(100, 181, 246)));

        tablesPanel.add(pricesPanel);
        tablesPanel.add(inventoryPanel);
        centerPanel.add(tablesPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        card.add(titleLbl, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    public void refreshData() {
        BigDecimal totalWeight = inventoryDao.getTotalInventoryWeight();
        BigDecimal totalValue = inventoryDao.getTotalInventoryValue();
        int todayCount = transactionDao.getTodayTransactionCount();
        BigDecimal todaySales = transactionDao.getTodayTotalAmount();

        totalWeightLabel.setText(CurrencyUtil.formatWeight(totalWeight));
        totalValueLabel.setText(CurrencyUtil.formatCurrency(totalValue));
        todayTxCountLabel.setText(String.valueOf(todayCount));
        todaySalesLabel.setText(CurrencyUtil.formatCurrency(todaySales));

        // Prices table
        pricesPanel.removeAll();
        String[] priceHeaders = {"Gold Type", "Purity", "Price/Gram"};
        Object[][] priceData = new Object[4][3];
        int i = 0;
        for (GoldType gt : GoldType.values()) {
            BigDecimal price = goldPriceDao.getCurrentPrice(gt);
            priceData[i][0] = gt.getLabel();
            priceData[i][1] = gt.getPurityPercent() + "%";
            priceData[i][2] = CurrencyUtil.formatCurrency(price);
            i++;
        }
        JTable priceTable = new JTable(priceData, priceHeaders);
        priceTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        priceTable.setRowHeight(30);
        priceTable.setEnabled(false);
        pricesPanel.add(new JScrollPane(priceTable), BorderLayout.CENTER);

        // Inventory table
        inventoryPanel.removeAll();
        List<Inventory> inventories = inventoryDao.findAll();
        String[] invHeaders = {"Gold Type", "Weight (g)", "Avg Cost/g", "Total Value"};
        Object[][] invData = new Object[inventories.size()][4];
        for (int j = 0; j < inventories.size(); j++) {
            Inventory inv = inventories.get(j);
            invData[j][0] = inv.getGoldType().getLabel();
            invData[j][1] = CurrencyUtil.formatWeight(inv.getWeightGrams());
            invData[j][2] = CurrencyUtil.formatCurrency(inv.getAvgCostPerGram());
            invData[j][3] = CurrencyUtil.formatCurrency(inv.getTotalValue());
        }
        JTable invTable = new JTable(invData, invHeaders);
        invTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        invTable.setRowHeight(30);
        invTable.setEnabled(false);
        inventoryPanel.add(new JScrollPane(invTable), BorderLayout.CENTER);

        revalidate();
        repaint();
    }
}
