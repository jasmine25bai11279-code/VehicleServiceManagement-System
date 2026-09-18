package com.vehicleservice;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.dao.CustomerDAO;
import com.vehicleservice.dao.ServiceDAO;
import com.vehicleservice.dao.VehicleDAO;
import com.vehicleservice.model.Customer;
import com.vehicleservice.model.ServiceItem;
import com.vehicleservice.model.ServiceRecord;
import com.vehicleservice.model.Vehicle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Service DAO & Items Management Tests")
public class ServiceDAOTest {

    private static final String TEST_DB = "test_service.db";
    private ServiceDAO serviceDAO;
    private int testVehicleId;

    @BeforeAll
    static void initDb() {
        new File(TEST_DB).delete();
        DatabaseConfig.setDatabaseUrl("jdbc:sqlite:" + TEST_DB);
        DatabaseConfig.initializeDatabase();
    }

    @BeforeEach
    void setupData() throws SQLException {
        serviceDAO = new ServiceDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM bills;");
            stmt.execute("DELETE FROM service_items;");
            stmt.execute("DELETE FROM service_records;");
            stmt.execute("DELETE FROM vehicles;");
            stmt.execute("DELETE FROM customers;");
        }

        Customer cust = customerDAO.insert(new Customer("Siddharth Roy", "9845012345", "sid@example.com", "Chennai"));
        Vehicle veh = vehicleDAO.insert(new Vehicle(cust.getId(), "TN09AZ9999", "Toyota", "Fortuner", "SUV", 2022));
        testVehicleId = veh.getId();
    }

    @Test
    @DisplayName("Should create service record and update status")
    void testCreateAndUpdateStatus() throws SQLException {
        ServiceRecord sr = new ServiceRecord(testVehicleId, "Oil Change", "Standard oil replacement", "2026-09-18", "Pending", 300.0, 0.0);
        ServiceRecord saved = serviceDAO.insert(sr);

        assertTrue(saved.getId() > 0);
        assertEquals("Pending", saved.getStatus());

        // Update status to In Progress
        boolean updated1 = serviceDAO.updateStatus(saved.getId(), "In Progress");
        assertTrue(updated1);
        Optional<ServiceRecord> found1 = serviceDAO.findById(saved.getId());
        assertTrue(found1.isPresent());
        assertEquals("In Progress", found1.get().getStatus());

        // Complete service
        boolean updated2 = serviceDAO.updateStatus(saved.getId(), "Completed");
        assertTrue(updated2);
        Optional<ServiceRecord> found2 = serviceDAO.findById(saved.getId());
        assertTrue(found2.isPresent());
        assertEquals("Completed", found2.get().getStatus());
    }

    @Test
    @DisplayName("Should add service items and automatically recalculate parts cost")
    void testServiceItemsAndPartsRecalculation() throws SQLException {
        ServiceRecord sr = new ServiceRecord(testVehicleId, "Brake Service", "Brake pad replacement", "2026-09-18", "In Progress", 500.0, 0.0);
        ServiceRecord saved = serviceDAO.insert(sr);

        // Add Part 1: Brake Pads (Qty 2 @ 1200 = 2400)
        ServiceItem item1 = new ServiceItem(saved.getId(), "Brake Pads", 2, 1200.0);
        serviceDAO.insertItem(item1);

        // Add Part 2: Brake Fluid (Qty 1 @ 450 = 450)
        ServiceItem item2 = new ServiceItem(saved.getId(), "Brake Fluid", 1, 450.0);
        serviceDAO.insertItem(item2);

        // Verify items retrieved
        List<ServiceItem> items = serviceDAO.getItemsByServiceId(saved.getId());
        assertEquals(2, items.size());

        // Verify parts cost updated on service record: 2400 + 450 = 2850
        ServiceRecord reloaded = serviceDAO.findById(saved.getId()).orElseThrow();
        assertEquals(2850.0, reloaded.getPartsCost(), 0.001);
        assertEquals(3350.0, reloaded.getTotalCost(), 0.001); // 500 labour + 2850 parts
    }
}
