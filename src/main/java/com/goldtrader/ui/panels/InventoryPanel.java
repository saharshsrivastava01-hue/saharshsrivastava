package com.goldtrader.ui.panels;

import com.goldtrader.dao.InventoryDao;
import com.goldtrader.model.GoldType;
import com.goldtrader.model.Inventory;
import com.goldtrader.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class InventoryPanel extends JPanel {
    private final InventoryDao inventoryDao = new InventoryDao();
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JLabel totalWeightLabel;
    private JLabel totalValueLabel;

    public InventoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Inventory Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(21, 101, 192));
        headerPanel.add(title, BorderLayout.WEST);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        JButton adjustBtn = new JButton("Adjust Inventory");
        adjustBtn.addActionListener(e -> showAdjustDialog());
        actionsPanel.add(adjustBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());
        actionsPanel.add(refreshBtn);

        headerPanel.add(actionsPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Gold Type", "Purity", "Weight (grams)", "Avg Cost/Gram", "Total Value", "Last Updated"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        inventoryTable = new JTable(tableModel);
        inventoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inventoryTable.setRowHeight(32);
        inventoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        // Summary
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));

        totalWeightLabel = new JLabel("Total Weight: 0.000 g");
        totalWeightLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryPanel.add(totalWeightLabel);

        totalValueLabel = new JLabel("Total Value: $0.00");
        totalValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalValueLabel.setForeground(new Color(21, 101, 192));
        summaryPanel.add(totalValueLabel);

        add(summaryPanel, BorderLayout.SOUTH);

        refreshData();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Inventory> inventories = inventoryDao.findAll();
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;

        for (Inventory inv : inventories) {
            boolean lowStock = inv.getWeightGrams().compareTo(new BigDecimal("10")) < 0;
            String weightStr = CurrencyUtil.formatWeight(inv.getWeightGrams());
            if (lowStock && inv.getWeightGrams().compareTo(BigDecimal.ZERO) > 0) {
                weightStr += " (LOW)";
            }

            tableModel.addRow(new Object[]{
                inv.getGoldType().getLabel(),
                inv.getGoldType().getPurityPercent() + "%",
                weightStr,
                CurrencyUtil.formatCurrency(inv.getAvgCostPerGram()),
                CurrencyUtil.formatCurrency(inv.getTotalValue()),
                inv.getLastUpdated() != null ? inv.getLastUpdated().toLocalDate().toString() : "N/A"
            });

            totalWeight = totalWeight.add(inv.getWeightGrams());
            totalValue = totalValue.add(inv.getTotalValue());
        }

        totalWeightLabel.setText("Total Weight: " + CurrencyUtil.formatWeight(totalWeight));
        totalValueLabel.setText("Total Value: " + CurrencyUtil.formatCurrency(totalValue));
    }

    private void showAdjustDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JComboBox<GoldType> typeCombo = new JComboBox<>(GoldType.values());
        JTextField weightField = new JTextField();
        JTextField costField = new JTextField();

        panel.add(new JLabel("Gold Type:"));
        panel.add(typeCombo);
        panel.add(new JLabel("New Weight (grams):"));
        panel.add(weightField);
        panel.add(new JLabel("Avg Cost per Gram ($):"));
        panel.add(costField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Adjust Inventory", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                GoldType type = (GoldType) typeCombo.getSelectedItem();
                BigDecimal weight = new BigDecimal(weightField.getText().trim());
                BigDecimal cost = new BigDecimal(costField.getText().trim());
                inventoryDao.updateInventory(type, weight, cost);
                refreshData();
                JOptionPane.showMessageDialog(this, "Inventory adjusted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
