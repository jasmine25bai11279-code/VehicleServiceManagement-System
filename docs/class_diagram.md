# Class Diagram: Vehicle Service Management System

## Object-Oriented Architecture Diagram
This class diagram highlights the entity models, encapsulations, DAO data access objects, services, and JavaFX controllers.

```mermaid
classDiagram
    %% Model Classes
    class Customer {
        -int id
        -String name
        -String phone
        -String email
        -String address
        -String createdAt
        +getId() int
        +getName() String
        +getPhone() String
        +getEmail() String
        +getAddress() String
    }

    class Vehicle {
        -int id
        -int customerId
        -String registrationNumber
        -String brand
        -String model
        -String vehicleType
        -int manufacturingYear
        -String customerName
        +getId() int
        +getCustomerId() int
        +getRegistrationNumber() String
        +getBrand() String
        +getModel() String
        +getManufacturingYear() int
    }

    class ServiceRecord {
        -int id
        -int vehicleId
        -String serviceType
        -String description
        -String serviceDate
        -String status
        -double labourCost
        -double partsCost
        -List~ServiceItem~ items
        +getId() int
        +getStatus() String
        +getLabourCost() double
        +getPartsCost() double
        +getTotalCost() double
    }

    class ServiceItem {
        -int id
        -int serviceId
        -String itemName
        -int quantity
        -double unitPrice
        -double totalPrice
        +getId() int
        +getItemName() String
        +getQuantity() int
        +getUnitPrice() double
        +getTotalPrice() double
    }

    class Bill {
        -int id
        -int serviceId
        -double subtotal
        -double tax
        -double totalAmount
        -String paymentStatus
        -String billDate
        +getId() int
        +getSubtotal() double
        +getTax() double
        +getTotalAmount() double
        +getPaymentStatus() String
    }

    class User {
        -int id
        -String username
        -String passwordHash
        -String fullName
        -String role
        +getUsername() String
        +getRole() String
    }

    %% Relationships
    Customer "1" *-- "0..*" Vehicle : owns
    Vehicle "1" *-- "0..*" ServiceRecord : serviced in
    ServiceRecord "1" *-- "0..*" ServiceItem : contains
    ServiceRecord "1" -- "0..1" Bill : billed by

    %% Service Layer
    class CustomerService {
        -CustomerDAO customerDAO
        -VehicleDAO vehicleDAO
        +createCustomer(name, phone, email, address) Customer
        +updateCustomer(...) boolean
        +deleteCustomer(id) boolean
        +searchCustomers(query) List~Customer~
    }

    class VehicleService {
        -VehicleDAO vehicleDAO
        -CustomerDAO customerDAO
        +registerVehicle(...) Vehicle
        +isRegistrationUnique(regNo, excludeId) boolean
        +getVehiclesByCustomerId(customerId) List~Vehicle~
    }

    class ServiceManagement {
        -ServiceDAO serviceDAO
        -VehicleDAO vehicleDAO
        +createServiceRequest(...) ServiceRecord
        +updateServiceStatus(id, newStatus) boolean
        +addServiceItem(...) ServiceItem
        +removeServiceItem(itemId, serviceId) boolean
    }

    class BillingService {
        -BillDAO billDAO
        -ServiceDAO serviceDAO
        -double taxRate
        +calculatePartsCost(items) double
        +calculateSubtotal(parts, labour) double
        +calculateTax(subtotal) double
        +calculateTotal(subtotal, tax) double
        +generateBill(serviceId) Bill
        +markBillAsPaid(billId) boolean
    }

    %% Associations with DAOs
    CustomerService ..> CustomerDAO
    VehicleService ..> VehicleDAO
    ServiceManagement ..> ServiceDAO
    BillingService ..> BillDAO
```
