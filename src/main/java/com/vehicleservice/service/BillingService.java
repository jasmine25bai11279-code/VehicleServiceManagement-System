package com.vehicleservice.service;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.dao.BillDAO;
import com.vehicleservice.dao.ServiceDAO;
import com.vehicleservice.model.Bill;
import com.vehicleservice.model.ServiceItem;
import com.vehicleservice.model.ServiceRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service enforcing billing calculations, invoice generation, and payment tracking.
 * Formulas strictly aligned with Section 11:
 * - Parts Cost = sum of service items
 * - Subtotal = Parts Cost + Labour Cost
 * - Tax = Subtotal * Tax Rate
 * - Total = Subtotal + Tax
 */
public class BillingService {

    public static final String PAYMENT_UNPAID = "Unpaid";
    public static final String PAYMENT_PAID = "Paid";

    private final BillDAO billDAO;
    private final ServiceDAO serviceDAO;
    private final double taxRate;

    public BillingService() {
        this.billDAO = new BillDAO();
        this.serviceDAO = new ServiceDAO();
        this.taxRate = DatabaseConfig.getTaxRate();
    }

    public BillingService(BillDAO billDAO, ServiceDAO serviceDAO, double taxRate) {
        this.billDAO = billDAO;
        this.serviceDAO = serviceDAO;
        this.taxRate = taxRate;
    }

    public double getTaxRate() {
        return taxRate;
    }

    /**
     * Calculates the parts cost from the given list of service items.
     */
    public double calculatePartsCost(List<ServiceItem> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (ServiceItem item : items) {
            sum += (item.getQuantity() * item.getUnitPrice());
        }
        return round(sum);
    }

    /**
     * Calculates subtotal = partsCost + labourCost.
     */
    public double calculateSubtotal(double partsCost, double labourCost) {
        if (partsCost < 0 || labourCost < 0) {
            throw new IllegalArgumentException("Parts and labour costs cannot be negative.");
        }
        return round(partsCost + labourCost);
    }

    /**
     * Calculates tax = subtotal * taxRate.
     */
    public double calculateTax(double subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative.");
        }
        return round(subtotal * taxRate);
    }

    /**
     * Calculates total = subtotal + tax.
     */
    public double calculateTotal(double subtotal, double tax) {
        return round(subtotal + tax);
    }

    /**
     * Generates a bill for a completed service record.
     * Enforces the rule that a bill can only be created for an existing, Completed service.
     */
    public Bill generateBill(int serviceId) throws SQLException {
        Optional<ServiceRecord> recordOpt = serviceDAO.findById(serviceId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Service record with ID " + serviceId + " does not exist.");
        }

        ServiceRecord record = recordOpt.get();
        if (!ServiceManagement.STATUS_COMPLETED.equalsIgnoreCase(record.getStatus())) {
            throw new IllegalStateException("Cannot generate bill: service status is '" + record.getStatus() + "'. It must be 'Completed' first.");
        }

        // Check if a bill already exists for this service
        Optional<Bill> existingBill = billDAO.findByServiceId(serviceId);
        if (existingBill.isPresent()) {
            throw new IllegalStateException("A bill has already been generated for Service Record #" + serviceId + ".");
        }

        List<ServiceItem> items = serviceDAO.getItemsByServiceId(serviceId);
        double partsCost = calculatePartsCost(items);
        double labourCost = record.getLabourCost();
        double subtotal = calculateSubtotal(partsCost, labourCost);
        double tax = calculateTax(subtotal);
        double total = calculateTotal(subtotal, tax);

        Bill bill = new Bill(serviceId, subtotal, tax, total, PAYMENT_UNPAID, LocalDate.now().toString());
        return billDAO.insert(bill);
    }

    public boolean markBillAsPaid(int billId) throws SQLException {
        Optional<Bill> billOpt = billDAO.findById(billId);
        if (billOpt.isEmpty()) {
            throw new IllegalArgumentException("Bill with ID " + billId + " does not exist.");
        }
        return billDAO.updatePaymentStatus(billId, PAYMENT_PAID);
    }

    public Optional<Bill> getBillById(int id) throws SQLException {
        return billDAO.findById(id);
    }

    public Optional<Bill> getBillByServiceId(int serviceId) throws SQLException {
        return billDAO.findByServiceId(serviceId);
    }

    public List<Bill> getAllBills() throws SQLException {
        return billDAO.findAll();
    }

    public double getTotalCollectedRevenue() throws SQLException {
        return round(billDAO.getTotalRevenue());
    }

    public double getTotalPendingRevenue() throws SQLException {
        return round(billDAO.getPendingRevenue());
    }

    public int getBillCount() throws SQLException {
        return billDAO.count();
    }

    public int getPaidBillsCount() throws SQLException {
        return billDAO.countByPaymentStatus(PAYMENT_PAID);
    }

    public int getUnpaidBillsCount() throws SQLException {
        return billDAO.countByPaymentStatus(PAYMENT_UNPAID);
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
