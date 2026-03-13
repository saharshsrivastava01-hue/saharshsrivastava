# Gold Trading Management System

A professional-grade Java console application for managing gold buying and selling operations, inventory tracking, customer/supplier management, and comprehensive financial reporting.

## Features

- **Gold Buy/Sell Trading** - Complete buy/sell workflow with invoice generation, GST calculation, and multiple payment methods
- **Inventory Management** - Track gold items by karat type (24K, 22K, 18K, 14K) with weight, pricing, and availability status
- **Gold Price Management** - Maintain current market buy/sell prices per gram for all gold types with spread tracking
- **Customer Management** - Full CRUD with ID proof tracking, search, and transaction history per customer
- **Supplier Management** - Manage suppliers with company details, license numbers, and ratings
- **Transaction History** - Complete audit trail of all buy/sell transactions with invoice numbers
- **Reports & Analytics** - Profit/Loss reports, Inventory reports by gold type, Transaction summaries, Dashboard
- **User Authentication** - Role-based access control (Admin, Manager, Staff) with SHA-256 password hashing
- **SQLite Database** - Lightweight persistent storage with automatic schema initialization

## Tech Stack

- Java 17
- Maven (build tool)
- SQLite (via JDBC)
- JUnit 5 (testing)

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Build

```bash
mvn clean package
```

### Run

```bash
java -jar target/gold-trading-system-1.0.0.jar
```

### Default Login

- **Username:** `admin`
- **Password:** `admin123`

## Project Structure

```
src/main/java/com/goldtrading/
├── GoldTradingApp.java          # Main entry point
├── model/                        # Domain models
│   ├── GoldType.java            # Gold karat enum (24K, 22K, 18K, 14K)
│   ├── GoldItem.java            # Gold inventory item
│   ├── GoldPrice.java           # Market price per gold type
│   ├── Customer.java            # Customer entity
│   ├── Supplier.java            # Supplier entity
│   ├── Transaction.java         # Buy/Sell transaction
│   ├── TransactionType.java     # BUY/SELL enum
│   └── User.java                # System user
├── dao/                          # Data Access Objects
│   ├── DatabaseManager.java     # SQLite connection & schema init
│   ├── GoldItemDao.java
│   ├── GoldPriceDao.java
│   ├── CustomerDao.java
│   ├── SupplierDao.java
│   ├── TransactionDao.java
│   └── UserDao.java
├── service/                      # Business logic
│   ├── AuthService.java         # Authentication & authorization
│   ├── GoldInventoryService.java
│   ├── TradingService.java      # Buy/sell operations
│   ├── CustomerService.java
│   ├── SupplierService.java
│   └── ReportService.java       # Analytics & reporting
└── ui/                           # Console user interface
    ├── MenuHandler.java         # Main menu controller
    ├── InputValidator.java      # Input validation utilities
    └── TableFormatter.java      # Console table formatting
```

## License

MIT
