package com.vehicleservice;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.dao.CustomerDAO;
import com.vehicleservice.dao.VehicleDAO;
import com.vehicleservice.model.Customer;
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

@DisplayName("Vehicle DAO CRUD & Unique Registration Tests")
public class VehicleDAOTest {

    private static final String TEST_DB = "test_vehicle.db";
    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private int testCustomerId;

    @BeforeAll
    static void initDb() {
        new File(TEST_DB).delete();
        DatabaseConfig.setDatabaseUrl("jdbc:sqlite:" + TEST_DB);
        DatabaseConfig.initializeDatabase();
    }

    @BeforeEach
    void setupData() throws SQLException {
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM bills;");
            stmt.execute("DELETE FROM service_items;");
            stmt.execute("DELETE FROM service_records;");
            stmt.execute("DELETE FROM vehicles;");
            stmt.execute("DELETE FROM customers;");
        }

        Customer cust = customerDAO.insert(new Customer("Kunal Kapoor", "9988776655", "kunal@example.com", "Pune"));
        testCustomerId = cust.getId();
    }

    @Test
    @DisplayName("Should insert vehicle and find by ID and Reg Number")
    void testInsertAndFind() throws SQLException {
        Vehicle v = new Vehicle(testCustomerId, "MH12AB1234", "Tata", "Nexon", "SUV", 2023);
        Vehicle saved = vehicleDAO.insert(v);

        assertTrue(saved.getId() > 0);
        Optional<Vehicle> byId = vehicleDAO.findById(saved.getId());
        assertTrue(byId.isPresent());
        assertEquals("MH12AB1234", byId.get().getRegistrationNumber());
        assertEquals("Tata", byId.get().getBrand());

        Optional<Vehicle> byReg = vehicleDAO.findByRegistrationNumber("mh12ab1234");
        assertTrue(byReg.isPresent());
        assertEquals(saved.getId(), byReg.get().getId());
    }

    @Test
    @DisplayName("Should prevent duplicate vehicle registration number")
    void testUniqueRegistrationNumber() throws SQLException {
        Vehicle v1 = new Vehicle(testCustomerId, "KA03CD9999", "Hyundai", "i20", "Hatchback", 2021);
        vehicleDAO.insert(v1);

        assertFalse(vehicleDAO.isRegistrationUnique("KA03CD9999", 0));
        assertTrue(vehicleDAO.isRegistrationUnique("KA03CD9999", v1.getId()), "Should be unique when excluding itself");

        Vehicle v2 = new Vehicle(testCustomerId, "KA03CD9999", "Honda", "Jazz", "Hatchback", 2020);
        assertThrows(SQLException.class, () -> vehicleDAO.insert(v2), "Inserting duplicate registration number must throw SQLException");
    }

    @Test
    @DisplayName("Should retrieve vehicles belonging to a specific customer")
    void testFindByCustomerId() throws SQLException {
        vehicleDAO.insert(new Vehicle(testCustomerId, "KA01M1111", "Maruti", "Swift", "Hatchback", 2019));
        vehicleDAO.insert(new Vehicle(testCustomerId, "KA01M2222", "Honda", "City", "Sedan", 2022));

        List<Vehicle> list = vehicleDAO.findByCustomerId(testCustomerId);
        assertEquals(2, list.size());
    }
}
