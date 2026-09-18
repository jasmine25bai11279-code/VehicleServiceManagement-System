package com.vehicleservice.model;

/**
 * Represents an invoice / bill for a completed vehicle service.
 */
public class Bill {
    private int id;
    private int serviceId;
    private double subtotal;
    private double tax;
    private double totalAmount;
    private String paymentStatus; // 'Unpaid', 'Paid'
    private String billDate;

    // Transient fields for reporting/UI display
    private String customerName;
    private String vehicleRegNumber;
    private String serviceType;

    public Bill() {
    }

    public Bill(int serviceId, double subtotal, double tax, double totalAmount, String paymentStatus, String billDate) {
        this.serviceId = serviceId;
        this.subtotal = subtotal;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.billDate = billDate;
    }

    public Bill(int id, int serviceId, double subtotal, double tax, double totalAmount, String paymentStatus, String billDate) {
        this.id = id;
        this.serviceId = serviceId;
        this.subtotal = subtotal;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.billDate = billDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public String toString() {
        return "Invoice #" + id + " - Total: " + totalAmount + " (" + paymentStatus + ")";
    }
}
