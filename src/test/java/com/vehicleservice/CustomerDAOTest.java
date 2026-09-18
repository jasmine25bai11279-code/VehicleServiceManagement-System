package com.vehicleservice;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.dao.CustomerDAO;
import com.vehicleservice.model.Customer;
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

@DisplayName("Customer DAO CRUD & Search Tests")
public class CustomerDAOTest {

    private static final String TEST_DB = "test_customer.db";
    private CustomerDAO customerDAO;

    @BeforeAll
    static void initDb() {
        new File(TEST_DB).delete();
        DatabaseConfig.setDatabaseUrl("jdbc:sqlite:" + TEST_DB);
        DatabaseConfig.initializeDatabase();
    }

    @BeforeEach
    void cleanTables() throws SQLException {
        customerDAO = new CustomerDAO();
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
    @DisplayName("Should insert and find customer by ID")
    void testInsertAndFindById() throws SQLException {
        Customer c = new Customer("Rohan Gupta", "9876543210", "rohan@example.com", "Indiranagar, Bangalore");
        Customer saved = customerDAO.insert(c);

        assertTrue(saved.getId() > 0, "Customer ID should be generated");
        Optional<Customer> found = customerDAO.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Rohan Gupta", found.get().getName());
        assertEquals("9876543210", found.get().getPhone());
    }

    @Test
    @DisplayName("Should update existing customer")
    void testUpdateCustomer() throws SQLException {
        Customer c = new Customer("Vikram Rao", "9812345678", "vikram@example.com", "Koramangala");
        Customer saved = customerDAO.insert(c);

        saved.setName("Vikram M. Rao");
        saved.setPhone("9998887776");
        boolean updated = customerDAO.update(saved);

        assertTrue(updated);
        Customer reloaded = customerDAO.findById(saved.getId()).orElseThrow();
        assertEquals("Vikram M. Rao", reloaded.getName());
        assertEquals("9998887776", reloaded.getPhone());
    }

    @Test
    @DisplayName("Should delete customer")
    void testDeleteCustomer() throws SQLException {
        Customer c = new Customer("Anita Sen", "9123456780", "anita@example.com", "Jayanagar");
        Customer saved = customerDAO.insert(c);

        boolean deleted = customerDAO.delete(saved.getId());
        assertTrue(deleted);
        assertTrue(customerDAO.findById(saved.getId()).isEmpty());
    }

    @Test
    @DisplayName("Should search customer by name or phone")
    void testSearchCustomer() throws SQLException {
        customerDAO.insert(new Customer("Deepak Verma", "9870001111", "deepak@test.com", "Whitefield"));
        customerDAO.insert(new Customer("Suresh Raina", "9870002222", "suresh@test.com", "MG Road"));

        List<Customer> results = customerDAO.search("Verma");
        assertEquals(1, results.size());
        assertEquals("Deepak Verma", results.get(0).getName());

        List<Customer> phoneResults = customerDAO.search("0002222");
        assertEquals(1, phoneResults.size());
        assertEquals("Suresh Raina", phoneResults.get(0).getName());
    }
}
