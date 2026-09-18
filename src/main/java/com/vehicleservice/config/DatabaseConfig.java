package com.vehicleservice.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseConfig manages the SQLite database connection, initialization,
 * schema migration, and default seed data.
 */
public class DatabaseConfig {

    private static final String DEFAULT_DB_NAME = "vehicleservice.db";
    private static String databaseUrl = "jdbc:sqlite:" + DEFAULT_DB_NAME;
    private static final double DEFAULT_TAX_RATE = 0.18; // 18% GST standard

    private DatabaseConfig() {
        // Utility class, private constructor
    }

    public static synchronized void setDatabaseUrl(String url) {
        databaseUrl = url;
    }

    public static synchronized String getDatabaseUrl() {
        return databaseUrl;
    }

    public static double getTaxRate() {
        return DEFAULT_TAX_RATE;
    }

    /**
     * Obtains a connection to the SQLite database with foreign keys enabled.
     *
     * @return active Connection instance
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(databaseUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Initializes the database by executing the schema SQL script if tables are missing
     * and seeding default administrative user.
     */
    public static synchronized void initializeDatabase() {
        try (Connection conn = getConnection()) {
            String schemaSql = loadSchemaSql();
            if (schemaSql != null && !schemaSql.trim().isEmpty()) {
                String[] statements = schemaSql.split(";");
                try (Statement stmt = conn.createStatement()) {
                    for (String sql : statements) {
                        String trimmed = sql.trim();
                        if (!trimmed.isEmpty()) {
                            stmt.execute(trimmed);
                        }
                    }
                }
            }
            seedDefaultUser(conn);
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Seeds a default administrator user if users table is empty.
     */
    private static void seedDefaultUser(Connection conn) {
        String countSql = "SELECT COUNT(*) FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, hashPassword("admin123"));
                    pstmt.setString(3, "System Administrator");
                    pstmt.setString(4, "ADMIN");
                    pstmt.executeUpdate();
                    System.out.println("Default admin user created (username: admin, password: admin123)");
                }
            }
        } catch (SQLException e) {
            System.err.println("Warning: Could not seed default admin user: " + e.getMessage());
        }
    }

    /**
     * Hashes a password using SHA-256.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Loads schema SQL from resource or disk fallback.
     */
    private static String loadSchemaSql() {
        StringBuilder sb = new StringBuilder();
        // 1. Try classpath resource
        try (InputStream is = DatabaseConfig.class.getResourceAsStream("/database/schema.sql")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    return sb.toString();
                }
            }
        } catch (Exception ignored) {
        }

        // 2. Try relative file paths
        String[] fallbackPaths = {
            "database/schema.sql",
            "src/main/resources/database/schema.sql",
            "../database/schema.sql"
        };

        for (String p : fallbackPaths) {
            Path path = Paths.get(p);
            if (Files.exists(path)) {
                try {
                    return Files.readString(path, StandardCharsets.UTF_8);
                } catch (Exception ignored) {
                }
            }
        }

        // 3. Fallback inline schema if files are unavailable
        return """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'STAFF',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            CREATE TABLE IF NOT EXISTS customers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT NOT NULL,
                email TEXT,
                address TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            CREATE TABLE IF NOT EXISTS vehicles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id INTEGER NOT NULL,
                registration_number TEXT UNIQUE NOT NULL,
                brand TEXT NOT NULL,
                model TEXT NOT NULL,
                vehicle_type TEXT NOT NULL,
                manufacturing_year INTEGER NOT NULL,
                FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
            );
            CREATE TABLE IF NOT EXISTS service_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                vehicle_id INTEGER NOT NULL,
                service_type TEXT NOT NULL,
                description TEXT,
                service_date TEXT NOT NULL,
                status TEXT NOT NULL CHECK(status IN ('Pending', 'In Progress', 'Completed', 'Cancelled')) DEFAULT 'Pending',
                labour_cost REAL NOT NULL DEFAULT 0.0,
                parts_cost REAL NOT NULL DEFAULT 0.0,
                FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
            );
            CREATE TABLE IF NOT EXISTS service_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                service_id INTEGER NOT NULL,
                item_name TEXT NOT NULL,
                quantity INTEGER NOT NULL DEFAULT 1,
                unit_price REAL NOT NULL DEFAULT 0.0,
                total_price REAL NOT NULL DEFAULT 0.0,
                FOREIGN KEY (service_id) REFERENCES service_records(id) ON DELETE CASCADE
            );
            CREATE TABLE IF NOT EXISTS bills (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                service_id INTEGER UNIQUE NOT NULL,
                subtotal REAL NOT NULL DEFAULT 0.0,
                tax REAL NOT NULL DEFAULT 0.0,
                total_amount REAL NOT NULL DEFAULT 0.0,
                payment_status TEXT NOT NULL CHECK(payment_status IN ('Unpaid', 'Paid')) DEFAULT 'Unpaid',
                bill_date TEXT NOT NULL,
                FOREIGN KEY (service_id) REFERENCES service_records(id) ON DELETE CASCADE
            );
            """;
    }
}
