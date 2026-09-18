package com.vehicleservice.dao;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Customer entities.
 * Utilizes parameterized JDBC PreparedStatements to guarantee safety against SQL injection.
 */
public class CustomerDAO {

    public Customer insert(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        customer.setId(rs.getInt(1));
                    }
                }
            }
            return customer;
        }
    }

    public boolean update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET name = ?, phone = ?, email = ?, address = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            pstmt.setInt(5, customer.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Optional<Customer> findById(int id) throws SQLException {
        String sql = "SELECT id, name, phone, email, address, created_at FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT id, name, phone, email, address, created_at FROM customers ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
        }
        return list;
    }

    public List<Customer> search(String keyword) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT id, name, phone, email, address, created_at FROM customers " +
                     "WHERE name LIKE ? OR phone LIKE ? OR email LIKE ? ORDER BY name ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String term = "%" + keyword + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            pstmt.setString(3, term);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCustomer(rs));
                }
            }
        }
        return list;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getString("created_at")
        );
    }
}
