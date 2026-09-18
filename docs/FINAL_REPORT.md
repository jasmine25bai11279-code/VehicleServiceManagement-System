# PROJECT REPORT: VEHICLE SERVICE MANAGEMENT SYSTEM

---

## 1. Cover Page

**Project Title:** Vehicle Service Management System (VSMS)  
**Track:** VITyarthi — Build Your Own Project  
**Domain:** Java Desktop Application Development, Relational Databases, and Software Engineering  
**Technology Stack:** Java 17 LTS, JavaFX 17, SQLite JDBC, JUnit 5, Maven  
**Architecture:** 5-Tier Layered Architecture (UI, Controller, Service, DAO, Database)  
**Academic Term:** Academic Year 2026  
**Status:** Completed, Built & Verified  

---

## 2. Introduction
In the contemporary automotive services sector, the efficiency of workshop operations relies heavily on rapid customer check-in, accurate maintenance tracking, transparent spare parts pricing, and compliant taxation and invoicing. Manual or spreadsheet-driven systems often fail when handling customer relationship history, service job queueing, and multi-line item billing calculations.

The **Vehicle Service Management System (VSMS)** is a comprehensive, standalone Java desktop application developed to modernize the day-to-day workflow of multi-brand automobile workshops. The system combines an intuitive graphical user interface (JavaFX) with a robust business logic layer and an embedded, transactional relational database (SQLite), guaranteeing complete data integrity, instant operational feedback, and zero calculation discrepancies.

---

## 3. Problem Statement
Automobile service workshops encounter recurring operational bottlenecks:
1. **Disorganized Customer and Vehicle Data**: Difficulties tracking repeat client visits, multiple vehicles per family or corporate fleet, and historical maintenance logs.
2. **Billing Inconsistencies and Computational Errors**: Discrepancies between parts consumed, manual labor charges, and applicable tax rates (GST/VAT).
3. **Operational Blind Spots**: Lack of real-time visibility into the status of ongoing jobs (`Pending`, `In Progress`, `Completed`, `Cancelled`) and overdue payments.
4. **Data Corruption & Vulnerability**: Traditional file-based storage or unprotected SQL queries risk data loss, constraint violations, and injection vulnerabilities.

VSMS solves these issues through a structured, layered Java application that centralizes all customer, vehicle, job, inventory, and payment operations.

---

## 4. Functional Requirements

### Module 1: Customer & Vehicle Management
- **FR-1.1**: The system must provide complete CRUD (Create, Read, Update, Delete) operations for customer profiles (name, phone, email, address).
- **FR-1.2**: The system must support real-time searching of customer records by name, telephone number, or email address.
- **FR-1.3**: The system must register vehicles with unique registration numbers, brand, model, vehicle type, and manufacturing year.
- **FR-1.4**: A customer must be able to own and service multiple vehicles (1-to-many relationship).
- **FR-1.5**: The system must prevent deletion of a customer profile if active registered vehicles or service histories are associated.

### Module 2: Service Job Management
- **FR-2.1**: Staff must be able to open new service requests for any registered vehicle, specifying service type, issue description, and base labor charge.
- **FR-2.2**: The system must enforce a formal lifecycle state machine: `Pending` ➔ `In Progress` ➔ `Completed` ➔ `Cancelled`.
- **FR-2.3**: Technicians must be able to add and remove itemized spare parts and consumables for any service job.
- **FR-2.4**: The system must automatically recalculate total parts costs whenever line items are modified.

### Module 3: Billing & Payment Management
- **FR-3.1**: The system must enforce that a bill can only be generated for a service job that has reached the `Completed` status.
- **FR-3.2**: Billing calculations must be fully automated based on:
  $$\text{Parts Cost} = \sum (\text{item.quantity} \times \text{item.unitPrice})$$
  $$\text{Subtotal} = \text{Parts Cost} + \text{Labour Cost}$$
  $$\text{Tax (GST 18\%)} = \text{Subtotal} \times 0.18$$
  $$\text{Total Amount} = \text{Subtotal} + \text{Tax}$$
