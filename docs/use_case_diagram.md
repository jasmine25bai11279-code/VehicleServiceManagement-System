# Use Case Diagram: Vehicle Service Management System

## Overview
This diagram depicts the primary actors (Staff Member, Workshop Supervisor, Billing Clerk, and Administrator) interacting with the system's core capabilities.

```mermaid
flowchart LR
    Staff((Staff / Advisor))
    Supervisor((Workshop Supervisor))
    Clerk((Billing Clerk))
    Admin((Administrator))

    subgraph SystemBoundary ["Vehicle Service Management System"]
        UC1(["Authenticate / Staff Login"])
        UC2(["Manage Customers (CRUD & Search)"])
        UC3(["Register & Associate Vehicles"])
        UC4(["Create Service Request"])
        UC5(["Update Service Status (Pending/In Progress/Completed)"])
        UC6(["Add & Manage Service Parts / Items"])
        UC7(["Generate Bill & Calculate 18% Tax"])
        UC8(["Record Payment (Unpaid -> Paid)"])
        UC9(["View Invoices & Print Receipts"])
        UC10(["View Real-Time Dashboard & Financial Charts"])
        UC11(["System Configuration & User Management"])
    end

    Staff --> UC1
    Staff --> UC2
    Staff --> UC3
    Staff --> UC4
    Staff --> UC10

    Supervisor --> UC1
    Supervisor --> UC5
    Supervisor --> UC6
    Supervisor --> UC10

    Clerk --> UC1
    Clerk --> UC7
    Clerk --> UC8
    Clerk --> UC9
    Clerk --> UC10

    Admin --> UC1
    Admin --> UC2
    Admin --> UC3
    Admin --> UC4
    Admin --> UC5
    Admin --> UC6
    Admin --> UC7
    Admin --> UC8
    Admin --> UC9
    Admin --> UC10
    Admin --> UC11

    UC7 -.->|includes| UC5
    UC6 -.->|extends| UC4
```

## Description of Primary Use Cases
1. **Authenticate / Staff Login (UC1)**: Users verify identity with username and password. Invalid attempts display sanitized notifications.
2. **Customer Management (UC2)**: Service advisors register customer contact information with phone and email validation.
3. **Vehicle Registration (UC3)**: Vehicles are registered under an owner with uniqueness checks on registration plates.
4. **Create Service Request (UC4)**: Opens a new job ticket with initial issue descriptions and estimated labor cost.
5. **Update Service Status (UC5)**: Advances the service lifecycle from Pending through In Progress to Completed.
6. **Manage Spare Parts / Items (UC6)**: Technicians attach replacement components with quantities and unit prices.
7. **Generate Bill (UC7)**: Automatically computes parts sum, labor, 18% GST, and total. Guarded to require Completed service.
8. **Record Payment (UC8)**: Transitions unpaid invoices to Paid upon receiving settlement from customer.
9. **Dashboard & Financial Charts (UC10)**: Displays aggregated operational metrics and dynamic JavaFX visual charts.
