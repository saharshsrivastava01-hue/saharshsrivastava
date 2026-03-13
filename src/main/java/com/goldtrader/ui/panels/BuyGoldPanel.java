package com.goldtrader.ui.panels;

import com.goldtrader.dao.CustomerDao;
import com.goldtrader.dao.GoldPriceDao;
import com.goldtrader.model.Customer;
import com.goldtrader.model.GoldType;
import com.goldtrader.service.GoldTradingService;
import com.goldtrader.util.CurrencyUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class BuyGoldPanel extends JPanel {
    private final GoldTradingService tradingService = new GoldTradingService();
    private final CustomerDao customerDao = new CustomerDao();
    private final GoldPriceDao goldPriceDao = new GoldPriceDao();

    private JComboBox<GoldType> goldTypeCombo;
    private JTextField weightField;
    private JTextField pricePerGramField;
    private JLabel totalLabel;
    private JComboBox<Customer> customerCombo;
    private JTextArea notesArea;
    private Runnable onTransactionComplete;

    public BuyGoldPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    public void setOnTransactionComplete(Runnable callback) {
        this.onTransactionComplete = callback;
    }

    private void initComponents() {
        // Title
        JLabel title = new JLabel("Buy Gold (Purchase from Supplier/Customer)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(46, 125, 50));
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
        goldTypeCombo.addActionListener(e -> updatePrice());
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        formPanel.add(goldTypeCombo, gbc);

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
        addLabel(formPanel, gbc, row, "Price per Gram ($):");
        pricePerGramField = new JTextField(15);
        pricePerGramField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pricePerGramField.getDocument().addDocumentListener(new SimpleDocListener(this::calculateTotal));
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(pricePerGramField, gbc);

        row++;

        // Total
        addLabel(formPanel, gbc, row, "Total Amount:");
        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(new Color(46, 125, 50));
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(totalLabel, gbc);

        row++;

        // Customer
        addLabel(formPanel, gbc, row, "Customer/Supplier:");
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
        JButton buyBtn = new JButton("Complete Purchase");
        buyBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        buyBtn.setBackground(new Color(46, 125, 50));
        buyBtn.setForeground(Color.WHITE);
        buyBtn.setPreferredSize(new Dimension(200, 40));
        buyBtn.addActionListener(e -> executeBuy());

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clearBtn.setPreferredSize(new Dimension(100, 40));
        clearBtn.addActionListener(e -> clearForm());

        buttonPanel.add(clearBtn);
        buttonPanel.add(buyBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        updatePrice();
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

    private void calculateTotal() {
        try {
            BigDecimal weight = new BigDecimal(weightField.getText().trim());
            BigDecimal price = new BigDecimal(pricePerGramField.getText().trim());
            BigDecimal total = weight.multiply(price);
            totalLabel.setText(CurrencyUtil.formatCurrency(total));
        } catch (NumberFormatException e) {
            totalLabel.setText("$0.00");
        }
    }

    private void executeBuy() {
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

            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Confirm purchase of %s grams of %s @ %s/g\nTotal: %s\nFrom: %s",
                            weight.toPlainString(), goldType.getLabel(), CurrencyUtil.formatCurrency(price),
                            CurrencyUtil.formatCurrency(weight.multiply(price)), customer.getName()),
                    "Confirm Purchase", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                tradingService.buyGold(goldType, weight, price, customer.getId(), customer.getName(), notesArea.getText());
                JOptionPane.showMessageDialog(this, "Purchase completed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                if (onTransactionComplete != null) onTransactionComplete.run();
            }
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
    }

    private void clearForm() {
        weightField.setText("");
        notesArea.setText("");
        updatePrice();
        totalLabel.setText("$0.00");
    }
}
