package com.vehicleservice.dao;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.model.ServiceItem;
import com.vehicleservice.model.ServiceRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Service Records and Service Items.
 */
public class ServiceDAO {

    public ServiceRecord insert(ServiceRecord sr) throws SQLException {
        String sql = "INSERT INTO service_records (vehicle_id, service_type, description, service_date, status, labour_cost, parts_cost) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, sr.getVehicleId());
            pstmt.setString(2, sr.getServiceType());
            pstmt.setString(3, sr.getDescription());
            pstmt.setString(4, sr.getServiceDate());
            pstmt.setString(5, sr.getStatus() != null ? sr.getStatus() : "Pending");
            pstmt.setDouble(6, sr.getLabourCost());
            pstmt.setDouble(7, sr.getPartsCost());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        sr.setId(rs.getInt(1));
                    }
                }
            }
            return sr;
        }
    }

    public boolean update(ServiceRecord sr) throws SQLException {
        String sql = "UPDATE service_records SET vehicle_id = ?, service_type = ?, description = ?, " +
                     "service_date = ?, status = ?, labour_cost = ?, parts_cost = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sr.getVehicleId());
            pstmt.setString(2, sr.getServiceType());
            pstmt.setString(3, sr.getDescription());
            pstmt.setString(4, sr.getServiceDate());
            pstmt.setString(5, sr.getStatus());
            pstmt.setDouble(6, sr.getLabourCost());
            pstmt.setDouble(7, sr.getPartsCost());
            pstmt.setInt(8, sr.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int serviceId, String newStatus) throws SQLException {
        String sql = "UPDATE service_records SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, serviceId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM service_records WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Optional<ServiceRecord> findById(int id) throws SQLException {
        String sql = "SELECT sr.id, sr.vehicle_id, sr.service_type, sr.description, sr.service_date, " +
                     "sr.status, sr.labour_cost, sr.parts_cost, v.registration_number, c.name AS customer_name, " +
                     "v.brand || ' ' || v.model AS vehicle_info " +
                     "FROM service_records sr " +
                     "JOIN vehicles v ON sr.vehicle_id = v.id " +
                     "JOIN customers c ON v.customer_id = c.id " +
                     "WHERE sr.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ServiceRecord record = mapResultSetToServiceRecord(rs);
                    record.setItems(getItemsByServiceId(record.getId()));
                    return Optional.of(record);
                }
            }
        }
        return Optional.empty();
    }

    public List<ServiceRecord> findAll() throws SQLException {
        List<ServiceRecord> list = new ArrayList<>();
        String sql = "SELECT sr.id, sr.vehicle_id, sr.service_type, sr.description, sr.service_date, " +
                     "sr.status, sr.labour_cost, sr.parts_cost, v.registration_number, c.name AS customer_name, " +
                     "v.brand || ' ' || v.model AS vehicle_info " +
                     "FROM service_records sr " +
                     "JOIN vehicles v ON sr.vehicle_id = v.id " +
                     "JOIN customers c ON v.customer_id = c.id " +
                     "ORDER BY sr.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToServiceRecord(rs));
            }
        }
        return list;
    }

    public List<ServiceRecord> findByStatus(String status) throws SQLException {
        List<ServiceRecord> list = new ArrayList<>();
        String sql = "SELECT sr.id, sr.vehicle_id, sr.service_type, sr.description, sr.service_date, " +
                     "sr.status, sr.labour_cost, sr.parts_cost, v.registration_number, c.name AS customer_name, " +
                     "v.brand || ' ' || v.model AS vehicle_info " +
                     "FROM service_records sr " +
                     "JOIN vehicles v ON sr.vehicle_id = v.id " +
                     "JOIN customers c ON v.customer_id = c.id " +
                     "WHERE sr.status = ? ORDER BY sr.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToServiceRecord(rs));
                }
            }
        }
        return list;
    }

    public List<ServiceRecord> search(String keyword) throws SQLException {
        List<ServiceRecord> list = new ArrayList<>();
        String sql = "SELECT sr.id, sr.vehicle_id, sr.service_type, sr.description, sr.service_date, " +
                     "sr.status, sr.labour_cost, sr.parts_cost, v.registration_number, c.name AS customer_name, " +
                     "v.brand || ' ' || v.model AS vehicle_info " +
                     "FROM service_records sr " +
                     "JOIN vehicles v ON sr.vehicle_id = v.id " +
                     "JOIN customers c ON v.customer_id = c.id " +
                     "WHERE v.registration_number LIKE ? OR c.name LIKE ? OR sr.service_type LIKE ? OR sr.status LIKE ? " +
                     "ORDER BY sr.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String term = "%" + keyword + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            pstmt.setString(3, term);
            pstmt.setString(4, term);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToServiceRecord(rs));
                }
            }
        }
        return list;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM service_records";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM service_records WHERE status = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // --- Service Items Operations ---

    public ServiceItem insertItem(ServiceItem item) throws SQLException {
        String sql = "INSERT INTO service_items (service_id, item_name, quantity, unit_price, total_price) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            double total = item.getQuantity() * item.getUnitPrice();
            pstmt.setInt(1, item.getServiceId());
            pstmt.setString(2, item.getItemName());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setDouble(4, item.getUnitPrice());
            pstmt.setDouble(5, total);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setId(rs.getInt(1));
                    }
                }
            }
            item.setTotalPrice(total);
            recalculatePartsCost(item.getServiceId());
            return item;
        }
    }

    public boolean deleteItem(int itemId, int serviceId) throws SQLException {
        String sql = "DELETE FROM service_items WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            boolean deleted = pstmt.executeUpdate() > 0;
            if (deleted) {
                recalculatePartsCost(serviceId);
            }
            return deleted;
        }
    }

    public List<ServiceItem> getItemsByServiceId(int serviceId) throws SQLException {
        List<ServiceItem> list = new ArrayList<>();
        String sql = "SELECT id, service_id, item_name, quantity, unit_price, total_price FROM service_items WHERE service_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, serviceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ServiceItem(
                            rs.getInt("id"),
                            rs.getInt("service_id"),
                            rs.getString("item_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("total_price")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Recalculates sum of all service_items for the service and updates parts_cost in service_records.
     */
    public void recalculatePartsCost(int serviceId) throws SQLException {
        String sumSql = "SELECT COALESCE(SUM(total_price), 0.0) FROM service_items WHERE service_id = ?";
        String updateSql = "UPDATE service_records SET parts_cost = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement sumStmt = conn.prepareStatement(sumSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            sumStmt.setInt(1, serviceId);
            double sum = 0.0;
            try (ResultSet rs = sumStmt.executeQuery()) {
                if (rs.next()) {
                    sum = rs.getDouble(1);
                }
            }

            updateStmt.setDouble(1, sum);
            updateStmt.setInt(2, serviceId);
            updateStmt.executeUpdate();
        }
    }

    private ServiceRecord mapResultSetToServiceRecord(ResultSet rs) throws SQLException {
        ServiceRecord sr = new ServiceRecord(
                rs.getInt("id"),
                rs.getInt("vehicle_id"),
                rs.getString("service_type"),
                rs.getString("description"),
                rs.getString("service_date"),
                rs.getString("status"),
                rs.getDouble("labour_cost"),
                rs.getDouble("parts_cost")
        );
        sr.setVehicleRegNumber(rs.getString("registration_number"));
        sr.setCustomerName(rs.getString("customer_name"));
        sr.setVehicleInfo(rs.getString("vehicle_info"));
        return sr;
    }
}
