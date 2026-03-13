package com.goldtrader.ui.panels;

import com.goldtrader.model.Inventory;
import com.goldtrader.model.TransactionType;
import com.goldtrader.service.ReportService;
import com.goldtrader.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReportsPanel extends JPanel {
    private final ReportService reportService = new ReportService();

    private JTextField startDateField;
    private JTextField endDateField;
    private JLabel salesLabel;
    private JLabel purchasesLabel;
    private JLabel profitLabel;
    private JLabel valuationLabel;
    private JPanel inventoryDetailPanel;

    public ReportsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    private void initComponents() {
        // Title
        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(0, 121, 107));
        add(title, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);

        // Date range and generate button
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        datePanel.setBorder(BorderFactory.createTitledBorder("Report Period"));

        datePanel.add(new JLabel("From:"));
        startDateField = new JTextField(10);
        startDateField.setText(LocalDate.now().withDayOfMonth(1).toString());
        datePanel.add(startDateField);

        datePanel.add(new JLabel("To:"));
        endDateField = new JTextField(10);
        endDateField.setText(LocalDate.now().toString());
        datePanel.add(endDateField);

        JButton generateBtn = new JButton("Generate Report");
        generateBtn.setBackground(new Color(0, 121, 107));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        generateBtn.addActionListener(e -> generateReport());
        datePanel.add(generateBtn);

        JButton exportBtn = new JButton("Export CSV");
        exportBtn.addActionListener(e -> exportCSV());
        datePanel.add(exportBtn);

        mainPanel.add(datePanel, BorderLayout.NORTH);

        // Stats
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        statsPanel.setOpaque(false);

        salesLabel = new JLabel("$0.00", SwingConstants.CENTER);
        statsPanel.add(createReportCard("Total Sales", salesLabel, new Color(46, 125, 50)));

        purchasesLabel = new JLabel("$0.00", SwingConstants.CENTER);
        statsPanel.add(createReportCard("Total Purchases", purchasesLabel, new Color(21, 101, 192)));

        profitLabel = new JLabel("$0.00", SwingConstants.CENTER);
        statsPanel.add(createReportCard("Profit/Loss", profitLabel, new Color(230, 81, 0)));

        valuationLabel = new JLabel("$0.00", SwingConstants.CENTER);
        statsPanel.add(createReportCard("Inventory Valuation", valuationLabel, new Color(142, 36, 170)));

        mainPanel.add(statsPanel, BorderLayout.CENTER);

        // Inventory detail
        inventoryDetailPanel = new JPanel(new BorderLayout());
        inventoryDetailPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 121, 107)), "Inventory Breakdown",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(0, 121, 107)));
        mainPanel.add(inventoryDetailPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        generateReport();
    }

    private JPanel createReportCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(titleLbl, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void generateReport() {
        try {
            LocalDate start = LocalDate.parse(startDateField.getText().trim());
            LocalDate end = LocalDate.parse(endDateField.getText().trim());

            BigDecimal sales = reportService.getTotalSales(start, end);
            BigDecimal purchases = reportService.getTotalPurchases(start, end);
            BigDecimal profit = reportService.getProfit(start, end);
            BigDecimal valuation = reportService.getInventoryValuation();

            salesLabel.setText(CurrencyUtil.formatCurrency(sales));
            purchasesLabel.setText(CurrencyUtil.formatCurrency(purchases));
            profitLabel.setText(CurrencyUtil.formatCurrency(profit));
            if (profit.compareTo(BigDecimal.ZERO) < 0) {
                profitLabel.setForeground(new Color(198, 40, 40));
            } else {
                profitLabel.setForeground(new Color(46, 125, 50));
            }
            valuationLabel.setText(CurrencyUtil.formatCurrency(valuation));

            // Inventory breakdown
            inventoryDetailPanel.removeAll();
            List<Inventory> inventories = reportService.getInventorySummary();
            String[] headers = {"Gold Type", "Weight (g)", "Avg Cost/g", "Total Value"};
            Object[][] data = new Object[inventories.size()][4];
            for (int i = 0; i < inventories.size(); i++) {
                Inventory inv = inventories.get(i);
                data[i] = new Object[]{
                    inv.getGoldType().getLabel(),
                    CurrencyUtil.formatWeight(inv.getWeightGrams()),
                    CurrencyUtil.formatCurrency(inv.getAvgCostPerGram()),
                    CurrencyUtil.formatCurrency(inv.getTotalValue())
                };
            }
            JTable invTable = new JTable(data, headers);
            invTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            invTable.setRowHeight(28);
            invTable.setEnabled(false);
            inventoryDetailPanel.add(new JScrollPane(invTable), BorderLayout.CENTER);

            revalidate();
            repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("transactions_report.csv"));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                LocalDate start = LocalDate.parse(startDateField.getText().trim());
                LocalDate end = LocalDate.parse(endDateField.getText().trim());
                reportService.exportTransactionsToCSV(fileChooser.getSelectedFile().getAbsolutePath(), null, start, end);
                JOptionPane.showMessageDialog(this, "Report exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
