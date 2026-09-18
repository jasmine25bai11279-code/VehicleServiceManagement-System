# Vehicle Service Management System

A production-grade, robust, fully Java-based desktop application for automotive service centers, workshops, and multi-brand garages. Built with **Java 17 LTS, JavaFX, SQLite JDBC, and JUnit 5**, strictly adhering to the **VITyarthi — Build Your Own Project** development roadmap and guidelines.

---

## 📌 1. Project Overview
The **Vehicle Service Management System** automates the end-to-end operational workflow of a vehicle repair and maintenance facility. It centralizes client records, fleet management, service job scheduling, dynamic spare parts tracking, automated GST/tax billing computations, and executive financial dashboards.

The system replaces prone-to-error manual paperwork and spreadsheets with an atomic, transactional, and user-friendly desktop experience.

---

## 🎯 2. Problem Statement & Objectives
### Problem Statement
Independent vehicle repair garages frequently encounter administrative and accounting issues:
- Untracked customer visit histories and fragmented vehicle maintenance logs.
- Duplicate vehicle records and registration ambiguity.
- Manual billing errors in parts totaling, labor cost computations, and sales tax/GST calculations.
- Lack of real-time operational visibility into job statuses (Pending, In Progress, Completed) and outstanding payments.

### Objectives
1. **End-to-End Workflow Automation**: Digitize customer check-in, job card issuance, parts allocation, job completion, and invoice generation.
2. **Eliminate Billing Discrepancies**: Guarantee accurate billing calculations using automated business logic (`Total = Parts + Labour + 18% Tax`).
3. **Robust Data Integrity**: Enforce strict relational constraints, parameterized JDBC queries, and foreign key cascades using SQLite.
4. **Actionable Insights**: Provide real-time operational and financial visibility via interactive JavaFX visual charts.
5. **Standardized Academic Architecture**: Cleanly demonstrate 5-tier layered Java design, OOP principles, and test-driven verification.

---

## 🌟 3. Key Features

### 🔐 1. Staff Authentication & Security
- Secure staff login portal with SHA-256 password hashing.
- Role-based attributes (Admin, Service Advisor, Mechanic).
- Default credentials seeded on initial launch: `admin` / `admin123`.

### 📊 2. Executive Dashboard & Visual Analytics
- Real-time KPI stat cards:
  - Total Registered Customers
  - Total Vehicles Managed
  - Active Service Jobs (Pending + In Progress)
  - Completed Services
  - Gross Revenue Collected (₹)
  - Pending Outstanding Payments (₹)
- **JavaFX PieChart**: Distribution of service jobs across lifecycle statuses.
- **JavaFX BarChart**: Financial comparison of collected vs. pending revenue.

### 👥 3. Customer Management
- Complete CRUD operations (Create, Read, Update, Delete).
- Real-time search by Customer Name, Phone Number, or Email.
- Single-click dialog inspecting all vehicles owned by a customer.
- Deletion guard preventing removal of customers with active vehicles.

### 🚘 4. Vehicle Registry & Fleet Tracking
- Complete CRUD operations and search by Reg #, Brand, Model, or Customer Name.
- Uniqueness validation preventing duplicate vehicle registration numbers.
- Manufacturing year validation (1900 to current year + 1).
- Vehicle classification (Sedan, SUV, Hatchback, Motorcycle, Coupe, Van, Truck).

### 🔧 5. Service Job Management & Parts Ledger
- Service request issuance with custom issue descriptions and estimated labor costs.
- State-machine status progression: `Pending` ➔ `In Progress` ➔ `Completed` ➔ `Cancelled`.
- Itemized spare parts and consumables register:
  - Add parts with quantities and unit rates.
  - Automatic calculation: `Line Item Total = Quantity × Unit Price`.
  - Automatic rollup updating `parts_cost` on the service record.
- Editable labor charges.

### 💳 6. Automated Billing & Invoicing Engine
- Strict automated calculation logic:
  $$\text{Parts Cost} = \sum (\text{item.quantity} \times \text{item.unitPrice})$$
  $$\text{Subtotal} = \text{Parts Cost} + \text{Labour Cost}$$
  $$\text{Tax (GST 18\%)} = \text{Subtotal} \times 0.18$$
  $$\text{Total Amount} = \text{Subtotal} + \text{Tax}$$
- Enforces precondition that only **Completed** services can be invoiced.
- Enforces strict 1-to-1 relationship between service job and tax invoice.
- Payment status toggling (`Unpaid` ➔ `Paid`).
- Printable/exportable tax invoice inspection modal.

---

