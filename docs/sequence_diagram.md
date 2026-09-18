# Sequence Diagram: Vehicle Service Management System

## Scenario: Completing a Service & Generating an Automated Bill
This sequence diagram details the runtime interaction between the Staff Member, `BillingController`, `BillingService`, `ServiceDAO`, `BillDAO`, and the SQLite Database.

```mermaid
sequenceDiagram
    autonumber
    actor Staff as Staff / Billing Clerk
    participant UI as BillingController (UI)
    participant Srv as BillingService
    participant SrvDAO as ServiceDAO
    participant BillDAO as BillDAO
    participant DB as SQLite Database (JDBC)

    Staff->>UI: Clicks "+ Generate New Bill"
    UI->>SrvDAO: getServicesByStatus("Completed")
    SrvDAO->>DB: SELECT * FROM service_records WHERE status = 'Completed'
    DB-->>SrvDAO: List of Completed ServiceRecords
    SrvDAO-->>UI: Return completed services list
    UI-->>Staff: Displays Modal with eligible unbilled services

    Staff->>UI: Selects Service #12 and clicks "OK"
    UI->>Srv: generateBill(serviceId = 12)
    
    rect rgb(240, 248, 255)
        note right of Srv: Validation & Preconditions
        Srv->>SrvDAO: findById(12)
        SrvDAO->>DB: SELECT * FROM service_records WHERE id = 12
        DB-->>SrvDAO: ServiceRecord(status="Completed", labourCost=700)
        SrvDAO-->>Srv: ServiceRecord
        
        Srv->>BillDAO: findByServiceId(12)
        BillDAO->>DB: SELECT * FROM bills WHERE service_id = 12
        DB-->>BillDAO: null (No existing bill)
        BillDAO-->>Srv: Optional.empty()
    end

    rect rgb(255, 250, 240)
        note right of Srv: Automatic Computation Rules
        Srv->>SrvDAO: getItemsByServiceId(12)
        SrvDAO->>DB: SELECT * FROM service_items WHERE service_id = 12
        DB-->>SrvDAO: List of ServiceItems
        SrvDAO-->>Srv: Return items
        
        Srv->>Srv: calculatePartsCost(items) -> ₹3450.00
        Srv->>Srv: calculateSubtotal(3450, 700) -> ₹4150.00
        Srv->>Srv: calculateTax(4150 * 0.18) -> ₹747.00
        Srv->>Srv: calculateTotal(4150 + 747) -> ₹4897.00
    end

    Srv->>BillDAO: insert(Bill)
    BillDAO->>DB: INSERT INTO bills (service_id, subtotal, tax, total_amount, payment_status, bill_date) VALUES (...)
    DB-->>BillDAO: Generated ID (e.g. 5)
    BillDAO-->>Srv: Persisted Bill Object
    Srv-->>UI: Return Bill Object
    UI-->>Staff: Display Success Alert with Invoice Summary

    opt Payment Processing
        Staff->>UI: Clicks "Mark as Paid"
        UI->>Srv: markBillAsPaid(5)
        Srv->>BillDAO: updatePaymentStatus(5, "Paid")
        BillDAO->>DB: UPDATE bills SET payment_status = 'Paid' WHERE id = 5
        DB-->>BillDAO: 1 row affected
        BillDAO-->>Srv: true
        Srv-->>UI: true
        UI-->>Staff: Displays "Payment Recorded Successfully"
    end
```
