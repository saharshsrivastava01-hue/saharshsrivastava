package com.goldtrading.ui;

import com.goldtrading.model.*;
import com.goldtrading.service.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Handles all menu interactions and user interface flow.
 */
public class MenuHandler {

    private final InputValidator input;
    private final AuthService authService;
    private final GoldInventoryService inventoryService;
    private final CustomerService customerService;
    private final SupplierService supplierService;
    private final TradingService tradingService;
    private final ReportService reportService;

    public MenuHandler(Scanner scanner) {
        this.input = new InputValidator(scanner);
        this.authService = new AuthService();
        this.inventoryService = new GoldInventoryService();
        this.customerService = new CustomerService();
        this.supplierService = new SupplierService();
        this.tradingService = new TradingService(inventoryService);
        this.reportService = new ReportService(inventoryService, customerService, supplierService);
    }

    // ========== LOGIN ==========

    public boolean handleLogin() {
        printHeader("GOLD TRADING MANAGEMENT SYSTEM - LOGIN");
        System.out.println("  Default credentials: admin / admin123\n");

        for (int attempts = 0; attempts < 3; attempts++) {
            String username = input.readNonEmpty("  Username: ");
            String password = input.readNonEmpty("  Password: ");

            if (authService.login(username, password)) {
                System.out.printf("\n  Welcome, %s! (Role: %s)\n",
                        authService.getCurrentUser().getFullName(),
                        authService.getCurrentUser().getRole());
                return true;
            }
            System.out.printf("  [!] Invalid credentials. %d attempt(s) remaining.\n\n", 2 - attempts);
        }
        System.out.println("  [!] Too many failed attempts. Exiting.");
        return false;
    }

    // ========== MAIN MENU ==========

