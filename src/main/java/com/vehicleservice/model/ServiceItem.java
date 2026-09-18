package com.vehicleservice.model;

/**
 * Represents a spare part or consumable item used in a vehicle service.
 */
public class ServiceItem {
    private int id;
    private int serviceId;
    private String itemName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;

    public ServiceItem() {
    }

    public ServiceItem(int serviceId, String itemName, int quantity, double unitPrice) {
        this.serviceId = serviceId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = quantity * unitPrice;
    }

    public ServiceItem(int id, int serviceId, String itemName, int quantity, double unitPrice, double totalPrice) {
        this.id = id;
        this.serviceId = serviceId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = this.quantity * this.unitPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.totalPrice = this.quantity * this.unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return itemName + " (Qty: " + quantity + ", Rate: " + unitPrice + ", Total: " + totalPrice + ")";
    }
}
