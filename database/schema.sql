-- Vehicle Service Management System Database Schema (SQLite)
-- Enables Foreign Key Support
PRAGMA foreign_keys = ON;

-- 1. Users Table (Staff Authentication)
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'STAFF',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    email TEXT,
    address TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 3. Vehicles Table
CREATE TABLE IF NOT EXISTS vehicles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    registration_number TEXT UNIQUE NOT NULL,
    brand TEXT NOT NULL,
    model TEXT NOT NULL,
    vehicle_type TEXT NOT NULL,
    manufacturing_year INTEGER NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- 4. Service Records Table
CREATE TABLE IF NOT EXISTS service_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_id INTEGER NOT NULL,
    service_type TEXT NOT NULL,
    description TEXT,
    service_date TEXT NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('Pending', 'In Progress', 'Completed', 'Cancelled')) DEFAULT 'Pending',
    labour_cost REAL NOT NULL DEFAULT 0.0,
    parts_cost REAL NOT NULL DEFAULT 0.0,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

-- 5. Service Items Table (Parts/Components Used)
CREATE TABLE IF NOT EXISTS service_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    service_id INTEGER NOT NULL,
    item_name TEXT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price REAL NOT NULL DEFAULT 0.0,
    total_price REAL NOT NULL DEFAULT 0.0,
    FOREIGN KEY (service_id) REFERENCES service_records(id) ON DELETE CASCADE
);

-- 6. Bills Table
CREATE TABLE IF NOT EXISTS bills (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    service_id INTEGER UNIQUE NOT NULL,
    subtotal REAL NOT NULL DEFAULT 0.0,
    tax REAL NOT NULL DEFAULT 0.0,
    total_amount REAL NOT NULL DEFAULT 0.0,
    payment_status TEXT NOT NULL CHECK(payment_status IN ('Unpaid', 'Paid')) DEFAULT 'Unpaid',
    bill_date TEXT NOT NULL,
    FOREIGN KEY (service_id) REFERENCES service_records(id) ON DELETE CASCADE
);

-- Indices for performance
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone);
CREATE INDEX IF NOT EXISTS idx_vehicles_reg ON vehicles(registration_number);
CREATE INDEX IF NOT EXISTS idx_vehicles_customer ON vehicles(customer_id);
CREATE INDEX IF NOT EXISTS idx_service_vehicle ON service_records(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_service_status ON service_records(status);
CREATE INDEX IF NOT EXISTS idx_items_service ON service_items(service_id);
CREATE INDEX IF NOT EXISTS idx_bills_service ON bills(service_id);
