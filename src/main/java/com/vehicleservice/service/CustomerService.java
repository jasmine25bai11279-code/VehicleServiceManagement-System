package com.vehicleservice.service;

import com.vehicleservice.dao.CustomerDAO;
import com.vehicleservice.dao.VehicleDAO;
import com.vehicleservice.model.Customer;
import com.vehicleservice.model.Vehicle;
import com.vehicleservice.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service handling customer business logic and operations.
 */
public class CustomerService {

    private final CustomerDAO customerDAO;
    private final VehicleDAO vehicleDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
        this.vehicleDAO = new VehicleDAO();
    }

    public CustomerService(CustomerDAO customerDAO, VehicleDAO vehicleDAO) {
        this.customerDAO = customerDAO;
        this.vehicleDAO = vehicleDAO;
    }

    public Customer createCustomer(String name, String phone, String email, String address) throws SQLException {
        ValidationUtil.validateCustomer(name, phone, email);
        Customer customer = new Customer(name.trim(), phone.trim(), email != null ? email.trim() : "", address != null ? address.trim() : "");
        return customerDAO.insert(customer);
    }

    public boolean updateCustomer(int id, String name, String phone, String email, String address) throws SQLException {
        ValidationUtil.validateCustomer(name, phone, email);
        Customer customer = new Customer(id, name.trim(), phone.trim(), email != null ? email.trim() : "", address != null ? address.trim() : "", null);
        return customerDAO.update(customer);
    }

    public boolean deleteCustomer(int id) throws SQLException {
        List<Vehicle> vehicles = vehicleDAO.findByCustomerId(id);
        if (!vehicles.isEmpty()) {
            throw new IllegalStateException("Cannot delete customer: " + vehicles.size() + " vehicle(s) are registered under this customer.");
        }
        return customerDAO.delete(id);
    }

    public Optional<Customer> getCustomerById(int id) throws SQLException {
        return customerDAO.findById(id);
    }

    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }

    public List<Customer> searchCustomers(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerDAO.search(keyword.trim());
    }

    public int getCustomerCount() throws SQLException {
        return customerDAO.count();
    }
}
