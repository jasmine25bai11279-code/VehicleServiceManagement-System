# Project Statement: Vehicle Service Management System

## 1. Problem Statement
Automotive service workshops and independent repair centers often struggle with manual, disjointed processes for handling client inquiries, tracking vehicle service jobs, inventory/parts consumption, and transparent invoicing. Traditional pen-and-paper or disjointed spreadsheet records lead to frequent human errors, lost service records, miscalculated billing (especially labor and GST/tax computations), delayed status updates for vehicle owners, and poor revenue visibility for garage management. 

There is an acute need for a robust, reliable, and self-contained desktop management system tailored specifically for vehicle service centers that enforces data integrity, automates billing calculations, prevents scheduling and record anomalies, and provides instant financial and operational intelligence.

---

## 2. Project Scope
The Vehicle Service Management System is designed as a secure, standalone, layered Java desktop application for automobile workshop owners, service advisors, mechanics, and billing clerks.

### Within Scope:
- **Customer Relationship Management**: Centralized customer records with input validation and quick search.
- **Vehicle Fleet Tracking**: Multi-vehicle customer ownership, unique vehicle identification (registration number uniqueness), category classification, and manufacturing specifications.
- **Service Request & Job Lifecycle**: End-to-end service job tracking across clear stages (`Pending` → `In Progress` → `Completed` / `Cancelled`).
- **Parts & Labor Accounting**: Itemized spare parts recording with automatic cost rollup and editable labor charges.
- **Automated Billing Engine**: System-calculated invoice generation based on `Parts Cost + Labor Cost + 18% Tax (GST)` with payment tracking (`Unpaid` / `Paid`).
- **Executive Analytics & Reporting**: Real-time KPI cards and JavaFX charts for job status distributions and collected versus pending revenue.
- **Security & Integrity**: Parameterized JDBC SQL queries preventing SQL injection, relational foreign keys with cascading cleanup, and password hashing for staff accounts.

### Out of Scope (Version 1 Scope Control):
- Public customer web portal or mobile native app.
- Third-party online payment gateways (Stripe/Razorpay/UPI API integrations).
- Vehicle GPS tracking or IoT OBD-II scanner hardware integration.
- AI/ML predictive component failure models.
- Automated SMS/WhatsApp telecommunication gateways.

*(These are prioritized under Future Enhancements in Section 14).*

---

## 3. Target Users
1. **Service Center Managers / Owners**: Oversee daily garage performance, inspect gross revenue, audit unpaid invoices, and track workflow velocity.
2. **Service Advisors / Reception Staff**: Register incoming customers, record newly arriving vehicles, log reported vehicle symptoms, and open new service requests.
3. **Technicians / Workshop Supervisors**: Update service job status (`Pending` → `In Progress` → `Completed`), log replaced spare parts and consumables, and record technician labor hours.
4. **Billing & Accounts Clerks**: Generate official tax invoices upon service completion, print customer receipts, and mark payments as received.

---

## 4. High-Level Features
- **Role-Based Staff Authentication**: Secure login portal with password hashing and session tracking.
- **Interactive Executive Dashboard**: Visual summary cards, JavaFX Pie Charts for service status breakdown, and Bar Charts for revenue flow.
- **Full Customer & Vehicle CRUD**: Create, read, update, delete, and real-time live search for both customer accounts and vehicle registrations.
- **Multi-Vehicle Ownership**: Dynamic association enabling single customers to own and service multiple vehicles.
- **Job Status Workflow State Machine**: Safe lifecycle transitions guarding against invalid workflow skips.
- **Itemized Spare Parts & Material Register**: Dynamic line-item billing for parts, oil, consumables, and labor.
- **Automated Tax Calculation**: Automatic tax calculation guaranteeing zero calculation discrepancy.
- **Integrated SQLite Relational Database**: Zero-configuration, zero-dependency embedded database supporting full ACID guarantees and foreign key referential integrity.
