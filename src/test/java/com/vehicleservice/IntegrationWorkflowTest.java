package com.vehicleservice;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.model.Bill;
import com.vehicleservice.model.Customer;
import com.vehicleservice.model.ServiceItem;
import com.vehicleservice.model.ServiceRecord;
import com.vehicleservice.model.Vehicle;
import com.vehicleservice.service.BillingService;
import com.vehicleservice.service.CustomerService;
import com.vehicleservice.service.ReportService;
import com.vehicleservice.service.ReportService.DashboardSummary;
import com.vehicleservice.service.ServiceManagement;
import com.vehicleservice.service.VehicleService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("End-to-End Integration Workflow Test: Customer -> Vehicle -> Service -> Bill")
public class IntegrationWorkflowTest {

    private static final String TEST_DB = "test_integration.db";

    @BeforeAll
    static void initDb() throws SQLException {
        new File(TEST_DB).delete();
        DatabaseConfig.setDatabaseUrl("jdbc:sqlite:" + TEST_DB);
        DatabaseConfig.initializeDatabase();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM bills;");
            stmt.execute("DELETE FROM service_items;");
            stmt.execute("DELETE FROM service_records;");
            stmt.execute("DELETE FROM vehicles;");
            stmt.execute("DELETE FROM customers;");
        }
    }

    @Test
    @DisplayName("Complete business cycle: Customer -> Vehicle -> Service Request -> Parts -> Completion -> Billing -> Payment")
    void testCompleteServiceAndBillingLifecycle() throws Exception {
        CustomerService customerService = new CustomerService();
        VehicleService vehicleService = new VehicleService();
        ServiceManagement serviceManagement = new ServiceManagement();
        BillingService billingService = new BillingService();
        ReportService reportService = new ReportService();

        // 1. Register Customer
        Customer customer = customerService.createCustomer(
                "Ananya Deshmukh", "9876543210", "ananya@example.com", "7th Main Road, Indiranagar"
        );
        assertNotNull(customer);
        assertTrue(customer.getId() > 0);

        // 2. Register Vehicle for Customer
        Vehicle vehicle = vehicleService.registerVehicle(
                customer.getId(), "KA05MK4444", "Volkswagen", "Polo GT", "Hatchback", 2021
        );
        assertNotNull(vehicle);
        assertTrue(vehicle.getId() > 0);
        assertEquals(customer.getId(), vehicle.getCustomerId());

        // 3. Create Service Request (Initial Labour = ₹700)
        ServiceRecord service = serviceManagement.createServiceRequest(
                vehicle.getId(), "Full Diagnostic & Brake Overhaul", "Regular checkup and brake squeaking issue", "2026-09-18", 700.0
        );
        assertNotNull(service);
        assertEquals(ServiceManagement.STATUS_PENDING, service.getStatus());

        // 4. Try generating bill before completion -> must fail
        assertThrows(IllegalStateException.class, () -> billingService.generateBill(service.getId()),
                "Cannot bill a service that is still Pending");

        // 5. Add Service Items (Parts)
        // Part 1: Front Brake Discs (Qty: 2, Unit: 1500 -> 3000)
        ServiceItem item1 = serviceManagement.addServiceItem(service.getId(), "Front Brake Discs", 2, 1500.0);
        assertEquals(3000.0, item1.getTotalPrice(), 0.001);

        // Part 2: DOT4 Brake Fluid (Qty: 1, Unit: 450 -> 450)
        ServiceItem item2 = serviceManagement.addServiceItem(service.getId(), "DOT4 Brake Fluid", 1, 450.0);
        assertEquals(450.0, item2.getTotalPrice(), 0.001);

        // Parts sum = 3000 + 450 = 3450
        ServiceRecord refreshedService = serviceManagement.getServiceById(service.getId()).orElseThrow();
        assertEquals(3450.0, refreshedService.getPartsCost(), 0.001);
        assertEquals(700.0, refreshedService.getLabourCost(), 0.001);

        // 6. Transition Service to In Progress, then Completed
        serviceManagement.updateServiceStatus(service.getId(), ServiceManagement.STATUS_IN_PROGRESS);
        serviceManagement.updateServiceStatus(service.getId(), ServiceManagement.STATUS_COMPLETED);

        // 7. Generate Bill
        // Subtotal = Parts (3450) + Labour (700) = 4150.00
        // Tax (18%) = 4150 * 0.18 = 747.00
        // Total = 4150 + 747 = 4897.00
        Bill bill = billingService.generateBill(service.getId());
        assertNotNull(bill);
        assertEquals(4150.00, bill.getSubtotal(), 0.001);
        assertEquals(747.00, bill.getTax(), 0.001);
        assertEquals(4897.00, bill.getTotalAmount(), 0.001);
        assertEquals(BillingService.PAYMENT_UNPAID, bill.getPaymentStatus());

        // Prevent duplicate bill for the same service
        assertThrows(IllegalStateException.class, () -> billingService.generateBill(service.getId()));

        // 8. Process Payment
        boolean paid = billingService.markBillAsPaid(bill.getId());
        assertTrue(paid);
        Bill paidBill = billingService.getBillById(bill.getId()).orElseThrow();
        assertEquals(BillingService.PAYMENT_PAID, paidBill.getPaymentStatus());

        // 9. Verify Dashboard Summary
        DashboardSummary summary = reportService.getDashboardSummary();
        assertEquals(1, summary.totalCustomers);
        assertEquals(1, summary.totalVehicles);
        assertEquals(1, summary.totalServices);
        assertEquals(1, summary.completedServices);
        assertEquals(1, summary.paidBills);
        assertEquals(0, summary.unpaidBills);
        assertEquals(4897.00, summary.collectedRevenue, 0.001);
        assertEquals(0.00, summary.pendingRevenue, 0.001);
    }
}
