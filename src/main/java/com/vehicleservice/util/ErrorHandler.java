package com.vehicleservice.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Standardized error handling and user feedback utility.
 * Safe for both JavaFX GUI runtime and headless test environments.
 */
public class ErrorHandler {

    private ErrorHandler() {
    }

    /**
     * Translates raw database/SQL exceptions into clean, user-friendly messages.
     */
    public static String getFriendlyDatabaseErrorMessage(Exception e) {
        if (e == null) {
            return "An unknown error occurred.";
        }
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("unique constraint failed") || msg.contains("unique")) {
            if (msg.contains("registration_number")) {
                return "A vehicle with this registration number already exists in the system.";
            }
            if (msg.contains("username")) {
                return "This username is already taken. Please choose another.";
            }
            if (msg.contains("service_id")) {
                return "A bill has already been generated for this service record.";
            }
            return "A duplicate record with the same unique identifier already exists.";
        } else if (msg.contains("foreign key constraint failed")) {
            return "Cannot complete operation: the record references non-existent data or is currently in use.";
        } else if (msg.contains("check constraint failed")) {
            return "Invalid value provided for one of the restricted fields.";
        }
        return "Database operation failed: " + e.getMessage();
    }

    /**
     * Shows an error alert or logs to stderr if headless.
     */
    public static void showError(String title, String message) {
        System.err.println("[ERROR] " + title + ": " + message);
        runOnFxThread(() -> {
            try {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Throwable ignored) {
                // Headless or toolkit uninitialized
            }
        });
    }

    /**
     * Shows an informational alert.
     */
    public static void showInfo(String title, String message) {
        System.out.println("[INFO] " + title + ": " + message);
        runOnFxThread(() -> {
            try {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Throwable ignored) {
            }
        });
    }

    /**
     * Shows a warning alert.
     */
    public static void showWarning(String title, String message) {
        System.out.println("[WARNING] " + title + ": " + message);
        runOnFxThread(() -> {
            try {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Throwable ignored) {
            }
        });
    }

    private static void runOnFxThread(Runnable action) {
        try {
            if (Platform.isFxApplicationThread()) {
                action.run();
            } else {
                Platform.runLater(action);
            }
        } catch (Throwable ignored) {
            // JavaFX toolkit might not be initialized in headless test runs
        }
    }
}