- **FR-3.3**: The system must enforce a strict 1-to-1 relationship between a service record and its tax invoice.
- **FR-3.4**: Invoices must support status tracking (`Unpaid` ➔ `Paid`).

### Module 4: Dashboard & Reports
- **FR-4.1**: The dashboard must display real-time KPI cards: Total Customers, Registered Vehicles, Active Jobs, Completed Jobs, Collected Revenue, and Pending Revenue.
- **FR-4.2**: The dashboard must render a JavaFX `PieChart` visualizing the breakdown of services across all statuses.
- **FR-4.3**: The dashboard must render a JavaFX `BarChart` comparing collected revenue versus pending payments.

---

## 5. Non-Functional Requirements
- **Performance**: CRUD and search operations on the local SQLite database execute in less than 50 milliseconds.
- **Usability**: Intuitive, responsive desktop interface with modern styling, distinct status badge colors, and descriptive dialog feedback.
- **Security**: Parameterized JDBC `PreparedStatements` prevent SQL injection. Staff passwords are encrypted using SHA-256 cryptographic hashing.
- **Reliability & Data Integrity**: SQLite foreign key constraints (`PRAGMA foreign_keys = ON;`) guarantee referential integrity and cascading consistency.
- **Maintainability**: Strict 5-tier layered design cleanly separating UI, controllers, business rules, DAOs, and persistence.
- **Error Handling**: Graceful exception capture with friendly human-readable alerts rather than raw stack traces.

---

## 6. System Architecture

The application adopts a **5-Tier Layered Java Architecture**:

```
+-------------------------------------------------------------+
|               Presentation Layer (JavaFX UI)                |
|  Main Window, Sidebar, Form Dialogs, JavaFX Pie & Bar Charts |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                      Controller Layer                       |
|   LoginController, DashboardController, CustomerController,  |
|     VehicleController, ServiceController, BillingController  |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|            Business Logic & Service Layer                   |
|   CustomerService, VehicleService, ServiceManagement,       |
|    BillingService, ReportService, AuthService, Validation   |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|               Data Access Object (DAO) Layer                |
|    CustomerDAO, VehicleDAO, ServiceDAO, BillDAO, UserDAO    |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|              Persistence Layer (SQLite Database)            |
|       vehicleservice.db (6 Relational Tables, Indexes)       |
+-------------------------------------------------------------+
```

---

## 7. Design Diagrams

