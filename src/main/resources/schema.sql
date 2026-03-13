-- Gold Trading Management System - Database Schema

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'STAFF',
    active INTEGER NOT NULL DEFAULT 1,
    last_login TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    address TEXT,
    id_proof TEXT,
    id_number TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Suppliers table
CREATE TABLE IF NOT EXISTS suppliers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    address TEXT,
    company_name TEXT,
    license_number TEXT,
    rating REAL NOT NULL DEFAULT 5.0,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Gold inventory table
CREATE TABLE IF NOT EXISTS gold_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    gold_type TEXT NOT NULL,
    weight_grams REAL NOT NULL,
    purchase_price_per_gram REAL NOT NULL,
    selling_price_per_gram REAL NOT NULL,
    description TEXT,
    status TEXT NOT NULL DEFAULT 'AVAILABLE',
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,
    gold_item_id INTEGER,
    customer_id INTEGER,
    supplier_id INTEGER,
    gold_type TEXT NOT NULL,
    weight_grams REAL NOT NULL,
    price_per_gram REAL NOT NULL,
    total_amount REAL NOT NULL,
    tax_amount REAL NOT NULL DEFAULT 0,
    final_amount REAL NOT NULL,
    payment_method TEXT,
    invoice_number TEXT UNIQUE,
    notes TEXT,
    transaction_date TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (gold_item_id) REFERENCES gold_items(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

-- Gold prices table (current market prices)
CREATE TABLE IF NOT EXISTS gold_prices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    gold_type TEXT NOT NULL UNIQUE,
    buy_price_per_gram REAL NOT NULL,
    sell_price_per_gram REAL NOT NULL,
    effective_date TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_gold_items_status ON gold_items(status);
CREATE INDEX IF NOT EXISTS idx_gold_items_type ON gold_items(gold_type);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_customer ON transactions(customer_id);
CREATE INDEX IF NOT EXISTS idx_transactions_supplier ON transactions(supplier_id);
CREATE INDEX IF NOT EXISTS idx_customers_name ON customers(name);
CREATE INDEX IF NOT EXISTS idx_suppliers_name ON suppliers(name);

-- Insert default admin user (password: admin123)
INSERT OR IGNORE INTO users (username, password_hash, full_name, role)
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'System Administrator', 'ADMIN');

-- Insert default gold prices (INR per gram)
INSERT OR REPLACE INTO gold_prices (gold_type, buy_price_per_gram, sell_price_per_gram)
VALUES ('GOLD_24K', 6200.00, 6350.00);
INSERT OR REPLACE INTO gold_prices (gold_type, buy_price_per_gram, sell_price_per_gram)
VALUES ('GOLD_22K', 5680.00, 5820.00);
INSERT OR REPLACE INTO gold_prices (gold_type, buy_price_per_gram, sell_price_per_gram)
VALUES ('GOLD_18K', 4650.00, 4770.00);
INSERT OR REPLACE INTO gold_prices (gold_type, buy_price_per_gram, sell_price_per_gram)
VALUES ('GOLD_14K', 3630.00, 3720.00);
