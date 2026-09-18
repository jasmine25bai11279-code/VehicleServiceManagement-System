package com.vehicleservice;

import com.vehicleservice.model.ServiceItem;
import com.vehicleservice.service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Billing Calculation Logic Tests")
public class BillingServiceTest {

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingService(null, null, 0.18);
    }

    @Test
    @DisplayName("Should correctly calculate sum of parts cost")
    void testCalculatePartsCost() {
        List<ServiceItem> items = new ArrayList<>();
        items.add(new ServiceItem(1, "Engine Oil", 2, 800.0)); // 1600
        items.add(new ServiceItem(1, "Oil Filter", 1, 350.0));  // 350
        items.add(new ServiceItem(1, "Spark Plug", 4, 150.0));  // 600

        double partsCost = billingService.calculatePartsCost(items);
        assertEquals(2550.0, partsCost, 0.001, "Parts cost must equal sum of item quantities * unit price");
    }

    @Test
    @DisplayName("Should return 0 parts cost for empty list")
    void testCalculatePartsCostEmpty() {
        assertEquals(0.0, billingService.calculatePartsCost(new ArrayList<>()));
        assertEquals(0.0, billingService.calculatePartsCost(null));
    }

    @Test
    @DisplayName("Should calculate subtotal = parts + labour")
    void testCalculateSubtotal() {
        double subtotal = billingService.calculateSubtotal(2550.0, 450.0);
        assertEquals(3000.0, subtotal, 0.001);
    }

    @Test
    @DisplayName("Should calculate tax = subtotal * tax rate (18%)")
    void testCalculateTax() {
        double subtotal = 3000.0;
        double tax = billingService.calculateTax(subtotal);
        assertEquals(540.0, tax, 0.001, "Tax on 3000 at 18% should be 540");
    }

    @Test
    @DisplayName("Should calculate total = subtotal + tax")
    void testCalculateTotal() {
        double subtotal = 3000.0;
        double tax = 540.0;
        double total = billingService.calculateTotal(subtotal, tax);
        assertEquals(3540.0, total, 0.001, "Total should be 3540");
    }

    @Test
    @DisplayName("Should reject negative costs in calculations")
    void testNegativeCostsRejection() {
        assertThrows(IllegalArgumentException.class, () -> billingService.calculateSubtotal(-10.0, 50.0));
        assertThrows(IllegalArgumentException.class, () -> billingService.calculateSubtotal(10.0, -5.0));
        assertThrows(IllegalArgumentException.class, () -> billingService.calculateTax(-100.0));
    }
}
