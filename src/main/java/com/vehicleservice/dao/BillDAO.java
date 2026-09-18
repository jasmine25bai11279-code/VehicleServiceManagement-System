package com.vehicleservice.dao;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Bill entities.
 */
public class BillDAO {

    public Bill insert(Bill bill) throws SQLException {
        String sql = "INSERT INTO bills (service_id, subtotal, tax, total_amount, payment_status, bill_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, bill.getServiceId());
            pstmt.setDouble(2, bill.getSubtotal());
            pstmt.setDouble(3, bill.getTax());
            pstmt.setDouble(4, bill.getTotalAmount());
            pstmt.setString(5, bill.getPaymentStatus() != null ? bill.getPaymentStatus() : "Unpaid");
            pstmt.setString(6, bill.getBillDate());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        bill.setId(rs.getInt(1));
                    }
                }
            }
            return bill;
        }
    }

    public boolean updatePaymentStatus(int billId, String paymentStatus) throws SQLException {
        String sql = "UPDATE bills SET payment_status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, paymentStatus);
            pstmt.setInt(2, billId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Optional<Bill> findById(int id) throws SQLException {
        String sql = "SELECT b.id, b.service_id, b.subtotal, b.tax, b.total_amount, b.payment_status, b.bill_date, " +
                     "c.name AS customer_name, v.registration_number, sr.service_type " +
                     "FROM bills b " +
                     "JOIN service_records sr ON b.service_id = sr.id " +
                     "JOIN vehicles v ON sr.vehicle_id = v.id " +
                     "JOIN customers c ON v.customer_id = c.id " +
                     "WHERE b.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Bill> findByServiceId(int serviceId) throws SQLException {
        String sql = "SELECT b.id, b.service_id, b.subtotal, b.tax, b.total_amount, b.payment_status, b.bill_date, " +
                     "c.name AS customer_name, v.registration_number, sr.service_type " +
                     "FROM bills b " +
                     "JOIN service_records sr ON b.service_id = sr.id " +
                     "JOIN vehicles v ON sr.vehicle_id = v.id " +
                     "JOIN customers c ON v.customer_id = c.id " +
                     "WHERE b.service_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, serviceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBill(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Bill> findAll() throws SQLException {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.id, b.service_id, b.subtotal, b.tax, b.total_amount, b.payment_status, b.bill_date, " +
                     "c.name AS customer_name, v.registration_number, sr.service_type " +
                     "FROM bills b " +
                     "JOIN service_records sr ON b.service_id = sr.id " +
                     "JOIN vehicles v ON sr.vehicle_id = v.id " +
                     "JOIN customers c ON v.customer_id = c.id " +
                     "ORDER BY b.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToBill(rs));
            }
        }
        return list;
    }

    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0.0) FROM bills WHERE payment_status = 'Paid'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double getPendingRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0.0) FROM bills WHERE payment_status = 'Unpaid'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bills";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countByPaymentStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bills WHERE payment_status = ?";
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

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill b = new Bill(
                rs.getInt("id"),
                rs.getInt("service_id"),
                rs.getDouble("subtotal"),
                rs.getDouble("tax"),
                rs.getDouble("total_amount"),
                rs.getString("payment_status"),
                rs.getString("bill_date")
        );
        b.setCustomerName(rs.getString("customer_name"));
        b.setVehicleRegNumber(rs.getString("registration_number"));
        b.setServiceType(rs.getString("service_type"));
        return b;
    }
}