    public void showMainMenu() {
        while (true) {
            printHeader("MAIN MENU");
            System.out.println("  1. Dashboard");
            System.out.println("  2. Buy Gold (Purchase from Supplier)");
            System.out.println("  3. Sell Gold (Sale to Customer)");
            System.out.println("  4. Gold Inventory Management");
            System.out.println("  5. Gold Price Management");
            System.out.println("  6. Customer Management");
            System.out.println("  7. Supplier Management");
            System.out.println("  8. Transaction History");
            System.out.println("  9. Reports & Analytics");
            if (authService.isAdmin()) {
                System.out.println("  10. User Management");
            }
            System.out.println("  0. Logout & Exit");
            System.out.println();

            int choice = input.readInt("  Enter your choice: ");

            switch (choice) {
                case 1 -> showDashboard();
                case 2 -> handleBuyGold();
                case 3 -> handleSellGold();
                case 4 -> showInventoryMenu();
                case 5 -> showPriceMenu();
                case 6 -> showCustomerMenu();
                case 7 -> showSupplierMenu();
                case 8 -> showTransactionHistory();
                case 9 -> showReportsMenu();
                case 10 -> {
                    if (authService.isAdmin()) handleUserManagement();
                    else System.out.println("  [!] Access denied.");
                }
                case 0 -> {
                    authService.logout();
                    System.out.println("\n  Goodbye! Thank you for using Gold Trading System.");
                    return;
                }
                default -> System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    // ========== DASHBOARD ==========

    private void showDashboard() {
        printHeader("DASHBOARD");
        Map<String, Object> summary = reportService.getDashboardSummary();

        System.out.println("  +--------------------------------------------+");
        System.out.printf("  | Inventory Items:     %20d |\n", (int) summary.getOrDefault("totalInventoryItems", 0));
        System.out.printf("  | Inventory Weight:    %17.2f g |\n", (double) summary.getOrDefault("totalInventoryWeight", 0.0));
        System.out.printf("  | Inventory Value:     %14.2f INR |\n", (double) summary.getOrDefault("totalInventoryValue", 0.0));
        System.out.println("  +--------------------------------------------+");
        System.out.printf("  | Total Customers:     %20d |\n", (int) summary.getOrDefault("totalCustomers", 0));
        System.out.printf("  | Total Suppliers:     %20d |\n", (int) summary.getOrDefault("totalSuppliers", 0));
        System.out.printf("  | Total Transactions:  %20d |\n", (int) summary.getOrDefault("totalTransactions", 0));
        System.out.println("  +--------------------------------------------+");
        System.out.printf("  | Total Purchases:     %14.2f INR |\n", (double) summary.getOrDefault("totalBuyAmount", 0.0));
        System.out.printf("  | Total Sales:         %14.2f INR |\n", (double) summary.getOrDefault("totalSellAmount", 0.0));
        System.out.printf("  | Gross Profit:        %14.2f INR |\n", (double) summary.getOrDefault("grossProfit", 0.0));
        System.out.println("  +--------------------------------------------+");

        // Show current gold prices
        System.out.println("\n  Current Gold Prices (per gram):");
        System.out.println("  " + "-".repeat(50));
        List<GoldPrice> prices = inventoryService.getAllPrices();
        for (GoldPrice price : prices) {
            System.out.printf("  %-10s | Buy: %10.2f | Sell: %10.2f\n",
                    price.getGoldType().getLabel(), price.getBuyPricePerGram(), price.getSellPricePerGram());
        }
        System.out.println("  " + "-".repeat(50));

        pressEnterToContinue();
    }

    // ========== BUY GOLD ==========

    private void handleBuyGold() {
        printHeader("BUY GOLD - Purchase from Supplier");

        // Select supplier
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        if (suppliers.isEmpty()) {
            System.out.println("  [!] No suppliers found. Please add a supplier first.");
            if (input.readConfirmation("  Would you like to add a supplier now?")) {
                handleAddSupplier();
                suppliers = supplierService.getAllSuppliers();
                if (suppliers.isEmpty()) return;
            } else {
                return;
            }
        }

        System.out.println("\n  Available Suppliers:");
        for (Supplier s : suppliers) {
            System.out.printf("    %d. %s (%s) - Rating: %.1f\n",
                    s.getId(), s.getName(), s.getCompanyName(), s.getRating());
        }
        int supplierId = input.readInt("\n  Select Supplier ID: ");

        // Select gold type
        System.out.println("\n  Gold Types:");
        GoldType[] types = GoldType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("    %d. %s\n", i + 1, types[i]);
        }
        int typeChoice = input.readIntInRange("  Select Gold Type (1-4): ", 1, 4);
        GoldType goldType = types[typeChoice - 1];

        // Get details
        double weight = input.readPositiveDouble("  Weight in grams: ");
        double pricePerGram = input.readPositiveDouble("  Purchase price per gram (INR): ");

        System.out.println("\n  Payment Methods: 1. CASH  2. BANK_TRANSFER  3. CHEQUE  4. UPI");
        int paymentChoice = input.readIntInRange("  Select Payment Method (1-4): ", 1, 4);
        String[] payments = {"CASH", "BANK_TRANSFER", "CHEQUE", "UPI"};
        String paymentMethod = payments[paymentChoice - 1];

        String description = input.readString("  Description (optional): ");
        String notes = input.readString("  Notes (optional): ");

        // Summary
        double total = weight * pricePerGram;
        double tax = total * 0.03;
        double finalAmount = total + tax;

        System.out.println("\n  --- Purchase Summary ---");
        System.out.printf("  Gold Type:    %s\n", goldType.getLabel());
        System.out.printf("  Weight:       %.2f grams\n", weight);
        System.out.printf("  Price/gram:   %.2f INR\n", pricePerGram);
        System.out.printf("  Subtotal:     %.2f INR\n", total);
        System.out.printf("  GST (3%%):     %.2f INR\n", tax);
        System.out.printf("  Total:        %.2f INR\n", finalAmount);
        System.out.printf("  Payment:      %s\n", paymentMethod);

        if (input.readConfirmation("\n  Confirm purchase?")) {
            Optional<Transaction> txn = tradingService.buyGold(supplierId, goldType, weight,
                    pricePerGram, paymentMethod, description, notes);
            if (txn.isPresent()) {
                System.out.printf("\n  Purchase successful! Invoice: %s\n", txn.get().getInvoiceNumber());
                System.out.printf("  Transaction ID: %d\n", txn.get().getId());
            } else {
                System.out.println("  [!] Purchase failed. Please try again.");
            }
        } else {
            System.out.println("  Purchase cancelled.");
        }

        pressEnterToContinue();
    }

    // ========== SELL GOLD ==========

    private void handleSellGold() {
        printHeader("SELL GOLD - Sale to Customer");

        // Show available inventory
        List<GoldItem> available = inventoryService.getAvailableGoldItems();
        if (available.isEmpty()) {
            System.out.println("  [!] No gold items available for sale.");
            pressEnterToContinue();
            return;
        }

        System.out.println("\n  Available Gold Items:");
        System.out.printf("  %-6s | %-8s | %12s | %12s | %s\n",
                "ID", "Type", "Weight (g)", "Price/g", "Description");
        System.out.println("  " + "-".repeat(70));
        for (GoldItem item : available) {
            System.out.printf("  %-6d | %-8s | %12.2f | %12.2f | %s\n",
                    item.getId(), item.getGoldType().getLabel(), item.getWeightGrams(),
                    item.getSellingPricePerGram(),
                    item.getDescription() != null ? item.getDescription() : "N/A");
        }

        int itemId = input.readInt("\n  Select Gold Item ID to sell: ");
        Optional<GoldItem> itemOpt = inventoryService.getGoldItem(itemId);
        if (itemOpt.isEmpty() || !"AVAILABLE".equals(itemOpt.get().getStatus())) {
            System.out.println("  [!] Invalid or unavailable item.");
            pressEnterToContinue();
            return;
        }
        GoldItem item = itemOpt.get();

        // Select or create customer
        List<Customer> customers = customerService.getAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("  [!] No customers found. Please add a customer first.");
            if (input.readConfirmation("  Would you like to add a customer now?")) {
                handleAddCustomer();
                customers = customerService.getAllCustomers();
                if (customers.isEmpty()) return;
            } else {
                return;
            }
        }

        System.out.println("\n  Customers:");
        for (Customer c : customers) {
            System.out.printf("    %d. %s (Phone: %s)\n", c.getId(), c.getName(), c.getPhone());
        }
        int customerId = input.readInt("\n  Select Customer ID: ");

        // Get selling price
        System.out.printf("  Current selling price: %.2f INR/gram\n", item.getSellingPricePerGram());
        String priceInput = input.readString("  Enter selling price per gram (press Enter for default): ");
        double sellPrice;
        if (priceInput.isEmpty()) {
            sellPrice = item.getSellingPricePerGram();
        } else {
            try {
                sellPrice = Double.parseDouble(priceInput);
                if (sellPrice <= 0) {
                    System.out.println("  [!] Price must be greater than 0. Using default price.");
                    sellPrice = item.getSellingPricePerGram();
                }
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number. Using default price.");
                sellPrice = item.getSellingPricePerGram();
            }
        }

        System.out.println("\n  Payment Methods: 1. CASH  2. BANK_TRANSFER  3. CHEQUE  4. UPI");
        int paymentChoice = input.readIntInRange("  Select Payment Method (1-4): ", 1, 4);
        String[] payments = {"CASH", "BANK_TRANSFER", "CHEQUE", "UPI"};
        String paymentMethod = payments[paymentChoice - 1];

        String notes = input.readString("  Notes (optional): ");

        // Summary
        double total = item.getWeightGrams() * sellPrice;
        double tax = total * 0.03;
        double finalAmount = total + tax;

        System.out.println("\n  --- Sale Summary ---");
        System.out.printf("  Gold Type:    %s\n", item.getGoldType().getLabel());
        System.out.printf("  Weight:       %.2f grams\n", item.getWeightGrams());
        System.out.printf("  Price/gram:   %.2f INR\n", sellPrice);
        System.out.printf("  Subtotal:     %.2f INR\n", total);
        System.out.printf("  GST (3%%):     %.2f INR\n", tax);
        System.out.printf("  Total:        %.2f INR\n", finalAmount);
        System.out.printf("  Payment:      %s\n", paymentMethod);

        if (input.readConfirmation("\n  Confirm sale?")) {
            Optional<Transaction> txn = tradingService.sellGold(customerId, itemId, sellPrice,
                    paymentMethod, notes);
            if (txn.isPresent()) {
                System.out.printf("\n  Sale successful! Invoice: %s\n", txn.get().getInvoiceNumber());
                System.out.printf("  Transaction ID: %d\n", txn.get().getId());
            } else {
                System.out.println("  [!] Sale failed. Please try again.");
            }
        } else {
            System.out.println("  Sale cancelled.");
        }

        pressEnterToContinue();
    }

    // ========== INVENTORY MENU ==========

    private void showInventoryMenu() {
        while (true) {
            printHeader("GOLD INVENTORY MANAGEMENT");
            System.out.println("  1. View All Inventory");
            System.out.println("  2. View Available Items");
            System.out.println("  3. View Items by Gold Type");
            System.out.println("  4. Add Gold Item Manually");
            System.out.println("  5. Update Gold Item");
            System.out.println("  6. Delete Gold Item");
            System.out.println("  0. Back to Main Menu");

            int choice = input.readInt("\n  Enter your choice: ");

            switch (choice) {
                case 1 -> showAllInventory();
                case 2 -> showAvailableInventory();
                case 3 -> showInventoryByType();
                case 4 -> handleAddGoldItem();
                case 5 -> handleUpdateGoldItem();
                case 6 -> handleDeleteGoldItem();
                case 0 -> { return; }
                default -> System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void showAllInventory() {
        List<GoldItem> items = inventoryService.getAllGoldItems();
        printGoldItemTable("ALL GOLD INVENTORY", items);
        pressEnterToContinue();
    }

    private void showAvailableInventory() {
        List<GoldItem> items = inventoryService.getAvailableGoldItems();
        printGoldItemTable("AVAILABLE GOLD ITEMS", items);
        pressEnterToContinue();
    }

    private void showInventoryByType() {
        System.out.println("\n  Gold Types:");
        GoldType[] types = GoldType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("    %d. %s\n", i + 1, types[i].getLabel());
        }
        int choice = input.readIntInRange("  Select Type (1-4): ", 1, 4);
        GoldType type = types[choice - 1];
        List<GoldItem> items = inventoryService.getGoldItemsByType(type);
        printGoldItemTable(type.getLabel() + " GOLD ITEMS", items);
        pressEnterToContinue();
    }

    private void handleAddGoldItem() {
        printHeader("ADD NEW GOLD ITEM");

        GoldType[] types = GoldType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("    %d. %s\n", i + 1, types[i]);
        }
        int typeChoice = input.readIntInRange("  Select Gold Type (1-4): ", 1, 4);
        GoldType goldType = types[typeChoice - 1];

        double weight = input.readPositiveDouble("  Weight in grams: ");
        double purchasePrice = input.readPositiveDouble("  Purchase price per gram (INR): ");
        double sellingPrice = input.readPositiveDouble("  Selling price per gram (INR): ");
        String description = input.readString("  Description: ");

        int id = inventoryService.addGoldItem(goldType, weight, purchasePrice, sellingPrice, description);
        if (id > 0) {
            System.out.printf("  Gold item added successfully! ID: %d\n", id);
        } else {
            System.out.println("  [!] Failed to add gold item.");
        }
        pressEnterToContinue();
    }

    private void handleUpdateGoldItem() {
        int id = input.readInt("  Enter Gold Item ID to update: ");
        Optional<GoldItem> itemOpt = inventoryService.getGoldItem(id);
        if (itemOpt.isEmpty()) {
            System.out.println("  [!] Item not found.");
            return;
        }
        GoldItem item = itemOpt.get();
        System.out.printf("  Current: %s, %.2fg, Buy: %.2f/g, Sell: %.2f/g\n",
                item.getGoldType().getLabel(), item.getWeightGrams(),
                item.getPurchasePricePerGram(), item.getSellingPricePerGram());

        String weightStr = input.readString("  New weight (Enter to skip): ");
        if (!weightStr.isEmpty()) item.setWeightGrams(Double.parseDouble(weightStr));

        String buyStr = input.readString("  New purchase price/gram (Enter to skip): ");
        if (!buyStr.isEmpty()) item.setPurchasePricePerGram(Double.parseDouble(buyStr));

        String sellStr = input.readString("  New selling price/gram (Enter to skip): ");
        if (!sellStr.isEmpty()) item.setSellingPricePerGram(Double.parseDouble(sellStr));

        String desc = input.readString("  New description (Enter to skip): ");
        if (!desc.isEmpty()) item.setDescription(desc);

        if (inventoryService.updateGoldItem(item)) {
            System.out.println("  Item updated successfully!");
        } else {
            System.out.println("  [!] Failed to update item.");
        }
        pressEnterToContinue();
    }

    private void handleDeleteGoldItem() {
        int id = input.readInt("  Enter Gold Item ID to delete: ");
        if (input.readConfirmation("  Are you sure you want to delete this item?")) {
            if (inventoryService.deleteGoldItem(id)) {
                System.out.println("  Item deleted successfully!");
            } else {
                System.out.println("  [!] Failed to delete item.");
            }
        }
        pressEnterToContinue();
    }

    private void printGoldItemTable(String title, List<GoldItem> items) {
        System.out.println("\n  " + title);
        System.out.println("  " + "-".repeat(90));
        System.out.printf("  %-6s | %-8s | %10s | %12s | %12s | %-10s | %s\n",
                "ID", "Type", "Weight(g)", "Buy Price/g", "Sell Price/g", "Status", "Description");
        System.out.println("  " + "-".repeat(90));

        if (items.isEmpty()) {
            System.out.println("  No items found.");
        } else {
            for (GoldItem item : items) {
                System.out.printf("  %-6d | %-8s | %10.2f | %12.2f | %12.2f | %-10s | %s\n",
                        item.getId(), item.getGoldType().getLabel(), item.getWeightGrams(),
                        item.getPurchasePricePerGram(), item.getSellingPricePerGram(),
                        item.getStatus(),
                        item.getDescription() != null ? item.getDescription() : "N/A");
            }
        }
        System.out.println("  " + "-".repeat(90));
        System.out.printf("  Total items: %d\n", items.size());
    }

    // ========== PRICE MANAGEMENT ==========

    private void showPriceMenu() {
        while (true) {
            printHeader("GOLD PRICE MANAGEMENT");
            System.out.println("  1. View Current Prices");
            System.out.println("  2. Update Gold Prices");
            System.out.println("  0. Back to Main Menu");

            int choice = input.readInt("\n  Enter your choice: ");
            switch (choice) {
                case 1 -> showCurrentPrices();
                case 2 -> handleUpdatePrices();
                case 0 -> { return; }
                default -> System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void showCurrentPrices() {
        printHeader("CURRENT GOLD PRICES (per gram in INR)");
        List<GoldPrice> prices = inventoryService.getAllPrices();

        System.out.printf("  %-10s | %15s | %15s | %10s | %10s\n",
                "Type", "Buy Price", "Sell Price", "Spread", "Spread %");
        System.out.println("  " + "-".repeat(70));
        for (GoldPrice price : prices) {
            System.out.printf("  %-10s | %15.2f | %15.2f | %10.2f | %9.2f%%\n",
                    price.getGoldType().getLabel(), price.getBuyPricePerGram(),
                    price.getSellPricePerGram(), price.getSpread(), price.getSpreadPercent());
        }
        System.out.println("  " + "-".repeat(70));
        pressEnterToContinue();
    }

    private void handleUpdatePrices() {
        if (!authService.isManager()) {
            System.out.println("  [!] Only managers and admins can update prices.");
            return;
        }

        GoldType[] types = GoldType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("    %d. %s\n", i + 1, types[i].getLabel());
        }
        int choice = input.readIntInRange("  Select Gold Type to update (1-4): ", 1, 4);
        GoldType type = types[choice - 1];

        Optional<GoldPrice> currentOpt = inventoryService.getPrice(type);
        if (currentOpt.isPresent()) {
            GoldPrice current = currentOpt.get();
            System.out.printf("  Current Buy: %.2f | Sell: %.2f\n",
                    current.getBuyPricePerGram(), current.getSellPricePerGram());
        }

        double buyPrice = input.readPositiveDouble("  New Buy Price per gram (INR): ");
        double sellPrice = input.readPositiveDouble("  New Sell Price per gram (INR): ");

        if (sellPrice <= buyPrice) {
            System.out.println("  [!] Warning: Sell price should be higher than buy price.");
            if (!input.readConfirmation("  Continue anyway?")) return;
        }

        if (inventoryService.updatePrice(type, buyPrice, sellPrice)) {
            System.out.println("  Price updated successfully!");
        } else {
            System.out.println("  [!] Failed to update price.");
        }
        pressEnterToContinue();
    }

    // ========== CUSTOMER MANAGEMENT ==========

    private void showCustomerMenu() {
        while (true) {
            printHeader("CUSTOMER MANAGEMENT");
            System.out.println("  1. View All Customers");
            System.out.println("  2. Add New Customer");
            System.out.println("  3. Search Customer");
            System.out.println("  4. Update Customer");
            System.out.println("  5. Delete Customer");
            System.out.println("  6. View Customer Transactions");
            System.out.println("  0. Back to Main Menu");

            int choice = input.readInt("\n  Enter your choice: ");
            switch (choice) {
                case 1 -> showAllCustomers();
                case 2 -> handleAddCustomer();
                case 3 -> handleSearchCustomer();
                case 4 -> handleUpdateCustomer();
                case 5 -> handleDeleteCustomer();
                case 6 -> handleCustomerTransactions();
                case 0 -> { return; }
                default -> System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void showAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        printHeader("ALL CUSTOMERS");
        System.out.printf("  %-6s | %-20s | %-15s | %-25s | %s\n",
                "ID", "Name", "Phone", "Email", "Address");
        System.out.println("  " + "-".repeat(90));
        for (Customer c : customers) {
            System.out.printf("  %-6d | %-20s | %-15s | %-25s | %s\n",
                    c.getId(), c.getName(),
                    c.getPhone() != null ? c.getPhone() : "N/A",
                    c.getEmail() != null ? c.getEmail() : "N/A",
                    c.getAddress() != null ? c.getAddress() : "N/A");
        }
        System.out.printf("\n  Total Customers: %d\n", customers.size());
        pressEnterToContinue();
    }

    private void handleAddCustomer() {
        printHeader("ADD NEW CUSTOMER");
        String name = input.readNonEmpty("  Full Name: ");
        String phone = input.readPhone("  Phone Number: ");
        String email = input.readEmail("  Email (optional): ");
        String address = input.readString("  Address: ");
        String idProof = input.readString("  ID Proof Type (Aadhaar/PAN/Passport): ");
        String idNumber = input.readString("  ID Number: ");

        int id = customerService.addCustomer(name, phone, email, address, idProof, idNumber);
        if (id > 0) {
            System.out.printf("  Customer added successfully! ID: %d\n", id);
        } else {
            System.out.println("  [!] Failed to add customer.");
        }
        pressEnterToContinue();
    }

    private void handleSearchCustomer() {
        String query = input.readNonEmpty("  Search by name: ");
        List<Customer> results = customerService.searchCustomers(query);
        if (results.isEmpty()) {
            System.out.println("  No customers found.");
        } else {
            for (Customer c : results) {
                System.out.printf("  ID: %d | %s | %s | %s\n",
                        c.getId(), c.getName(), c.getPhone(), c.getEmail());
            }
        }
        pressEnterToContinue();
    }

    private void handleUpdateCustomer() {
        int id = input.readInt("  Enter Customer ID to update: ");
        Optional<Customer> custOpt = customerService.getCustomer(id);
        if (custOpt.isEmpty()) {
            System.out.println("  [!] Customer not found.");
            return;
        }
        Customer customer = custOpt.get();
        System.out.printf("  Current: %s | %s | %s\n", customer.getName(), customer.getPhone(), customer.getEmail());

        String name = input.readString("  New name (Enter to skip): ");
        if (!name.isEmpty()) customer.setName(name);

        String phone = input.readString("  New phone (Enter to skip): ");
        if (!phone.isEmpty()) customer.setPhone(phone);

        String email = input.readString("  New email (Enter to skip): ");
        if (!email.isEmpty()) customer.setEmail(email);

        String address = input.readString("  New address (Enter to skip): ");
        if (!address.isEmpty()) customer.setAddress(address);

        if (customerService.updateCustomer(customer)) {
            System.out.println("  Customer updated successfully!");
        } else {
            System.out.println("  [!] Failed to update customer.");
        }
        pressEnterToContinue();
    }

    private void handleDeleteCustomer() {
        int id = input.readInt("  Enter Customer ID to delete: ");
        if (input.readConfirmation("  Are you sure?")) {
            if (customerService.deleteCustomer(id)) {
                System.out.println("  Customer deleted successfully!");
            } else {
                System.out.println("  [!] Failed to delete customer.");
            }
        }
        pressEnterToContinue();
    }

    private void handleCustomerTransactions() {
        int id = input.readInt("  Enter Customer ID: ");
        List<Transaction> txns = tradingService.getCustomerTransactions(id);
        if (txns.isEmpty()) {
            System.out.println("  No transactions found for this customer.");
        } else {
            printTransactionList(txns);
        }
        pressEnterToContinue();
    }

    // ========== SUPPLIER MANAGEMENT ==========

    private void showSupplierMenu() {
        while (true) {
            printHeader("SUPPLIER MANAGEMENT");
            System.out.println("  1. View All Suppliers");
            System.out.println("  2. Add New Supplier");
            System.out.println("  3. Search Supplier");
            System.out.println("  4. Update Supplier");
            System.out.println("  5. Delete Supplier");
            System.out.println("  0. Back to Main Menu");

            int choice = input.readInt("\n  Enter your choice: ");
            switch (choice) {
                case 1 -> showAllSuppliers();
                case 2 -> handleAddSupplier();
                case 3 -> handleSearchSupplier();
                case 4 -> handleUpdateSupplier();
                case 5 -> handleDeleteSupplier();
                case 0 -> { return; }
                default -> System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void showAllSuppliers() {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        printHeader("ALL SUPPLIERS");
        System.out.printf("  %-6s | %-18s | %-18s | %-15s | %-20s | %s\n",
                "ID", "Name", "Company", "Phone", "License", "Rating");
        System.out.println("  " + "-".repeat(100));
        for (Supplier s : suppliers) {
            System.out.printf("  %-6d | %-18s | %-18s | %-15s | %-20s | %.1f/5.0\n",
                    s.getId(), s.getName(),
                    s.getCompanyName() != null ? s.getCompanyName() : "N/A",
                    s.getPhone() != null ? s.getPhone() : "N/A",
                    s.getLicenseNumber() != null ? s.getLicenseNumber() : "N/A",
                    s.getRating());
        }
        System.out.printf("\n  Total Suppliers: %d\n", suppliers.size());
        pressEnterToContinue();
    }

    private void handleAddSupplier() {
        printHeader("ADD NEW SUPPLIER");
        String name = input.readNonEmpty("  Contact Name: ");
        String phone = input.readPhone("  Phone Number: ");
        String email = input.readEmail("  Email (optional): ");
        String address = input.readString("  Address: ");
        String companyName = input.readString("  Company Name: ");
        String license = input.readString("  License Number: ");

        int id = supplierService.addSupplier(name, phone, email, address, companyName, license);
        if (id > 0) {
            System.out.printf("  Supplier added successfully! ID: %d\n", id);
        } else {
            System.out.println("  [!] Failed to add supplier.");
        }
        pressEnterToContinue();
    }

    private void handleSearchSupplier() {
        String query = input.readNonEmpty("  Search by name/company: ");
        List<Supplier> results = supplierService.searchSuppliers(query);
        if (results.isEmpty()) {
            System.out.println("  No suppliers found.");
        } else {
            for (Supplier s : results) {
                System.out.printf("  ID: %d | %s | %s | Rating: %.1f\n",
                        s.getId(), s.getName(), s.getCompanyName(), s.getRating());
            }
        }
        pressEnterToContinue();
    }

    private void handleUpdateSupplier() {
        int id = input.readInt("  Enter Supplier ID to update: ");
        Optional<Supplier> suppOpt = supplierService.getSupplier(id);
        if (suppOpt.isEmpty()) {
            System.out.println("  [!] Supplier not found.");
            return;
        }
        Supplier supplier = suppOpt.get();
        System.out.printf("  Current: %s | %s | %s\n", supplier.getName(), supplier.getCompanyName(), supplier.getPhone());

        String name = input.readString("  New name (Enter to skip): ");
        if (!name.isEmpty()) supplier.setName(name);

        String phone = input.readString("  New phone (Enter to skip): ");
        if (!phone.isEmpty()) supplier.setPhone(phone);

        String company = input.readString("  New company (Enter to skip): ");
        if (!company.isEmpty()) supplier.setCompanyName(company);

        String ratingStr = input.readString("  New rating 0-5 (Enter to skip): ");
        if (!ratingStr.isEmpty()) supplier.setRating(Double.parseDouble(ratingStr));

        if (supplierService.updateSupplier(supplier)) {
            System.out.println("  Supplier updated successfully!");
        } else {
            System.out.println("  [!] Failed to update supplier.");
        }
        pressEnterToContinue();
    }

    private void handleDeleteSupplier() {
        int id = input.readInt("  Enter Supplier ID to delete: ");
        if (input.readConfirmation("  Are you sure?")) {
            if (supplierService.deleteSupplier(id)) {
                System.out.println("  Supplier deleted successfully!");
            } else {
                System.out.println("  [!] Failed to delete supplier.");
            }
        }
        pressEnterToContinue();
    }

    // ========== TRANSACTION HISTORY ==========

    private void showTransactionHistory() {
        while (true) {
            printHeader("TRANSACTION HISTORY");
            System.out.println("  1. View All Transactions");
            System.out.println("  2. View Buy Transactions Only");
            System.out.println("  3. View Sell Transactions Only");
            System.out.println("  4. Search by Invoice Number");
            System.out.println("  0. Back to Main Menu");

            int choice = input.readInt("\n  Enter your choice: ");
            switch (choice) {
                case 1 -> { printTransactionList(tradingService.getAllTransactions()); pressEnterToContinue(); }
                case 2 -> { printTransactionList(tradingService.getBuyTransactions()); pressEnterToContinue(); }
                case 3 -> { printTransactionList(tradingService.getSellTransactions()); pressEnterToContinue(); }
                case 4 -> {
                    String invoice = input.readNonEmpty("  Enter Invoice Number: ");
                    Optional<Transaction> txn = tradingService.findByInvoice(invoice);
                    if (txn.isPresent()) {
                        printTransactionDetail(txn.get());
                    } else {
                        System.out.println("  [!] Transaction not found.");
                    }
                    pressEnterToContinue();
                }
                case 0 -> { return; }
                default -> System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void printTransactionList(List<Transaction> transactions) {
        System.out.println("\n  TRANSACTIONS");
        System.out.println("  " + "-".repeat(100));
        System.out.printf("  %-6s | %-6s | %-8s | %10s | %12s | %12s | %-22s | %s\n",
                "ID", "Type", "Gold", "Weight(g)", "Price/g", "Total", "Invoice", "Date");
        System.out.println("  " + "-".repeat(100));

        if (transactions.isEmpty()) {
            System.out.println("  No transactions found.");
        } else {
            for (Transaction txn : transactions) {
                System.out.printf("  %-6d | %-6s | %-8s | %10.2f | %12.2f | %12.2f | %-22s | %s\n",
                        txn.getId(), txn.getType().getCode(), txn.getGoldType().getLabel(),
                        txn.getWeightGrams(), txn.getPricePerGram(), txn.getFinalAmount(),
                        txn.getInvoiceNumber() != null ? txn.getInvoiceNumber() : "N/A",
                        txn.getTransactionDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            }
        }
        System.out.println("  " + "-".repeat(100));
        System.out.printf("  Total: %d transactions\n", transactions.size());
    }

    private void printTransactionDetail(Transaction txn) {
        System.out.println("\n  === TRANSACTION DETAIL ===");
        System.out.printf("  Transaction ID:   %d\n", txn.getId());
        System.out.printf("  Type:             %s\n", txn.getType().getDescription());
        System.out.printf("  Invoice:          %s\n", txn.getInvoiceNumber());
        System.out.printf("  Gold Type:        %s\n", txn.getGoldType().getLabel());
        System.out.printf("  Weight:           %.2f grams\n", txn.getWeightGrams());
        System.out.printf("  Price/gram:       %.2f INR\n", txn.getPricePerGram());
        System.out.printf("  Subtotal:         %.2f INR\n", txn.getTotalAmount());
        System.out.printf("  Tax:              %.2f INR\n", txn.getTaxAmount());
        System.out.printf("  Final Amount:     %.2f INR\n", txn.getFinalAmount());
        System.out.printf("  Payment Method:   %s\n", txn.getPaymentMethod());
        System.out.printf("  Date:             %s\n",
                txn.getTransactionDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        if (txn.getNotes() != null && !txn.getNotes().isEmpty()) {
            System.out.printf("  Notes:            %s\n", txn.getNotes());
        }
    }

    // ========== REPORTS ==========

    private void showReportsMenu() {
        while (true) {
            printHeader("REPORTS & ANALYTICS");
            System.out.println("  1. Profit & Loss Report");
            System.out.println("  2. Inventory Report (by Gold Type)");
            System.out.println("  3. Transaction Report");
            System.out.println("  4. Full Dashboard Summary");
            System.out.println("  0. Back to Main Menu");

            int choice = input.readInt("\n  Enter your choice: ");
            switch (choice) {
                case 1 -> { System.out.println(reportService.generateProfitLossReport()); pressEnterToContinue(); }
                case 2 -> { System.out.println(reportService.generateInventoryReport()); pressEnterToContinue(); }
                case 3 -> { System.out.println(reportService.generateTransactionReport()); pressEnterToContinue(); }
                case 4 -> showDashboard();
                case 0 -> { return; }
                default -> System.out.println("  [!] Invalid choice.");
            }
        }
    }

    // ========== USER MANAGEMENT ==========

    private void handleUserManagement() {
        while (true) {
            printHeader("USER MANAGEMENT (Admin Only)");
            System.out.println("  1. Register New User");
            System.out.println("  2. Change Password");
            System.out.println("  0. Back to Main Menu");

            int choice = input.readInt("\n  Enter your choice: ");
            switch (choice) {
                case 1 -> {
                    String username = input.readNonEmpty("  Username: ");
                    String password = input.readNonEmpty("  Password: ");
                    String fullName = input.readNonEmpty("  Full Name: ");
                    System.out.println("  Roles: ADMIN, MANAGER, STAFF");
                    String role = input.readNonEmpty("  Role: ").toUpperCase();
                    if (authService.registerUser(username, password, fullName, role)) {
                        System.out.println("  User registered successfully!");
                    } else {
                        System.out.println("  [!] Failed to register user.");
                    }
                    pressEnterToContinue();
                }
                case 2 -> {
                    String oldPass = input.readNonEmpty("  Current Password: ");
                    String newPass = input.readNonEmpty("  New Password: ");
                    if (authService.changePassword(oldPass, newPass)) {
                        System.out.println("  Password changed successfully!");
                    } else {
                        System.out.println("  [!] Incorrect current password.");
                    }
                    pressEnterToContinue();
                }
                case 0 -> { return; }
                default -> System.out.println("  [!] Invalid choice.");
            }
        }
    }

    // ========== HELPERS ==========

    private void printHeader(String title) {
        System.out.println();
        System.out.println("  " + "=".repeat(60));
        System.out.printf("    %s\n", title);
        System.out.println("  " + "=".repeat(60));
        System.out.printf("  User: %s | Role: %s | %s\n",
                authService.getCurrentUser() != null ? authService.getCurrentUser().getFullName() : "N/A",
                authService.getCurrentUser() != null ? authService.getCurrentUser().getRole() : "N/A",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        System.out.println("  " + "-".repeat(60));
    }

    private void pressEnterToContinue() {
        input.readString("\n  Press Enter to continue...");
    }
}
