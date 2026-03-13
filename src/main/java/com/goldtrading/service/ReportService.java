package com.goldtrading.service;

import com.goldtrading.dao.TransactionDao;
import com.goldtrading.model.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for generating reports and analytics.
 */
public class ReportService {

    private final TransactionDao transactionDao;
    private final GoldInventoryService inventoryService;
    private final CustomerService customerService;
    private final SupplierService supplierService;

    public ReportService(GoldInventoryService inventoryService, CustomerService customerService,
                         SupplierService supplierService) {
        this.transactionDao = new TransactionDao();
        this.inventoryService = inventoryService;
        this.customerService = customerService;
        this.supplierService = supplierService;
    }

    /**
     * Generate a comprehensive dashboard summary.
     */
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        try {
            summary.put("totalInventoryItems", inventoryService.getAvailableCount());
            summary.put("totalInventoryWeight", inventoryService.getTotalInventoryWeight());
            summary.put("totalInventoryValue", inventoryService.getTotalInventoryValue());
            summary.put("totalCustomers", customerService.getCustomerCount());
            summary.put("totalSuppliers", supplierService.getSupplierCount());
            summary.put("totalTransactions", transactionDao.getTransactionCount());
            summary.put("totalBuyAmount", transactionDao.getTotalBuyAmount());
            summary.put("totalSellAmount", transactionDao.getTotalSellAmount());
            summary.put("totalBuyWeight", transactionDao.getTotalBuyWeight());
            summary.put("totalSellWeight", transactionDao.getTotalSellWeight());

            double totalBuy = transactionDao.getTotalBuyAmount();
            double totalSell = transactionDao.getTotalSellAmount();
            summary.put("grossProfit", totalSell - totalBuy);
        } catch (SQLException e) {
            System.err.println("[REPORT] Error generating dashboard: " + e.getMessage());
        }
        return summary;
    }

    /**
     * Generate profit/loss report.
     */
    public String generateProfitLossReport() {
        StringBuilder report = new StringBuilder();
        try {
            double totalBuy = transactionDao.getTotalBuyAmount();
            double totalSell = transactionDao.getTotalSellAmount();
            double buyWeight = transactionDao.getTotalBuyWeight();
            double sellWeight = transactionDao.getTotalSellWeight();
            double grossProfit = totalSell - totalBuy;
            double profitMargin = totalSell > 0 ? (grossProfit / totalSell) * 100 : 0;

            report.append("\n");
            report.append("=".repeat(60)).append("\n");
            report.append("           PROFIT & LOSS REPORT\n");
            report.append("=".repeat(60)).append("\n");
            report.append(String.format("  Report Date: %s\n",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
            report.append("-".repeat(60)).append("\n");
            report.append(String.format("  Total Gold Purchased:    %10.2f grams\n", buyWeight));
            report.append(String.format("  Total Purchase Amount:   %10.2f INR\n", totalBuy));
            report.append("-".repeat(60)).append("\n");
            report.append(String.format("  Total Gold Sold:         %10.2f grams\n", sellWeight));
            report.append(String.format("  Total Sales Amount:      %10.2f INR\n", totalSell));
            report.append("-".repeat(60)).append("\n");
            report.append(String.format("  Gross Profit/Loss:       %10.2f INR\n", grossProfit));
            report.append(String.format("  Profit Margin:           %10.2f %%\n", profitMargin));
            report.append("=".repeat(60)).append("\n");

            // Inventory value
            double inventoryValue = inventoryService.getTotalInventoryValue();
            double inventoryWeight = inventoryService.getTotalInventoryWeight();
            report.append(String.format("  Current Inventory:       %10.2f grams\n", inventoryWeight));
            report.append(String.format("  Inventory Value:         %10.2f INR\n", inventoryValue));
            report.append("=".repeat(60)).append("\n");

        } catch (SQLException e) {
            report.append("Error generating report: ").append(e.getMessage());
        }
        return report.toString();
    }

    /**
     * Generate inventory report by gold type.
     */
    public String generateInventoryReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n");
        report.append("=".repeat(80)).append("\n");
        report.append("                    INVENTORY REPORT BY GOLD TYPE\n");
        report.append("=".repeat(80)).append("\n");
        report.append(String.format("%-8s | %12s | %12s | %15s | %12s\n",
                "Type", "Items", "Weight (g)", "Purchase Value", "Selling Value"));
        report.append("-".repeat(80)).append("\n");

        for (GoldType type : GoldType.values()) {
            List<GoldItem> items = inventoryService.getGoldItemsByType(type);
            List<GoldItem> available = items.stream()
                    .filter(i -> "AVAILABLE".equals(i.getStatus()))
                    .toList();

            double totalWeight = available.stream().mapToDouble(GoldItem::getWeightGrams).sum();
            double totalPurchase = available.stream().mapToDouble(GoldItem::getTotalPurchaseValue).sum();
            double totalSelling = available.stream().mapToDouble(GoldItem::getTotalSellingValue).sum();

            report.append(String.format("%-8s | %12d | %12.2f | %15.2f | %12.2f\n",
                    type.getLabel(), available.size(), totalWeight, totalPurchase, totalSelling));
        }

        report.append("-".repeat(80)).append("\n");
        report.append(String.format("%-8s | %12d | %12.2f | %15s | %12.2f\n",
                "TOTAL", inventoryService.getAvailableCount(),
                inventoryService.getTotalInventoryWeight(),
                "", inventoryService.getTotalInventoryValue()));
        report.append("=".repeat(80)).append("\n");

        return report.toString();
    }

    /**
     * Generate transaction summary report.
     */
    public String generateTransactionReport() {
        StringBuilder report = new StringBuilder();
        try {
            List<Transaction> allTransactions = transactionDao.findAll();

            report.append("\n");
            report.append("=".repeat(100)).append("\n");
            report.append("                          TRANSACTION HISTORY REPORT\n");
            report.append("=".repeat(100)).append("\n");
            report.append(String.format("%-6s | %-6s | %-8s | %10s | %12s | %12s | %-20s | %-12s\n",
                    "ID", "Type", "Gold", "Weight(g)", "Price/g", "Total", "Invoice", "Date"));
            report.append("-".repeat(100)).append("\n");

            for (Transaction txn : allTransactions) {
                report.append(String.format("%-6d | %-6s | %-8s | %10.2f | %12.2f | %12.2f | %-20s | %-12s\n",
                        txn.getId(),
                        txn.getType().getCode(),
                        txn.getGoldType().getLabel(),
                        txn.getWeightGrams(),
                        txn.getPricePerGram(),
                        txn.getFinalAmount(),
                        txn.getInvoiceNumber() != null ? txn.getInvoiceNumber() : "N/A",
                        txn.getTransactionDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
            }

            report.append("-".repeat(100)).append("\n");
            report.append(String.format("Total Transactions: %d\n", allTransactions.size()));
            report.append(String.format("Total Buy Amount:  %.2f INR\n", transactionDao.getTotalBuyAmount()));
            report.append(String.format("Total Sell Amount: %.2f INR\n", transactionDao.getTotalSellAmount()));
            report.append("=".repeat(100)).append("\n");

        } catch (SQLException e) {
            report.append("Error generating report: ").append(e.getMessage());
        }
        return report.toString();
    }
}
