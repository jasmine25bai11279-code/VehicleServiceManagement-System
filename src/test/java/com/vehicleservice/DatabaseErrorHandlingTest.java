package com.vehicleservice;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.dao.VehicleDAO;
import com.vehicleservice.model.Vehicle;
import com.vehicleservice.util.ErrorHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Database Error Handling & Constraints Tests")
public class DatabaseErrorHandlingTest {

    private static final String TEST_DB = "test_errors.db";

    @BeforeAll
    static void initDb() {
        new File(TEST_DB).delete();
        DatabaseConfig.setDatabaseUrl("jdbc:sqlite:" + TEST_DB);
        DatabaseConfig.initializeDatabase();
    }

    @Test
    @DisplayName("Should enforce foreign key constraint on vehicle creation")
    void testForeignKeyConstraint() {
        VehicleDAO vehicleDAO = new VehicleDAO();
        // Customer ID 999999 does not exist
        Vehicle v = new Vehicle(999999, "XX01YY9999", "TestBrand", "TestModel", "Sedan", 2020);

        SQLException ex = assertThrows(SQLException.class, () -> vehicleDAO.insert(v));
        String friendly = ErrorHandler.getFriendlyDatabaseErrorMessage(ex);
        assertTrue(friendly.contains("references non-existent data") || friendly.contains("Foreign key"),
                "Friendly message should clarify foreign key failure: " + friendly);
    }

    @Test
    @DisplayName("Should enforce check constraint on invalid status")
    void testCheckConstraint() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // Attempting to insert status not in CHECK('Pending', 'In Progress', 'Completed', 'Cancelled')
            SQLException ex = assertThrows(SQLException.class, () ->
                stmt.execute("INSERT INTO service_records (vehicle_id, service_type, service_date, status) VALUES (1, 'Test', '2026-09-18', 'InvalidStatus');")
            );

            String friendly = ErrorHandler.getFriendlyDatabaseErrorMessage(ex);
            assertNotNull(friendly);
            assertFalse(friendly.isEmpty());
        }
    }
}
