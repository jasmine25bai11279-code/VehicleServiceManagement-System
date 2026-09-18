package com.vehicleservice.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a service job / request for a vehicle.
 */
public class ServiceRecord {
    private int id;
    private int vehicleId;
    private String serviceType;
    private String description;
    private String serviceDate;
    private String status; // 'Pending', 'In Progress', 'Completed', 'Cancelled'
    private double labourCost;
    private double partsCost;

    // Transient fields for display convenience in tables and reports
    private String vehicleRegNumber;
    private String customerName;
    private String vehicleInfo;
    private List<ServiceItem> items = new ArrayList<>();

    public ServiceRecord() {
    }

    public ServiceRecord(int vehicleId, String serviceType, String description, String serviceDate, String status, double labourCost, double partsCost) {
        this.vehicleId = vehicleId;
        this.serviceType = serviceType;
        this.description = description;
        this.serviceDate = serviceDate;
        this.status = status;
        this.labourCost = labourCost;
        this.partsCost = partsCost;
    }

    public ServiceRecord(int id, int vehicleId, String serviceType, String description, String serviceDate, String status, double labourCost, double partsCost) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.serviceType = serviceType;
        this.description = description;
        this.serviceDate = serviceDate;
        this.status = status;
        this.labourCost = labourCost;
        this.partsCost = partsCost;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(String serviceDate) {
        this.serviceDate = serviceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getLabourCost() {
        return labourCost;
    }

    public void setLabourCost(double labourCost) {
        this.labourCost = labourCost;
    }

    public double getPartsCost() {
        return partsCost;
    }

    public void setPartsCost(double partsCost) {
        this.partsCost = partsCost;
    }

    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    public List<ServiceItem> getItems() {
        return items;
    }

    public void setItems(List<ServiceItem> items) {
        this.items = items;
    }

    public double getTotalCost() {
        return labourCost + partsCost;
    }

    @Override
    public String toString() {
        return "#" + id + " - " + serviceType + " (" + status + ")";
    }
}