## 🛠️ 4. Technology Stack
| Layer | Technology | Purpose |
| :--- | :--- | :--- |
| **Language** | Java 17 LTS (Temurin / OpenJDK) | Core OOP implementation, pattern matching, records |
| **UI Framework** | OpenJFX 17 (JavaFX) | Modern desktop graphical user interface and charts |
| **Styling** | Custom CSS3 (`style.css`) | Professional dark-sidebar & modern card UI theme |
| **Database** | SQLite 3 | Embedded zero-configuration relational database |
| **Database Access** | JDBC (`PreparedStatement`) | Parameterized SQL execution preventing SQL injection |
| **Testing** | JUnit 5 Jupiter | Unit, validation, constraint, and integration workflow tests |
| **Build Tool** | Apache Maven 3.9+ | Dependency management, compilation, and packaging |
| **Version Control**| Git + GitHub | Incremental, milestone-driven commit history |

---

## 🏗️ 5. System Architecture

The application is engineered strictly around a **Layered Java Architecture**:

```
Presentation Layer (JavaFX GUI, CSS Theme, Charts)
                       ↓
Controller Layer (Login, Dashboard, Customer, Vehicle, Service, Billing)
                       ↓
Business Logic & Service Layer (ValidationUtil, ErrorHandler, BillingService, ReportService)
                       ↓
Data Access Object (DAO) Layer (CustomerDAO, VehicleDAO, ServiceDAO, BillDAO, UserDAO)
                       ↓
Persistence Layer (SQLite Database: vehicleservice.db with Foreign Keys & ACID)
```


---

## 📂 6. Project Structure

```
Vehicle-Service-Management-System/
├── pom.xml                                   # Maven dependencies & build plugins
├── mvnw                                      # Lightweight executable Maven wrapper
├── .gitignore                                # Git ignore rules
├── README.md                                 # Main project documentation
├── statement.md                              # Problem statement, scope & target users
├── database/
│   └── schema.sql                            # SQLite DDL schema and index definitions
├── docs/
│   ├── system_architecture.md                # System Architecture & Layered Diagram
│   ├── use_case_diagram.md                   # Actor Use Case Flowcharts
│   ├── workflow_diagram.md                   # End-to-End Business Workflow Diagram
│   ├── sequence_diagram.md                   # Runtime Billing Sequence Diagram
│   ├── class_diagram.md                      # OOP Class Diagram & Entity Relationships
│   ├── er_diagram.md                         # Relational Entity-Relationship Diagram
│   └── FINAL_REPORT.md                       # Comprehensive 15-Section Academic Report
├── screenshots/
│   └── .gitkeep                              # Application UI screenshots placeholder
├── src/
│   ├── main/
│   │   ├── java/com/vehicleservice/
│   │   │   ├── Main.java                     # Application entry point & seeder
│   │   │   ├── config/
│   │   │   │   └── DatabaseConfig.java       # SQLite JDBC connection & migration manager
│   │   │   ├── model/
│   │   │   │   ├── Customer.java             # Customer entity
│   │   │   │   ├── Vehicle.java              # Vehicle entity
│   │   │   │   ├── ServiceRecord.java        # Service job card entity
│   │   │   │   ├── ServiceItem.java          # Spare part / consumable line item entity
│   │   │   │   ├── Bill.java                 # Tax invoice entity
│   │   │   │   └── User.java                 # Staff credentials entity
│   │   │   ├── dao/
│   │   │   │   ├── CustomerDAO.java          # Parameterized JDBC for Customers
│   │   │   │   ├── VehicleDAO.java           # Parameterized JDBC for Vehicles
│   │   │   │   ├── ServiceDAO.java           # Parameterized JDBC for Jobs & Items
│   │   │   │   ├── BillDAO.java              # Parameterized JDBC for Invoices
│   │   │   │   └── UserDAO.java              # Parameterized JDBC for Staff Authentication
│   │   │   ├── service/
│   │   │   │   ├── CustomerService.java      # Customer business logic
│   │   │   │   ├── VehicleService.java       # Vehicle uniqueness & ownership rules
│   │   │   │   ├── ServiceManagement.java    # Service lifecycle & parts aggregation
│   │   │   │   ├── BillingService.java       # Automated billing formulas & tax rollup
│   │   │   │   ├── ReportService.java        # Dashboard KPI and chart aggregations
│   │   │   │   └── AuthService.java          # Staff login & session state
│   │   │   ├── controller/
│   │   │   │   ├── LoginController.java      # Login screen controller
│   │   │   │   ├── DashboardController.java  # Dashboard & charts controller
│   │   │   │   ├── CustomerController.java   # Customer CRUD & search controller
│   │   │   │   ├── VehicleController.java    # Vehicle CRUD & registry controller
│   │   │   │   ├── ServiceController.java    # Service job & parts controller
│   │   │   │   └── BillingController.java    # Billing & payment tracking controller
│   │   │   └── util/
│   │   │       ├── ValidationUtil.java       # Regex & numerical validation utilities
│   │   │       └── ErrorHandler.java         # Friendly errors & alert dialogs
│   │   └── resources/
│   │       ├── styles/
│   │       │   └── style.css                 # Modern CSS design theme
│   │       └── database/
│   │           └── schema.sql                # Embedded resource schema
│   └── test/
│       └── java/com/vehicleservice/
│           ├── CustomerDAOTest.java          # Customer CRUD & search tests
│           ├── VehicleDAOTest.java           # Vehicle CRUD & uniqueness tests
│           ├── ServiceDAOTest.java           # Service jobs & parts rollup tests
│           ├── BillingServiceTest.java       # Billing math & tax calculation tests
│           ├── ValidationUtilTest.java       # Format & field validation tests
│           ├── DatabaseErrorHandlingTest.java# Constraint violation handling tests
│           ├── IntegrationWorkflowTest.java  # Complete end-to-end lifecycle test
│           └── TestSuiteRunner.java          # Headless CLI test runner
```

