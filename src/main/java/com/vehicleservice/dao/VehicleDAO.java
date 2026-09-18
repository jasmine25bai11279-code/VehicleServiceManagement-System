package com.vehicleservice.dao;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Vehicle entities.
 */
public class VehicleDAO {

    public Vehicle insert(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles (customer_id, registration_number, brand, model, vehicle_type, manufacturing_year) " +
                     "VALUES (?, UPPER(?), ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, vehicle.getCustomerId());
            pstmt.setString(2, vehicle.getRegistrationNumber().trim().toUpperCase());
            pstmt.setString(3, vehicle.getBrand().trim());
            pstmt.setString(4, vehicle.getModel().trim());
            pstmt.setString(5, vehicle.getVehicleType());
            pstmt.setInt(6, vehicle.getManufacturingYear());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        vehicle.setId(rs.getInt(1));
                    }
                }
            }
            return vehicle;
        }
    }

    public boolean update(Vehicle vehicle) throws SQLException {
        String sql = "UPDATE vehicles SET customer_id = ?, registration_number = UPPER(?), brand = ?, " +
                     "model = ?, vehicle_type = ?, manufacturing_year = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vehicle.getCustomerId());
            pstmt.setString(2, vehicle.getRegistrationNumber().trim().toUpperCase());
            pstmt.setString(3, vehicle.getBrand().trim());
            pstmt.setString(4, vehicle.getModel().trim());
            pstmt.setString(5, vehicle.getVehicleType());
            pstmt.setInt(6, vehicle.getManufacturingYear());
            pstmt.setInt(7, vehicle.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Optional<Vehicle> findById(int id) throws SQLException {
        String sql = "SELECT v.id, v.customer_id, v.registration_number, v.brand, v.model, v.vehicle_type, " +
                     "v.manufacturing_year, c.name AS customer_name " +
                     "FROM vehicles v " +
                     "JOIN customers c ON v.customer_id = c.id " +
                     "WHERE v.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVehicle(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Vehicle> findByRegistrationNumber(String regNo) throws SQLException {
        String sql = "SELECT v.id, v.customer_id, v.registration_number, v.brand, v.model, v.vehicle_type, " +
                     "v.manufacturing_year, c.name AS customer_name " +
                     "FROM vehicles v " +
                     "JOIN customers c ON v.customer_id = c.id " +
                     "WHERE UPPER(v.registration_number) = UPPER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, regNo.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVehicle(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean isRegistrationUnique(String regNo, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicles WHERE UPPER(registration_number) = UPPER(?) AND id != ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, regNo.trim());
            pstmt.setInt(2, excludeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return true;
    }

    public List<Vehicle> findByCustomerId(int customerId) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.id, v.customer_id, v.registration_number, v.brand, v.model, v.vehicle_type, " +
                     "v.manufacturing_year, c.name AS customer_name " +
                     "FROM vehicles v " +
                     "JOIN customers c ON v.customer_id = c.id " +
                     "WHERE v.customer_id = ? ORDER BY v.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToVehicle(rs));
                }
            }
        }
        return list;
    }

    public List<Vehicle> findAll() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.id, v.customer_id, v.registration_number, v.brand, v.model, v.vehicle_type, " +
                     "v.manufacturing_year, c.name AS customer_name " +
                     "FROM vehicles v " +
                     "JOIN customers c ON v.customer_id = c.id " +
                     "ORDER BY v.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToVehicle(rs));
            }
        }
        return list;
    }

    public List<Vehicle> search(String keyword) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.id, v.customer_id, v.registration_number, v.brand, v.model, v.vehicle_type, " +
                     "v.manufacturing_year, c.name AS customer_name " +
                     "FROM vehicles v " +
                     "JOIN customers c ON v.customer_id = c.id " +
                     "WHERE v.registration_number LIKE ? OR v.brand LIKE ? OR v.model LIKE ? OR c.name LIKE ? " +
                     "ORDER BY v.brand ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String term = "%" + keyword + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            pstmt.setString(3, term);
            pstmt.setString(4, term);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToVehicle(rs));
                }
            }
        }
        return list;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicles";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle(
                rs.getInt("id"),
                rs.getInt("customer_id"),
                rs.getString("registration_number"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getString("vehicle_type"),
                rs.getInt("manufacturing_year")
        );
        v.setCustomerName(rs.getString("customer_name"));
        return v;
    }
}
