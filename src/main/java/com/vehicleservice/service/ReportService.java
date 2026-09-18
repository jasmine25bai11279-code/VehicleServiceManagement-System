package com.vehicleservice.service;

import com.vehicleservice.dao.BillDAO;
import com.vehicleservice.dao.CustomerDAO;
import com.vehicleservice.dao.ServiceDAO;
import com.vehicleservice.dao.VehicleDAO;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service providing statistical reports and aggregated metrics for the dashboard.
 */
public class ReportService {

    private final CustomerDAO customerDAO;
    private final VehicleDAO vehicleDAO;
    private final ServiceDAO serviceDAO;
    private final BillDAO billDAO;

    public ReportService() {
        this.customerDAO = new CustomerDAO();
        this.vehicleDAO = new VehicleDAO();
        this.serviceDAO = new ServiceDAO();
        this.billDAO = new BillDAO();
    }

    public ReportService(CustomerDAO customerDAO, VehicleDAO vehicleDAO, ServiceDAO serviceDAO, BillDAO billDAO) {
        this.customerDAO = customerDAO;
        this.vehicleDAO = vehicleDAO;
        this.serviceDAO = serviceDAO;
        this.billDAO = billDAO;
    }

    public static class DashboardSummary {
        public int totalCustomers;
        public int totalVehicles;
        public int totalServices;
        public int pendingServices;
        public int inProgressServices;
        public int completedServices;
        public int cancelledServices;
        public int totalBills;
        public int paidBills;
        public int unpaidBills;
        public double collectedRevenue;
        public double pendingRevenue;
    }

    public DashboardSummary getDashboardSummary() throws SQLException {
        DashboardSummary summary = new DashboardSummary();
        summary.totalCustomers = customerDAO.count();
        summary.totalVehicles = vehicleDAO.count();
        summary.totalServices = serviceDAO.count();
        summary.pendingServices = serviceDAO.countByStatus(ServiceManagement.STATUS_PENDING);
        summary.inProgressServices = serviceDAO.countByStatus(ServiceManagement.STATUS_IN_PROGRESS);
        summary.completedServices = serviceDAO.countByStatus(ServiceManagement.STATUS_COMPLETED);
        summary.cancelledServices = serviceDAO.countByStatus(ServiceManagement.STATUS_CANCELLED);
        summary.totalBills = billDAO.count();
        summary.paidBills = billDAO.countByPaymentStatus(BillingService.PAYMENT_PAID);
        summary.unpaidBills = billDAO.countByPaymentStatus(BillingService.PAYMENT_UNPAID);
        summary.collectedRevenue = billDAO.getTotalRevenue();
        summary.pendingRevenue = billDAO.getPendingRevenue();
        return summary;
    }

    public Map<String, Integer> getServiceStatusDistribution() throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        map.put(ServiceManagement.STATUS_PENDING, serviceDAO.countByStatus(ServiceManagement.STATUS_PENDING));
        map.put(ServiceManagement.STATUS_IN_PROGRESS, serviceDAO.countByStatus(ServiceManagement.STATUS_IN_PROGRESS));
        map.put(ServiceManagement.STATUS_COMPLETED, serviceDAO.countByStatus(ServiceManagement.STATUS_COMPLETED));
        map.put(ServiceManagement.STATUS_CANCELLED, serviceDAO.countByStatus(ServiceManagement.STATUS_CANCELLED));
        return map;
    }
}
