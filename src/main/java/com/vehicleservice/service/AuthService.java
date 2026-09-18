package com.vehicleservice.service;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.dao.UserDAO;
import com.vehicleservice.model.User;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service managing user authentication and active staff sessions.
 */
public class AuthService {

    private static User currentUser;
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public Optional<User> login(String username, String rawPassword) throws SQLException {
        if (username == null || rawPassword == null) {
            return Optional.empty();
        }
        String hash = DatabaseConfig.hashPassword(rawPassword);
        Optional<User> userOpt = userDAO.authenticate(username, hash);
        if (userOpt.isPresent()) {
            currentUser = userOpt.get();
        }
        return userOpt;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
