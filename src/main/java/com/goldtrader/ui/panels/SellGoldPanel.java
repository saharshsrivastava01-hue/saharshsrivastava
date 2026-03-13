package com.goldtrader.ui.panels;

import com.goldtrader.dao.CustomerDao;
import com.goldtrader.dao.GoldPriceDao;
import com.goldtrader.dao.InventoryDao;
import com.goldtrader.model.Customer;
import com.goldtrader.model.GoldType;
import com.goldtrader.model.Inventory;
import com.goldtrader.service.GoldTradingService;
import com.goldtrader.util.CurrencyUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class SellGoldPanel extends JPanel {
    private final GoldTradingService tradingService = new GoldTradingService();
    private final CustomerDao customerDao = new CustomerDao();
    private final GoldPriceDao goldPriceDao = new GoldPriceDao();
    private final InventoryDao inventoryDao = new InventoryDao();

    private JComboBox<GoldType> goldTypeCombo;
    private JTextField weightField;
    private JTextField pricePerGramField;
    private JLabel totalLabel;
    private JLabel taxLabel;
    private JLabel netLabel;
    private JLabel availableLabel;
    private JComboBox<Customer> customerCombo;
    private JTextArea notesArea;
    private Runnable onTransactionComplete;

    private static final BigDecimal TAX_RATE = new BigDecimal("3.0");

    public SellGoldPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    public void setOnTransactionComplete(Runnable callback) {
        this.onTransactionComplete = callback;
    }

    private void initComponents() {
        // Title
        JLabel title = new JLabel("Sell Gold (Sale to Customer)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(198, 40, 40));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Gold Type
        addLabel(formPanel, gbc, row, "Gold Type:");
        goldTypeCombo = new JComboBox<>(GoldType.values());
        goldTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        goldTypeCombo.addActionListener(e -> { updatePrice(); updateAvailable(); });
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        formPanel.add(goldTypeCombo, gbc);

        row++;

        // Available stock
        addLabel(formPanel, gbc, row, "Available Stock:");
        availableLabel = new JLabel("0.000 g");
        availableLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        availableLabel.setForeground(new Color(21, 101, 192));
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(availableLabel, gbc);

        row++;

        // Weight
        addLabel(formPanel, gbc, row, "Weight (grams):");
        weightField = new JTextField(15);
        weightField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        weightField.getDocument().addDocumentListener(new SimpleDocListener(this::calculateTotal));
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(weightField, gbc);

        row++;

        // Price per gram
        addLabel(formPanel, gbc, row, "Selling Price/Gram ($):");
        pricePerGramField = new JTextField(15);
        pricePerGramField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pricePerGramField.getDocument().addDocumentListener(new SimpleDocListener(this::calculateTotal));
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(pricePerGramField, gbc);

        row++;

        // Subtotal
        addLabel(formPanel, gbc, row, "Subtotal:");
        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(totalLabel, gbc);

        row++;

        // Tax
        addLabel(formPanel, gbc, row, "Tax (3%):");
        taxLabel = new JLabel("$0.00");
        taxLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(taxLabel, gbc);

        row++;

        // Net Total
        addLabel(formPanel, gbc, row, "Net Total:");
        netLabel = new JLabel("$0.00");
        netLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        netLabel.setForeground(new Color(198, 40, 40));
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(netLabel, gbc);

        row++;

        // Customer
        addLabel(formPanel, gbc, row, "Customer:");
        JPanel customerPanel = new JPanel(new BorderLayout(5, 0));
        customerCombo = new JComboBox<>();
        customerCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        refreshCustomers();
        customerPanel.add(customerCombo, BorderLayout.CENTER);
        JButton addCustBtn = new JButton("+");
        addCustBtn.setToolTipText("Quick add customer");
        addCustBtn.addActionListener(e -> quickAddCustomer());
        customerPanel.add(addCustBtn, BorderLayout.EAST);
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(customerPanel, gbc);

        row++;

        // Notes
        addLabel(formPanel, gbc, row, "Notes:");
        notesArea = new JTextArea(3, 15);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setLineWrap(true);
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(new JScrollPane(notesArea), gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton sellBtn = new JButton("Complete Sale");
        sellBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sellBtn.setBackground(new Color(198, 40, 40));
        sellBtn.setForeground(Color.WHITE);
        sellBtn.setPreferredSize(new Dimension(200, 40));
        sellBtn.addActionListener(e -> executeSell());

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clearBtn.setPreferredSize(new Dimension(100, 40));
        clearBtn.addActionListener(e -> clearForm());

        buttonPanel.add(clearBtn);
        buttonPanel.add(sellBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        updatePrice();
        updateAvailable();
    }

    private void addLabel(JPanel panel, GridBagConstraints gbc, int row, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(label, gbc);
    }

    private void updatePrice() {
        GoldType selected = (GoldType) goldTypeCombo.getSelectedItem();
        if (selected != null) {
            BigDecimal price = goldPriceDao.getCurrentPrice(selected);
            pricePerGramField.setText(price.toPlainString());
        }
    }

    private void updateAvailable() {
        GoldType selected = (GoldType) goldTypeCombo.getSelectedItem();
        if (selected != null) {
            Optional<Inventory> inv = inventoryDao.findByGoldType(selected);
            BigDecimal available = inv.map(Inventory::getWeightGrams).orElse(BigDecimal.ZERO);
            availableLabel.setText(CurrencyUtil.formatWeight(available));
        }
    }

    private void calculateTotal() {
        try {
            BigDecimal weight = new BigDecimal(weightField.getText().trim());
            BigDecimal price = new BigDecimal(pricePerGramField.getText().trim());
            BigDecimal subtotal = weight.multiply(price);
            BigDecimal tax = subtotal.multiply(TAX_RATE).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal net = subtotal.add(tax);
            totalLabel.setText(CurrencyUtil.formatCurrency(subtotal));
            taxLabel.setText(CurrencyUtil.formatCurrency(tax));
            netLabel.setText(CurrencyUtil.formatCurrency(net));
        } catch (NumberFormatException e) {
            totalLabel.setText("$0.00");
            taxLabel.setText("$0.00");
            netLabel.setText("$0.00");
        }
    }

    private void executeSell() {
        try {
            GoldType goldType = (GoldType) goldTypeCombo.getSelectedItem();
            BigDecimal weight = CurrencyUtil.parseWeight(weightField.getText());
            BigDecimal price = CurrencyUtil.parseCurrency(pricePerGramField.getText());

            if (weight.compareTo(BigDecimal.ZERO) <= 0 || price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid weight and price.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Customer customer = (Customer) customerCombo.getSelectedItem();
            if (customer == null) {
                JOptionPane.showMessageDialog(this, "Please select a customer.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal subtotal = weight.multiply(price);
            BigDecimal tax = subtotal.multiply(TAX_RATE).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Confirm sale of %s grams of %s @ %s/g\nSubtotal: %s | Tax: %s | Net: %s\nTo: %s",
                            weight.toPlainString(), goldType.getLabel(), CurrencyUtil.formatCurrency(price),
                            CurrencyUtil.formatCurrency(subtotal), CurrencyUtil.formatCurrency(tax),
                            CurrencyUtil.formatCurrency(subtotal.add(tax)), customer.getName()),
                    "Confirm Sale", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                tradingService.sellGold(goldType, weight, price, customer.getId(), customer.getName(), notesArea.getText());
                JOptionPane.showMessageDialog(this, "Sale completed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                if (onTransactionComplete != null) onTransactionComplete.run();
            }
        } catch (GoldTradingService.InsufficientStockException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quickAddCustomer() {
        String name = JOptionPane.showInputDialog(this, "Customer Name:");
        if (name != null && !name.trim().isEmpty()) {
            String phone = JOptionPane.showInputDialog(this, "Phone Number:");
            Customer c = new Customer(name.trim(), phone != null ? phone.trim() : "", "", "");
            customerDao.save(c);
            refreshCustomers();
        }
    }

    public void refreshCustomers() {
        customerCombo.removeAllItems();
        List<Customer> customers = customerDao.findAll();
        for (Customer c : customers) {
            customerCombo.addItem(c);
        }
        updateAvailable();
    }

    private void clearForm() {
        weightField.setText("");
        notesArea.setText("");
        updatePrice();
        updateAvailable();
        totalLabel.setText("$0.00");
        taxLabel.setText("$0.00");
        netLabel.setText("$0.00");
    }
}
