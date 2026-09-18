# Entity-Relationship (ER) Diagram: Vehicle Service Management System

## Relational Database Schema
This diagram presents the relational SQLite database entities, primary keys (PK), foreign keys (FK), column attributes, and cardinality constraints.

```mermaid
erDiagram
    USERS {
        INTEGER id PK "AUTOINCREMENT"
        TEXT username "UNIQUE, NOT NULL"
        TEXT password_hash "NOT NULL"
        TEXT full_name "NOT NULL"
        TEXT role "DEFAULT 'STAFF'"
        DATETIME created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    CUSTOMERS {
        INTEGER id PK "AUTOINCREMENT"
        TEXT name "NOT NULL"
        TEXT phone "NOT NULL"
        TEXT email "OPTIONAL"
        TEXT address "OPTIONAL"
        DATETIME created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    VEHICLES {
        INTEGER id PK "AUTOINCREMENT"
        INTEGER customer_id FK "REFERENCES customers(id)"
        TEXT registration_number "UNIQUE, NOT NULL"
        TEXT brand "NOT NULL"
        TEXT model "NOT NULL"
        TEXT vehicle_type "NOT NULL"
        INTEGER manufacturing_year "NOT NULL"
    }

    SERVICE_RECORDS {
        INTEGER id PK "AUTOINCREMENT"
        INTEGER vehicle_id FK "REFERENCES vehicles(id)"
        TEXT service_type "NOT NULL"
        TEXT description "OPTIONAL"
        TEXT service_date "NOT NULL"
        TEXT status "CHECK: Pending, In Progress, Completed, Cancelled"
        REAL labour_cost "DEFAULT 0.0"
        REAL parts_cost "DEFAULT 0.0"
    }

    SERVICE_ITEMS {
        INTEGER id PK "AUTOINCREMENT"
        INTEGER service_id FK "REFERENCES service_records(id)"
        TEXT item_name "NOT NULL"
        INTEGER quantity "DEFAULT 1"
        REAL unit_price "DEFAULT 0.0"
        REAL total_price "DEFAULT 0.0"
    }

    BILLS {
        INTEGER id PK "AUTOINCREMENT"
        INTEGER service_id FK "UNIQUE, REFERENCES service_records(id)"
        REAL subtotal "DEFAULT 0.0"
        REAL tax "DEFAULT 0.0"
        REAL total_amount "DEFAULT 0.0"
        TEXT payment_status "CHECK: Unpaid, Paid"
        TEXT bill_date "NOT NULL"
    }

    CUSTOMERS ||--o{ VEHICLES : "1-to-Many (owns)"
    VEHICLES ||--o{ SERVICE_RECORDS : "1-to-Many (has)"
    SERVICE_RECORDS ||--o{ SERVICE_ITEMS : "1-to-Many (consumes)"
    SERVICE_RECORDS ||--o| BILLS : "1-to-1 (invoiced by)"
```

## Relational Integrity Rules
- **Cascade Deletes**:
  - Deleting a customer cascades to their vehicles, service records, and associated bills.
  - Deleting a service record cascades to its service items and bill record.
- **Uniqueness Constraints**:
  - `vehicles.registration_number` is strictly unique. Duplicate registration attempts are rejected.
  - `bills.service_id` is unique, enforcing the strict 1-to-1 relationship between a service job and its invoice.
  - `users.username` is strictly unique across all staff logins.
- **Check Constraints**:
  - `service_records.status` is restricted to `('Pending', 'In Progress', 'Completed', 'Cancelled')`.
  - `bills.payment_status` is restricted to `('Unpaid', 'Paid')`.