---

## 🗄️ 7. Database Design & Relationships
The SQLite database (`vehicleservice.db`) incorporates foreign key referential integrity (`PRAGMA foreign_keys = ON;`) and explicit indices for fast lookups.

### Table Schema Summary
1. **`users`**: Staff credentials (`id`, `username` UNIQUE, `password_hash`, `full_name`, `role`, `created_at`).
2. **`customers`**: Customer profiles (`id`, `name`, `phone`, `email`, `address`, `created_at`).
3. **`vehicles`**: Vehicle registry (`id`, `customer_id` FK, `registration_number` UNIQUE, `brand`, `model`, `vehicle_type`, `manufacturing_year`).
4. **`service_records`**: Workshop jobs (`id`, `vehicle_id` FK, `service_type`, `description`, `service_date`, `status` CHECK, `labour_cost`, `parts_cost`).
5. **`service_items`**: Parts consumed (`id`, `service_id` FK, `item_name`, `quantity`, `unit_price`, `total_price`).
6. **`bills`**: Tax invoices (`id`, `service_id` UNIQUE FK, `subtotal`, `tax`, `total_amount`, `payment_status` CHECK, `bill_date`).

### Relationships
- **Customer 1 ➔ Many Vehicles**: A customer can register and manage multiple vehicles.
- **Vehicle 1 ➔ Many Service Records**: A vehicle can have multiple service job visits over time.
- **Service Record 1 ➔ Many Service Items**: A service job can consume multiple spare parts and fluids.
- **Service Record 1 ➔ 1 Bill**: Each completed service job has exactly one generated tax invoice.



## 🚀 8. Installation & Setup

### Prerequisites
- **Java Development Kit (JDK) 17+** (Java 17 LTS recommended)
- **Git**
- *(Optional)* Apache Maven 3.8+ (A self-contained wrapper `./mvnw` is included in the project)

### Clone / Locate the Project
```bash
c

## ▶️ 9. How to Run the Application

### Method 1: Using the JavaFX Maven Plugin (Recommended)
```bash
./mvnw javafx:run
```

### Method 2: Package into JAR and Run
```bash
./mvnw clean package
java -jar target/vehicle-service-management-system-1.0.0.jar
```

### Initial Credentials
On the first application run, the system automatically creates the SQLite database and seeds default administrative credentials:
- **Username**: `admin`
- **Password**: `admin123`

---

## 🧪 10. Testing Instructions

The application includes an exhaustive test suite of **31 JUnit 5 tests** verifying every module, calculation rule, constraint check, and database error handler.

### Run All Tests via Maven
```bash
./mvnw test
```

### Run Tests via Headless Test Runner
```bash
./mvnw test-compile
java -cp "target/classes:target/test-classes:$(./mvnw dependency:build-classpath | grep -A 1 'Dependencies classpath:' | tail -n 1)" com.vehicleservice.TestSuiteRunner
```



---

## 🔮 11. Future Enhancements (Version 2.0 Roadmap)
In accordance with academic scope control, the initial release intentionally excludes complex third-party dependencies. Recommended future enhancements include:
1. **SMS / WhatsApp Gateway Integration**: Automated notification alerts sent to customer phones when a service transitions to "Completed" or "Ready for Delivery".
2. **Payment Gateway Integration**: Direct QR code generation for UPI payments or Stripe integration for debit/credit cards.
3. **Inventory Management & Low-Stock Alerts**: Automatic stock level decrementing with purchase order triggers for low-inventory parts.
4. **PDF Invoice Export**: Direct PDF generation using iText / Apache PDFBox for emailing receipts to clients.
5. **OBD-II Diagnostic Telematics**: Hardware integration to read vehicle fault codes via Bluetooth OBD-II scanners.

---

## 📚 12. References
1. Oracle Corporation. *Java SE 17 Documentation & Language Specification*. https://docs.oracle.com/en/java/javase/17/
2. OpenJFX Project. *JavaFX 17 Documentation & Controls Reference*. https://openjfx.io/
3. SQLite Development Team. *SQLite 3 Documentation & Foreign Key Reference*. https://www.sqlite.org/
4. JUnit 5 Team. *JUnit 5 User Guide & Jupiter API*. https://junit.org/junit5/docs/current/user-guide/
5. VITyarthi Academic Guidelines. *Build Your Own Project — General Instructions & Submission Guidelines*.