### 7.1 Use Case Diagram
*(See [docs/use_case_diagram.md](file:///Users/arihantjain/.gemini/antigravity/scratch/Vehicle-Service-Management-System/docs/use_case_diagram.md) for full interactive Mermaid view)*
- **Actors**: Service Advisor, Workshop Supervisor, Billing Clerk, System Administrator.
- **Key Use Cases**: Staff Authentication, Manage Customers, Register Vehicles, Issue Service Requests, Manage Spare Parts, Update Job Status, Generate Tax Invoice, Process Payment, View Executive Dashboard.

### 7.2 Core Application Workflow Diagram
*(See [docs/workflow_diagram.md](file:///Users/arihantjain/.gemini/antigravity/scratch/Vehicle-Service-Management-System/docs/workflow_diagram.md) for full flowchart)*
$$\text{Customer Arrives} \rightarrow \text{Register Customer} \rightarrow \text{Register Vehicle} \rightarrow \text{Create Service (Pending)} \rightarrow \text{Work In Progress (Add Parts)} \rightarrow \text{Job Completed} \rightarrow \text{Generate Bill (18\% GST)} \rightarrow \text{Collect Payment} \rightarrow \text{Delivery \& Dashboard Update}$$

### 7.3 Sequence Diagram: Service Completion to Bill Generation
*(See [docs/sequence_diagram.md](file:///Users/arihantjain/.gemini/antigravity/scratch/Vehicle-Service-Management-System/docs/sequence_diagram.md) for full sequence interaction)*
- Highlights message flows between Staff UI, `BillingController`, `BillingService`, `ServiceDAO`, `BillDAO`, and SQLite JDBC.

### 7.4 Class Diagram
*(See [docs/class_diagram.md](file:///Users/arihantjain/.gemini/antigravity/scratch/Vehicle-Service-Management-System/docs/class_diagram.md) for complete class models)*
- Details models (`Customer`, `Vehicle`, `ServiceRecord`, `ServiceItem`, `Bill`, `User`), services, DAOs, and controller mappings.

### 7.5 Entity-Relationship (ER) Diagram
*(See [docs/er_diagram.md](file:///Users/arihantjain/.gemini/antigravity/scratch/Vehicle-Service-Management-System/docs/er_diagram.md) for full relational schema)*
- Outlines the 6 relational tables (`users`, `customers`, `vehicles`, `service_records`, `service_items`, `bills`), primary keys, foreign keys, and cascading relationships.

---

## 8. Design Decisions & Rationale

1. **Why JavaFX Desktop instead of Web/Spring Boot?**
   - In accordance with the project guidelines, a pure Java desktop application avoids unnecessary web-stack complexities (servlet containers, CORS, HTTP states, JavaScript frameworks) while providing instant local responsiveness, native desktop windowing, and rich interactive visual charts.
2. **Why SQLite + JDBC instead of ORM (Hibernate)?**
   - Direct JDBC using `PreparedStatement` provides complete transparency, lower memory overhead, zero configuration, and explicit demonstration of core SQL, transaction handling, and connection lifecycles. SQLite is an embedded zero-configuration database that packages cleanly without requiring external server installation.
3. **Why Separation of Model, DAO, and Service Layers?**
   - Keeps business validations (e.g., verifying registration number format or checking if a service is completed before billing) decoupled from persistence operations. This enables isolated unit testing of business logic without hitting the database.
4. **Why 18% Fixed GST Default?**
   - Automotive repair services standardly apply an 18% Goods and Services Tax (GST) rate. The architecture encapsulates this in `DatabaseConfig.getTaxRate()` for immediate configurability.

---

## 9. Implementation Details

### Module Organization
- **`com.vehicleservice.model`**: POJO classes encapsulating state with private fields, full accessor/mutator pairs, and descriptive `toString()` representations.
- **`com.vehicleservice.dao`**: Database access layer utilizing parameterized SQL queries and `try-with-resources` ensuring zero connection leaks.
- **`com.vehicleservice.service`**: Domain logic containing calculations, state transitions, and validation guards.
- **`com.vehicleservice.controller`**: JavaFX UI screen controllers managing table models, search filtering, and event dispatching.
- **`com.vehicleservice.util`**: Validation regex patterns (`ValidationUtil`) and friendly error translation (`ErrorHandler`).

---

## 10. Screenshots / Results

The application delivers a polished, responsive user interface:
- **Login View**: Card-based login with password masking and validation alerts.
- **Dashboard View**: Six executive KPI cards with distinct accent colors, real-time JavaFX `PieChart` showing service status distributions, and `BarChart` comparing financial revenues.
- **Customer View**: TableView with columns for ID, Name, Phone, Email, Address, and Date. Search filtering and modal forms for adding/editing customers.
- **Vehicle View**: TableView with vehicle specifications and customer associations. Uniqueness check ensures duplicate registration numbers cannot be submitted.
- **Service Jobs View**: TableView with color-coded status badges (`Pending`, `In Progress`, `Completed`, `Cancelled`). Integrated parts manager for adding spare parts and updating labor charges.
- **Billing View**: Displays subtotal, 18% tax, and final amount. Dialog for generating new invoices with real-time price preview and one-click "Mark as Paid".

---

## 11. Testing Approach & Verification Results

A test-driven methodology was implemented using **JUnit 5 Jupiter**, comprising **31 comprehensive test cases** spanning 7 test suites:

```
-------------------------------------------------------
 T E S T S   E X E C U T I O N   R E S U L T S
-------------------------------------------------------
✓ BillingServiceTest            (6 tests, 0 failures)
  - Sum of parts cost calculation
  - Subtotal = parts + labour
  - Tax calculation at 18%
  - Total calculation = subtotal + tax
  - Negative cost rejection
  - Empty items handling
✓ ValidationUtilTest            (13 tests, 0 failures)
  - Valid and invalid email formats
  - Valid and invalid phone numbers
  - Manufacturing year boundary tests
  - Field constraint assertions
✓ CustomerDAOTest               (4 tests, 0 failures)
  - Customer insert and retrieval
  - Customer update
  - Customer deletion
  - Search by name and phone
✓ VehicleDAOTest                (3 tests, 0 failures)
  - Vehicle insertion and retrieval
  - Duplicate registration number rejection
  - Customer vehicle associations
✓ ServiceDAOTest                (2 tests, 0 failures)
  - Service request lifecycle and status updates
  - Parts addition and automatic parts_cost rollup
✓ DatabaseErrorHandlingTest     (2 tests, 0 failures)
  - Foreign key constraint enforcement
  - Check constraint enforcement
✓ IntegrationWorkflowTest       (1 test, 0 failures)
  - Complete end-to-end business cycle: Customer ➔ Vehicle ➔ Service ➔ Parts ➔ Completion ➔ Bill ➔ Payment ➔ Dashboard Verification

Total Tests Executed: 31 | Passed: 31 | Failures: 0 | Errors: 0
Build Status: SUCCESS
```

---

## 12. Challenges Faced & Solutions

1. **Foreign Key Support in SQLite**:
   - *Challenge*: By default, SQLite disables foreign key enforcement on new JDBC connections.
   - *Solution*: Explicitly configured `stmt.execute("PRAGMA foreign_keys = ON;");` upon connection creation in `DatabaseConfig.getConnection()`.
2. **Synchronizing Parts Costs with Service Records**:
   - *Challenge*: Adding or removing spare parts could cause discrepancy with the service record's `parts_cost`.
   - *Solution*: Implemented an atomic `recalculatePartsCost(serviceId)` method in `ServiceDAO` executed automatically whenever a line item is inserted or deleted.
3. **Headless Execution Compatibility**:
   - *Challenge*: Running JavaFX controls inside automated CI/terminal test environments can fail due to uninitialized graphics toolkits.
   - *Solution*: Wrapped UI alert operations in `ErrorHandler` with safe platform detection and provided the standalone `TestSuiteRunner` for pure CLI test execution.

---

## 13. Learnings & Key Takeaways
- **Layered Modularity**: Decoupling persistence from business rules made the application straightforward to test, debug, and extend.
- **Defensive Database Engineering**: Using parameterized queries and constraints prevented bad state from ever reaching storage.
- **User-Centric Feedback**: Translating low-level SQLite constraint errors into clear UI messages significantly improved usability.

---

## 14. Future Enhancements
1. Automated customer notification via SMS / WhatsApp upon service completion.
2. Direct UPI / QR-code payment integration.
3. Low-stock inventory alerts for spare parts.
4. Exporting invoices directly as styled PDF documents.
5. OBD-II diagnostic scanner integration.

---

## 15. References
1. Oracle Corporation. *Java SE 17 Documentation & Language Specification*. https://docs.oracle.com/en/java/javase/17/
2. OpenJFX Project. *JavaFX 17 Documentation & Controls Reference*. https://openjfx.io/
3. SQLite Development Team. *SQLite 3 Documentation & Foreign Key Reference*. https://www.sqlite.org/
4. JUnit 5 Team. *JUnit 5 User Guide & Jupiter API*. https://junit.org/junit5/docs/current/user-guide/
5. VITyarthi Academic Guidelines. *Build Your Own Project — General Instructions & Submission Guidelines*.
