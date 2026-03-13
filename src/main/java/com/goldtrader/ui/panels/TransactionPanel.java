package com.goldtrader.ui.panels;

import com.goldtrader.dao.TransactionDao;
import com.goldtrader.model.Transaction;
import com.goldtrader.model.TransactionType;
import com.goldtrader.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransactionPanel extends JPanel {
    private final TransactionDao transactionDao = new TransactionDao();
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> typeFilter;
    private JTextField startDateField;
    private JTextField endDateField;

    public TransactionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Transaction History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(230, 81, 0));
        headerPanel.add(title, BorderLayout.WEST);

        // Filters
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);

        filterPanel.add(new JLabel("Type:"));
        typeFilter = new JComboBox<>(new String[]{"All", "Purchase", "Sale"});
        typeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterPanel.add(typeFilter);

        filterPanel.add(new JLabel("From:"));
        startDateField = new JTextField(8);
        startDateField.setToolTipText("yyyy-MM-dd");
        startDateField.setText(LocalDate.now().minusMonths(1).toString());
        filterPanel.add(startDateField);

        filterPanel.add(new JLabel("To:"));
        endDateField = new JTextField(8);
        endDateField.setToolTipText("yyyy-MM-dd");
        endDateField.setText(LocalDate.now().toString());
        filterPanel.add(endDateField);

        JButton filterBtn = new JButton("Filter");
        filterBtn.addActionListener(e -> applyFilter());
        filterPanel.add(filterBtn);

        JButton clearBtn = new JButton("Show All");
        clearBtn.addActionListener(e -> refreshData());
        filterPanel.add(clearBtn);

        headerPanel.add(filterPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Type", "Gold Type", "Weight (g)", "Price/g", "Subtotal", "Tax", "Net Amount", "Customer", "Date", "By"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transactionTable.setRowHeight(30);
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        add(new JScrollPane(transactionTable), BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        loadTransactions(null, null, null);
    }

    private void applyFilter() {
        try {
            TransactionType type = null;
            String selected = (String) typeFilter.getSelectedItem();
            if ("Purchase".equals(selected)) type = TransactionType.BUY;
            else if ("Sale".equals(selected)) type = TransactionType.SELL;

            LocalDate start = null;
            LocalDate end = null;
            if (!startDateField.getText().trim().isEmpty()) {
                start = LocalDate.parse(startDateField.getText().trim());
            }
            if (!endDateField.getText().trim().isEmpty()) {
                end = LocalDate.parse(endDateField.getText().trim());
            }

            loadTransactions(type, start, end);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactions(TransactionType type, LocalDate start, LocalDate end) {
        tableModel.setRowCount(0);
        List<Transaction> transactions = transactionDao.findByFilter(type, start, end);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Transaction tx : transactions) {
            tableModel.addRow(new Object[]{
                tx.getId(),
                tx.getType().getLabel(),
                tx.getGoldType().getLabel(),
                CurrencyUtil.formatWeight(tx.getWeightGrams()),
                CurrencyUtil.formatCurrency(tx.getPricePerGram()),
                CurrencyUtil.formatCurrency(tx.getTotalAmount()),
                CurrencyUtil.formatCurrency(tx.getTaxAmount()),
                CurrencyUtil.formatCurrency(tx.getNetAmount()),
                tx.getCustomerName(),
                tx.getTransactionDate().format(fmt),
                tx.getCreatedBy()
            });
        }
    }
}
