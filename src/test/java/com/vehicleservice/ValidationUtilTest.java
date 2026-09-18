package com.vehicleservice;

import com.vehicleservice.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation Utility Tests")
public class ValidationUtilTest {

    @Test
    @DisplayName("Should accept valid email formats")
    void testValidEmails() {
        assertTrue(ValidationUtil.isValidEmail("test@example.com"));
        assertTrue(ValidationUtil.isValidEmail("john.doe@sub.domain.org"));
        assertTrue(ValidationUtil.isValidEmail("")); // Optional
        assertTrue(ValidationUtil.isValidEmail(null)); // Optional
    }

    @Test
    @DisplayName("Should reject invalid email formats")
    void testInvalidEmails() {
        assertFalse(ValidationUtil.isValidEmail("plainaddress"));
        assertFalse(ValidationUtil.isValidEmail("@missingusername.com"));
        assertFalse(ValidationUtil.isValidEmail("user@.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"9876543210", "+919876543210", "0123456789", "98765-43210"})
    @DisplayName("Should validate correct phone numbers")
    void testValidPhoneNumbers(String phone) {
        assertTrue(ValidationUtil.isValidPhone(phone));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "abc1234567", "12345"})
    @DisplayName("Should reject invalid phone numbers")
    void testInvalidPhoneNumbers(String phone) {
        assertFalse(ValidationUtil.isValidPhone(phone));
    }

    @Test
    @DisplayName("Should validate vehicle manufacturing years")
    void testManufacturingYearValidation() {
        int current = Year.now().getValue();
        assertTrue(ValidationUtil.isValidManufacturingYear(2020));
        assertTrue(ValidationUtil.isValidManufacturingYear(current));
        assertTrue(ValidationUtil.isValidManufacturingYear(1950));

        assertFalse(ValidationUtil.isValidManufacturingYear(1899));
        assertFalse(ValidationUtil.isValidManufacturingYear(current + 5));
    }

    @Test
    @DisplayName("Should validate customer creation constraints")
    void testValidateCustomer() {
        assertDoesNotThrow(() -> ValidationUtil.validateCustomer("Alice Smith", "9876543210", "alice@example.com"));

        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateCustomer("", "9876543210", "alice@example.com"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateCustomer("Alice", "invalid-phone", "alice@example.com"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateCustomer("Alice", "9876543210", "bad-email"));
    }

    @Test
    @DisplayName("Should validate vehicle registration constraints")
    void testValidateVehicle() {
        assertDoesNotThrow(() -> ValidationUtil.validateVehicle(1, "KA01AB1234", "Honda", "City", 2022));

        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateVehicle(0, "KA01AB1234", "Honda", "City", 2022));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateVehicle(1, "", "Honda", "City", 2022));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateVehicle(1, "KA01AB1234", "", "City", 2022));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateVehicle(1, "KA01AB1234", "Honda", "", 2022));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateVehicle(1, "KA01AB1234", "Honda", "City", 1850));
    }
}
