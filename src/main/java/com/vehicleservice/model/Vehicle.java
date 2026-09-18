package com.vehicleservice.model;

/**
 * Represents a vehicle belonging to a customer.
 */
public class Vehicle {
    private int id;
    private int customerId;
    private String registrationNumber;
    private String brand;
    private String model;
    private String vehicleType;
    private int manufacturingYear;
    // Transient field for display convenience
    private String customerName;

    public Vehicle() {
    }

    public Vehicle(int customerId, String registrationNumber, String brand, String model, String vehicleType, int manufacturingYear) {
        this.customerId = customerId;
        this.registrationNumber = registrationNumber;
        this.brand = brand;
        this.model = model;
        this.vehicleType = vehicleType;
        this.manufacturingYear = manufacturingYear;
    }

    public Vehicle(int id, int customerId, String registrationNumber, String brand, String model, String vehicleType, int manufacturingYear) {
        this.id = id;
        this.customerId = customerId;
        this.registrationNumber = registrationNumber;
        this.brand = brand;
        this.model = model;
        this.vehicleType = vehicleType;
        this.manufacturingYear = manufacturingYear;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getManufacturingYear() {
        return manufacturingYear;
    }

    public void setManufacturingYear(int manufacturingYear) {
        this.manufacturingYear = manufacturingYear;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return registrationNumber + " - " + brand + " " + model;
    }
}
