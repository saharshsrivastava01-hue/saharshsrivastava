package com.goldtrader.ui.panels;

import com.goldtrader.dao.GoldPriceDao;
import com.goldtrader.model.GoldPrice;
import com.goldtrader.model.GoldType;
import com.goldtrader.util.CurrencyUtil;
import com.goldtrader.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GoldPricePanel extends JPanel {
    private final GoldPriceDao goldPriceDao = new GoldPriceDao();
    private JTable currentPriceTable;
    private DefaultTableModel currentPriceModel;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    private JComboBox<GoldType> historyTypeCombo;

    public GoldPricePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    private void initComponents() {
        // Title
        JLabel title = new JLabel("Gold Price Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(255, 179, 0));
        add(title, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 15, 0));

        // Left - Current prices with update
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Current Prices"));

        String[] cols = {"Gold Type", "Purity", "Price/Gram", "Last Updated"};
        currentPriceModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        currentPriceTable = new JTable(currentPriceModel);
        currentPriceTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        currentPriceTable.setRowHeight(32);
        leftPanel.add(new JScrollPane(currentPriceTable), BorderLayout.CENTER);

        // Update price form
        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        updatePanel.setBorder(BorderFactory.createTitledBorder("Update Price"));

        JComboBox<GoldType> typeCombo = new JComboBox<>(GoldType.values());
        updatePanel.add(new JLabel("Type:"));
        updatePanel.add(typeCombo);

        JTextField priceField = new JTextField(10);
        updatePanel.add(new JLabel("New Price/g ($):"));
        updatePanel.add(priceField);

        JButton updateBtn = new JButton("Update");
        updateBtn.setBackground(new Color(255, 179, 0));
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        updateBtn.addActionListener(e -> {
            try {
                GoldType type = (GoldType) typeCombo.getSelectedItem();
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(this, "Price must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String updatedBy = SessionManager.getInstance().getCurrentUsername();
                goldPriceDao.save(new GoldPrice(type, price, updatedBy));
                priceField.setText("");
                refreshData();
                JOptionPane.showMessageDialog(this, "Price updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        updatePanel.add(updateBtn);

        leftPanel.add(updatePanel, BorderLayout.SOUTH);
        mainPanel.add(leftPanel);

        // Right - Price history
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Price History"));

        JPanel historyFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        historyTypeCombo = new JComboBox<>(GoldType.values());
        historyFilterPanel.add(new JLabel("Gold Type:"));
        historyFilterPanel.add(historyTypeCombo);
        JButton viewHistoryBtn = new JButton("View History");
        viewHistoryBtn.addActionListener(e -> loadHistory());
        historyFilterPanel.add(viewHistoryBtn);
        rightPanel.add(historyFilterPanel, BorderLayout.NORTH);

        String[] histCols = {"Price/Gram", "Effective Date", "Updated By"};
        historyModel = new DefaultTableModel(histCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setRowHeight(28);
        rightPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        mainPanel.add(rightPanel);

        add(mainPanel, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        currentPriceModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (GoldType gt : GoldType.values()) {
            java.util.Optional<GoldPrice> priceOpt = goldPriceDao.getLatestPrice(gt);
            currentPriceModel.addRow(new Object[]{
                gt.getLabel(),
                gt.getPurityPercent() + "%",
                CurrencyUtil.formatCurrency(priceOpt.map(GoldPrice::getPricePerGram).orElse(BigDecimal.ZERO)),
                priceOpt.map(p -> p.getEffectiveDate().format(fmt)).orElse("N/A")
            });
        }
        loadHistory();
    }

    private void loadHistory() {
        historyModel.setRowCount(0);
        GoldType type = (GoldType) historyTypeCombo.getSelectedItem();
        if (type == null) return;
        List<GoldPrice> history = goldPriceDao.getPriceHistory(type, 20);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (GoldPrice p : history) {
            historyModel.addRow(new Object[]{
                CurrencyUtil.formatCurrency(p.getPricePerGram()),
                p.getEffectiveDate().format(fmt),
                p.getUpdatedBy()
            });
        }
    }
}
