package com.goldtrader.ui.panels;

import com.goldtrader.dao.CustomerDao;
import com.goldtrader.dao.TransactionDao;
import com.goldtrader.model.Customer;
import com.goldtrader.model.Transaction;
import com.goldtrader.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CustomerPanel extends JPanel {
    private final CustomerDao customerDao = new CustomerDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public CustomerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Customer Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(142, 36, 170));
        headerPanel.add(title, BorderLayout.WEST);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setToolTipText("Search by name or phone");
        actionsPanel.add(new JLabel("Search:"));
        actionsPanel.add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchCustomers());
        actionsPanel.add(searchBtn);

        JButton addBtn = new JButton("Add Customer");
        addBtn.setBackground(new Color(46, 125, 50));
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> showAddEditDialog(null));
        actionsPanel.add(addBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());
        actionsPanel.add(refreshBtn);

        headerPanel.add(actionsPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Phone", "Email", "Address", "Created"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        customerTable = new JTable(tableModel);
        customerTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        customerTable.setRowHeight(30);
        customerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(customerTable), BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton editBtn = new JButton("Edit Selected");
        editBtn.addActionListener(e -> editSelected());
        bottomPanel.add(editBtn);

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setForeground(new Color(198, 40, 40));
        deleteBtn.addActionListener(e -> deleteSelected());
        bottomPanel.add(deleteBtn);

        JButton historyBtn = new JButton("View Transaction History");
        historyBtn.addActionListener(e -> viewHistory());
        bottomPanel.add(historyBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        refreshData();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Customer> customers = customerDao.findAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Customer c : customers) {
            tableModel.addRow(new Object[]{
                c.getId(), c.getName(), c.getPhone(), c.getEmail(), c.getAddress(),
                c.getCreatedAt() != null ? c.getCreatedAt().format(fmt) : "N/A"
            });
        }
    }

    private void searchCustomers() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            refreshData();
            return;
        }
        tableModel.setRowCount(0);
        List<Customer> customers = customerDao.search(keyword);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Customer c : customers) {
            tableModel.addRow(new Object[]{
                c.getId(), c.getName(), c.getPhone(), c.getEmail(), c.getAddress(),
                c.getCreatedAt() != null ? c.getCreatedAt().format(fmt) : "N/A"
            });
        }
    }

    private void showAddEditDialog(Customer existing) {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField nameField = new JTextField(existing != null ? existing.getName() : "");
        JTextField phoneField = new JTextField(existing != null ? existing.getPhone() : "");
        JTextField emailField = new JTextField(existing != null ? existing.getEmail() : "");
        JTextField addressField = new JTextField(existing != null ? existing.getAddress() : "");

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Address:"));
        panel.add(addressField);

        String dialogTitle = existing != null ? "Edit Customer" : "Add Customer";
        int result = JOptionPane.showConfirmDialog(this, panel, dialogTitle, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (existing != null) {
                existing.setName(name);
                existing.setPhone(phoneField.getText().trim());
                existing.setEmail(emailField.getText().trim());
                existing.setAddress(addressField.getText().trim());
                customerDao.update(existing);
            } else {
                Customer c = new Customer(name, phoneField.getText().trim(), emailField.getText().trim(), addressField.getText().trim());
                customerDao.save(c);
            }
            refreshData();
        }
    }

    private void editSelected() {
        int row = customerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a customer.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        customerDao.findById(id).ifPresent(c -> showAddEditDialog(c));
    }

    private void deleteSelected() {
        int row = customerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a customer.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete customer: " + name + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            customerDao.delete(id);
            refreshData();
        }
    }

    private void viewHistory() {
        int row = customerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a customer.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int customerId = (int) tableModel.getValueAt(row, 0);
        String customerName = (String) tableModel.getValueAt(row, 1);

        List<Transaction> transactions = transactionDao.findByCustomerId(customerId);

        String[] cols = {"ID", "Type", "Gold", "Weight", "Price/g", "Total", "Date"};
        Object[][] data = new Object[transactions.size()][7];
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (int i = 0; i < transactions.size(); i++) {
            Transaction tx = transactions.get(i);
            data[i] = new Object[]{
                tx.getId(), tx.getType().getLabel(), tx.getGoldType().getLabel(),
                CurrencyUtil.formatWeight(tx.getWeightGrams()), CurrencyUtil.formatCurrency(tx.getPricePerGram()),
                CurrencyUtil.formatCurrency(tx.getNetAmount()), tx.getTransactionDate().format(fmt)
            };
        }

        JTable historyTable = new JTable(data, cols);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.setRowHeight(28);
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new Dimension(700, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Transaction History - " + customerName, JOptionPane.PLAIN_MESSAGE);
    }
}
