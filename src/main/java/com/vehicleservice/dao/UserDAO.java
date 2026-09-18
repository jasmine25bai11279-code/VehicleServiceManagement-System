package com.vehicleservice.dao;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Data Access Object for User/Staff authentication.
 */
public class UserDAO {

    public User insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername().toLowerCase().trim());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole() != null ? user.getRole() : "STAFF");

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }
            return user;
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, full_name, role, created_at FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> authenticate(String username, String passwordHash) throws SQLException {
        String sql = "SELECT id, username, password_hash, full_name, role, created_at FROM users WHERE LOWER(username) = LOWER(?) AND password_hash = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            pstmt.setString(2, passwordHash);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getString("created_at")
        );
    }
}
