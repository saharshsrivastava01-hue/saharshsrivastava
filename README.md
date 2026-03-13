# Gold Trading Management System

A professional-grade gold buying and selling management software built with Java Swing.

## Features

- **Authentication**: Secure login system with admin/user roles and BCrypt password hashing
- **Dashboard**: Real-time overview of inventory, transactions, and gold prices
- **Buy Gold**: Record gold purchases from suppliers/customers with auto-calculations
- **Sell Gold**: Process gold sales with tax calculation and inventory validation
- **Inventory Management**: Track gold stock by type (24K/22K/18K/14K) with low-stock alerts
- **Customer Management**: Full CRUD operations with transaction history per customer
- **Transaction History**: View/filter all buy/sell transactions by date and type
- **Reports & Analytics**: Sales/purchases/profit reports with CSV export capability
- **Gold Price Management**: Set and track gold prices per purity level with history

## Tech Stack

- **Language**: Java 17
- **GUI Framework**: Java Swing with FlatLaf dark theme
- **Database**: H2 embedded database (zero configuration)
- **Build Tool**: Apache Maven
- **Password Security**: BCrypt hashing
- **CSV Export**: OpenCSV
- **Logging**: SLF4J + Logback

## Quick Start

### Prerequisites
- Java 17 or higher
- Apache Maven 3.6+

### Build & Run

```bash
# Clone the repository
git clone <repository-url>
cd gold-selling-software

# Build the project
mvn clean package

# Run the application
java -jar target/gold-selling-software-1.0.0.jar
```

### Default Login Credentials
- **Username**: `admin`
- **Password**: Set via `GOLD_TRADER_ADMIN_PASSWORD` environment variable (defaults to `changeme` if not set)

To customize the admin password before first launch:
```bash
export GOLD_TRADER_ADMIN_PASSWORD=your_secure_password
java -jar target/gold-selling-software-1.0.0.jar
```

## Architecture

```
com.goldtrader
├── model/          # Domain models (Customer, Transaction, Inventory, etc.)
├── dao/            # Data Access Objects (database operations)
├── service/        # Business logic layer
├── ui/             # Swing GUI components
│   └── panels/     # Individual panel components
└── util/           # Utilities (database, security, formatting)
```

## Gold Types Supported
| Type | Purity | Description |
|------|--------|-------------|
| 24K  | 99.9%  | Pure Gold |
| 22K  | 91.6%  | Jewelry Grade |
| 18K  | 75.0%  | Standard Jewelry |
| 14K  | 58.3%  | Economy Jewelry |

## License

MIT License
