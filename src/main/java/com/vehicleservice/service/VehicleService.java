package com.vehicleservice.service;

import com.vehicleservice.dao.CustomerDAO;
import com.vehicleservice.dao.ServiceDAO;
import com.vehicleservice.dao.VehicleDAO;
import com.vehicleservice.model.ServiceRecord;
import com.vehicleservice.model.Vehicle;
import com.vehicleservice.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service handling vehicle business rules and operations.
 */
public class VehicleService {

    private final VehicleDAO vehicleDAO;
    private final CustomerDAO customerDAO;
    private final ServiceDAO serviceDAO;

    public VehicleService() {
        this.vehicleDAO = new VehicleDAO();
        this.customerDAO = new CustomerDAO();
        this.serviceDAO = new ServiceDAO();
    }

    public VehicleService(VehicleDAO vehicleDAO, CustomerDAO customerDAO, ServiceDAO serviceDAO) {
        this.vehicleDAO = vehicleDAO;
        this.customerDAO = customerDAO;
        this.serviceDAO = serviceDAO;
    }

    public Vehicle registerVehicle(int customerId, String regNo, String brand, String model, String vehicleType, int year) throws SQLException {
        ValidationUtil.validateVehicle(customerId, regNo, brand, model, year);

        // Verify customer exists
        if (customerDAO.findById(customerId).isEmpty()) {
            throw new IllegalArgumentException("Customer with ID " + customerId + " does not exist.");
        }

        // Verify registration number uniqueness
        if (!vehicleDAO.isRegistrationUnique(regNo, 0)) {
            throw new IllegalArgumentException("Registration number '" + regNo.toUpperCase() + "' is already registered.");
        }

        Vehicle vehicle = new Vehicle(customerId, regNo.toUpperCase().trim(), brand.trim(), model.trim(), vehicleType, year);
        return vehicleDAO.insert(vehicle);
    }

    public boolean updateVehicle(int id, int customerId, String regNo, String brand, String model, String vehicleType, int year) throws SQLException {
        ValidationUtil.validateVehicle(customerId, regNo, brand, model, year);

        if (customerDAO.findById(customerId).isEmpty()) {
            throw new IllegalArgumentException("Customer with ID " + customerId + " does not exist.");
        }

        if (!vehicleDAO.isRegistrationUnique(regNo, id)) {
            throw new IllegalArgumentException("Registration number '" + regNo.toUpperCase() + "' is already registered to another vehicle.");
        }

        Vehicle vehicle = new Vehicle(id, customerId, regNo.toUpperCase().trim(), brand.trim(), model.trim(), vehicleType, year);
        return vehicleDAO.update(vehicle);
    }

    public boolean deleteVehicle(int id) throws SQLException {
        Optional<Vehicle> opt = vehicleDAO.findById(id);
        if (opt.isEmpty()) {
            return false;
        }
        return vehicleDAO.delete(id);
    }

    public Optional<Vehicle> getVehicleById(int id) throws SQLException {
        return vehicleDAO.findById(id);
    }

    public Optional<Vehicle> getVehicleByRegistration(String regNo) throws SQLException {
        return vehicleDAO.findByRegistrationNumber(regNo);
    }

    public List<Vehicle> getVehiclesByCustomerId(int customerId) throws SQLException {
        return vehicleDAO.findByCustomerId(customerId);
    }

    public List<Vehicle> getAllVehicles() throws SQLException {
        return vehicleDAO.findAll();
    }

    public List<Vehicle> searchVehicles(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllVehicles();
        }
        return vehicleDAO.search(keyword.trim());
    }

    public int getVehicleCount() throws SQLException {
        return vehicleDAO.count();
    }
}
