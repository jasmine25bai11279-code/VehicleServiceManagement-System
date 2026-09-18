package com.vehicleservice.service;

import com.vehicleservice.dao.ServiceDAO;
import com.vehicleservice.dao.VehicleDAO;
import com.vehicleservice.model.ServiceItem;
import com.vehicleservice.model.ServiceRecord;
import com.vehicleservice.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service managing the complete service request lifecycle, line items, and job statuses.
 */
public class ServiceManagement {

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_CANCELLED = "Cancelled";

    public static final List<String> VALID_STATUSES = Arrays.asList(
            STATUS_PENDING, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_CANCELLED
    );

    private final ServiceDAO serviceDAO;
    private final VehicleDAO vehicleDAO;

    public ServiceManagement() {
        this.serviceDAO = new ServiceDAO();
        this.vehicleDAO = new VehicleDAO();
    }

    public ServiceManagement(ServiceDAO serviceDAO, VehicleDAO vehicleDAO) {
        this.serviceDAO = serviceDAO;
        this.vehicleDAO = vehicleDAO;
    }

    public ServiceRecord createServiceRequest(int vehicleId, String serviceType, String description, String serviceDate, double labourCost) throws SQLException {
        ValidationUtil.validateServiceRecord(vehicleId, serviceType, labourCost);

        if (vehicleDAO.findById(vehicleId).isEmpty()) {
            throw new IllegalArgumentException("Vehicle with ID " + vehicleId + " does not exist.");
        }

        String date = (serviceDate != null && !serviceDate.trim().isEmpty())
                ? serviceDate.trim()
                : LocalDate.now().toString();

        ServiceRecord record = new ServiceRecord(vehicleId, serviceType.trim(), description, date, STATUS_PENDING, labourCost, 0.0);
        return serviceDAO.insert(record);
    }

    public boolean updateServiceStatus(int serviceId, String newStatus) throws SQLException {
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid service status: " + newStatus + ". Must be one of: " + VALID_STATUSES);
        }

        Optional<ServiceRecord> current = serviceDAO.findById(serviceId);
        if (current.isEmpty()) {
            throw new IllegalArgumentException("Service record with ID " + serviceId + " does not exist.");
        }

        return serviceDAO.updateStatus(serviceId, newStatus);
    }

    public boolean updateLabourCost(int serviceId, double labourCost) throws SQLException {
        if (!ValidationUtil.isNonNegative(labourCost)) {
            throw new IllegalArgumentException("Labour cost cannot be negative.");
        }

        Optional<ServiceRecord> currentOpt = serviceDAO.findById(serviceId);
        if (currentOpt.isEmpty()) {
            throw new IllegalArgumentException("Service record with ID " + serviceId + " does not exist.");
        }

        ServiceRecord current = currentOpt.get();
        current.setLabourCost(labourCost);
        return serviceDAO.update(current);
    }

    public ServiceItem addServiceItem(int serviceId, String itemName, int quantity, double unitPrice) throws SQLException {
        ValidationUtil.validateServiceItem(serviceId, itemName, quantity, unitPrice);

        Optional<ServiceRecord> recordOpt = serviceDAO.findById(serviceId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Service record with ID " + serviceId + " does not exist.");
        }

        ServiceItem item = new ServiceItem(serviceId, itemName.trim(), quantity, unitPrice);
        return serviceDAO.insertItem(item);
    }

    public boolean removeServiceItem(int itemId, int serviceId) throws SQLException {
        return serviceDAO.deleteItem(itemId, serviceId);
    }

    public List<ServiceItem> getServiceItems(int serviceId) throws SQLException {
        return serviceDAO.getItemsByServiceId(serviceId);
    }

    public Optional<ServiceRecord> getServiceById(int id) throws SQLException {
        return serviceDAO.findById(id);
    }

    public List<ServiceRecord> getAllServices() throws SQLException {
        return serviceDAO.findAll();
    }

    public List<ServiceRecord> getServicesByStatus(String status) throws SQLException {
        return serviceDAO.findByStatus(status);
    }

    public List<ServiceRecord> searchServices(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllServices();
        }
        return serviceDAO.search(keyword.trim());
    }

    public boolean deleteService(int id) throws SQLException {
        return serviceDAO.delete(id);
    }

    public int getServiceCount() throws SQLException {
        return serviceDAO.count();
    }

    public int getServiceCountByStatus(String status) throws SQLException {
        return serviceDAO.countByStatus(status);
    }
}
