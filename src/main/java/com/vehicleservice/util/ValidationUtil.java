package com.vehicleservice.util;

import java.time.Year;
import java.util.regex.Pattern;

/**
 * Utility methods for input validation across the application.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    // Matches 10 digits or standard formatted phone numbers like +1-1234567890 or 9876543210
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+?\\d{1,3}[- ]?)?\\d{10}$"
    );

    // Registration number format: typically alphanumeric, 4 to 15 chars, e.g. KA01AB1234, DL-3C-1234
    private static final Pattern REG_NUMBER_PATTERN = Pattern.compile(
            "^[A-Z0-9- ]{4,15}$", Pattern.CASE_INSENSITIVE
    );

    private ValidationUtil() {
    }

    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email is optional for customers
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        return cleanPhone.matches("^\\+?\\d{10,13}$");
    }

    public static boolean isValidRegistrationNumber(String regNo) {
        if (!isNotEmpty(regNo)) {
            return false;
        }
        return REG_NUMBER_PATTERN.matcher(regNo.trim()).matches();
    }

    public static boolean isValidManufacturingYear(int year) {
        int currentYear = Year.now().getValue();
        return year >= 1900 && year <= (currentYear + 1);
    }

    public static boolean isNonNegative(double value) {
        return value >= 0.0;
    }

    public static boolean isPositive(int value) {
        return value > 0;
    }

    /**
     * Validates customer fields.
     *
     * @param name customer name
     * @param phone customer phone
     * @param email customer email
     * @throws IllegalArgumentException if any field is invalid
     */
    public static void validateCustomer(String name, String phone, String email) {
        if (!isNotEmpty(name)) {
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone number. Must be 10 to 13 digits.");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format (e.g. name@example.com).");
        }
    }

    /**
     * Validates vehicle fields.
     */
    public static void validateVehicle(int customerId, String regNo, String brand, String model, int year) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Vehicle must be linked to a valid customer.");
        }
        if (!isValidRegistrationNumber(regNo)) {
            throw new IllegalArgumentException("Invalid vehicle registration number.");
        }
        if (!isNotEmpty(brand)) {
            throw new IllegalArgumentException("Vehicle brand cannot be empty.");
        }
        if (!isNotEmpty(model)) {
            throw new IllegalArgumentException("Vehicle model cannot be empty.");
        }
        if (!isValidManufacturingYear(year)) {
            throw new IllegalArgumentException("Manufacturing year must be between 1900 and " + (Year.now().getValue() + 1) + ".");
        }
    }

    /**
     * Validates service record costs and vehicle linkage.
     */
    public static void validateServiceRecord(int vehicleId, String serviceType, double labourCost) {
        if (vehicleId <= 0) {
            throw new IllegalArgumentException("Service must reference an existing vehicle.");
        }
        if (!isNotEmpty(serviceType)) {
            throw new IllegalArgumentException("Service type cannot be empty.");
        }
        if (!isNonNegative(labourCost)) {
            throw new IllegalArgumentException("Labour cost cannot be negative.");
        }
    }

    /**
     * Validates service item.
     */
    public static void validateServiceItem(int serviceId, String itemName, int quantity, double unitPrice) {
        if (serviceId <= 0) {
            throw new IllegalArgumentException("Service item must reference a valid service record.");
        }
        if (!isNotEmpty(itemName)) {
            throw new IllegalArgumentException("Item name cannot be empty.");
        }
        if (!isPositive(quantity)) {
            throw new IllegalArgumentException("Item quantity must be at least 1.");
        }
        if (!isNonNegative(unitPrice)) {
            throw new IllegalArgumentException("Unit price cannot be negative.");
        }
    }
}
