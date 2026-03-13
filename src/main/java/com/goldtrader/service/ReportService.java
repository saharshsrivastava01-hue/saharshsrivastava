package com.goldtrader.service;

import com.goldtrader.dao.InventoryDao;
import com.goldtrader.dao.TransactionDao;
import com.goldtrader.model.Inventory;
import com.goldtrader.model.Transaction;
import com.goldtrader.model.TransactionType;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final TransactionDao transactionDao = new TransactionDao();
    private final InventoryDao inventoryDao = new InventoryDao();

    public BigDecimal getTotalSales(LocalDate start, LocalDate end) {
        return transactionDao.getTotalSales(start, end);
    }

    public BigDecimal getTotalPurchases(LocalDate start, LocalDate end) {
        return transactionDao.getTotalPurchases(start, end);
    }

    public BigDecimal getProfit(LocalDate start, LocalDate end) {
        BigDecimal sales = getTotalSales(start, end);
        BigDecimal purchases = getTotalPurchases(start, end);
        return sales.subtract(purchases);
    }

    public BigDecimal getInventoryValuation() {
        return inventoryDao.getTotalInventoryValue();
    }

    public List<Inventory> getInventorySummary() {
        return inventoryDao.findAll();
    }

    public void exportTransactionsToCSV(String filePath, TransactionType type, LocalDate start, LocalDate end) throws IOException {
        List<Transaction> transactions = transactionDao.findByFilter(type, start, end);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Header
            writer.writeNext(new String[]{
                "ID", "Type", "Gold Type", "Weight (g)", "Price/g", "Total Amount",
                "Tax", "Net Amount", "Customer", "Notes", "Date", "Created By"
            });

            // Data rows
            for (Transaction tx : transactions) {
                writer.writeNext(new String[]{
                    String.valueOf(tx.getId()),
                    tx.getType().getLabel(),
                    tx.getGoldType().getLabel(),
                    tx.getWeightGrams().toPlainString(),
                    tx.getPricePerGram().toPlainString(),
                    tx.getTotalAmount().toPlainString(),
                    tx.getTaxAmount().toPlainString(),
                    tx.getNetAmount().toPlainString(),
                    tx.getCustomerName(),
                    tx.getNotes() != null ? tx.getNotes() : "",
                    tx.getTransactionDate().format(fmt),
                    tx.getCreatedBy()
                });
            }
            logger.info("Exported {} transactions to {}", transactions.size(), filePath);
        }
    }
}
