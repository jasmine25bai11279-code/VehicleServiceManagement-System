# Workflow Diagram: Vehicle Service Management System

## Core Business Workflow
This diagram illustrates the step-by-step end-to-end lifecycle of a vehicle through the workshop, from customer arrival to final payment settlement.

```mermaid
flowchart TD
    Start([START: Customer Arrives]) --> Auth{Staff Authenticated?}
    Auth -- No --> Login[Staff Signs In with Credentials]
    Login --> Auth
    Auth -- Yes --> Dash[Access Dashboard]

    Dash --> CustCheck{Customer Exists?}
    CustCheck -- No --> CreateCust[Add New Customer Profile<br/>Validate Name, Phone & Email]
    CreateCust --> VehCheck
    CustCheck -- Yes --> SelectCust[Select Existing Customer]
    SelectCust --> VehCheck{Vehicle Registered?}

    VehCheck -- No --> RegVeh[Register Vehicle<br/>Validate Reg No Uniqueness & Year]
    RegVeh --> CreateJob
    VehCheck -- Yes --> SelectVeh[Select Vehicle]

    SelectVeh --> CreateJob[Create Service Request<br/>Set Status = 'Pending', Record Issues & Labour]
    
    CreateJob --> StartWork[Workshop Commences Work<br/>Update Status = 'In Progress']
    
    StartWork --> AddParts[Inspect & Add Parts / Consumables<br/>Auto-recalculate Parts Cost]
    
    AddParts --> MoreParts{More Parts Needed?}
    MoreParts -- Yes --> AddParts
    MoreParts -- No --> FinishWork[Workshop Completes Job<br/>Update Status = 'Completed']

    FinishWork --> GenBill[Generate Official Tax Invoice<br/>Auto-compute Parts + Labour + 18% GST = Total]

    GenBill --> DisplayInvoice[Present Invoice to Customer]
    
    DisplayInvoice --> SettlePayment[Collect Payment<br/>Update Payment Status = 'Paid']

    SettlePayment --> UpdateDash[Live Dashboard Metrics & Charts Refresh]

    UpdateDash --> End([END: Vehicle Delivered to Customer])

    classDef step fill:#e0f2fe,stroke:#0284c7,stroke-width:2px,color:#0369a1;
    classDef decision fill:#fef3c7,stroke:#d97706,stroke-width:2px,color:#92400e;
    classDef term fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#166534;

    class Dash,CreateCust,SelectCust,RegVeh,SelectVeh,CreateJob,StartWork,AddParts,FinishWork,GenBill,DisplayInvoice,SettlePayment,UpdateDash step;
    class Auth,CustCheck,VehCheck,MoreParts decision;
    class Start,End term;
```
