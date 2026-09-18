package com.vehicleservice;

import com.vehicleservice.config.DatabaseConfig;
import com.vehicleservice.controller.BillingController;
import com.vehicleservice.controller.CustomerController;
import com.vehicleservice.controller.DashboardController;
import com.vehicleservice.controller.LoginController;
import com.vehicleservice.controller.ServiceController;
import com.vehicleservice.controller.VehicleController;
import com.vehicleservice.dao.CustomerDAO;
import com.vehicleservice.model.Customer;
import com.vehicleservice.model.User;
import com.vehicleservice.model.Vehicle;
import com.vehicleservice.service.AuthService;
import com.vehicleservice.service.BillingService;
import com.vehicleservice.service.CustomerService;
import com.vehicleservice.service.ServiceManagement;
import com.vehicleservice.service.VehicleService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Main application entry point for the Vehicle Service Management System.
 */
public class Main extends Application {

    private Stage primaryStage;
    private BorderPane mainLayout;
    private StackPane contentArea;

    private DashboardController dashboardController;
    private CustomerController customerController;
    private VehicleController vehicleController;
    private ServiceController serviceController;
    private BillingController billingController;

    private Button activeNavButton;

    public static void main(String[] args) {
        // Initialize SQLite schema and seed defaults
        DatabaseConfig.initializeDatabase();
        seedSampleDataIfEmpty();

        // Launch JavaFX GUI
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Vehicle Service Management System");

        showLoginScreen();

        primaryStage.setWidth(1100);
        primaryStage.setHeight(720);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    private void showLoginScreen() {
        LoginController loginController = new LoginController(this::onLoginSuccess);
        Parent loginView = loginController.getView();
        applyStyles(loginView);
        primaryStage.setScene(new Scene(loginView));
    }

    private void onLoginSuccess(User user) {
        // Instantiate controllers
        dashboardController = new DashboardController();
        customerController = new CustomerController();
        vehicleController = new VehicleController();
        serviceController = new ServiceController();
        billingController = new BillingController();

        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("root");

        // Sidebar Navigation
        VBox sidebar = buildSidebar(user);
        mainLayout.setLeft(sidebar);

        // Content Area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f8fafc;");
        mainLayout.setCenter(contentArea);

        // Default screen: Dashboard
        switchView(dashboardController.getView(), null);

        applyStyles(mainLayout);
        primaryStage.setScene(new Scene(mainLayout));
    }

    private VBox buildSidebar(User user) {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");

        Label brand = new Label("🚗 AutoCare Pro");
        brand.getStyleClass().add("sidebar-brand");

        Label staffInfo = new Label("Staff: " + user.getFullName() + "\nRole: " + user.getRole());
        staffInfo.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-padding: 0 0 15px 12px;");

        Button navDash = createNavButton("📊  Dashboard", () -> {
            dashboardController.refreshDashboard();
            switchView(dashboardController.getView(), null);
        });

        Button navCust = createNavButton("👥  Customers", () -> {
            customerController.loadCustomers("");
            switchView(customerController.getView(), null);
        });

        Button navVehicles = createNavButton("🚘  Vehicles", () -> {
            vehicleController.loadVehicles("");
            switchView(vehicleController.getView(), null);
        });

        Button navServices = createNavButton("🔧  Service Jobs", () -> {
            serviceController.loadServices();
            switchView(serviceController.getView(), null);
        });

        Button navBilling = createNavButton("💳  Billing & Invoices", () -> {
            billingController.loadBills();
            switchView(billingController.getView(), null);
        });

        // Logout button pushed to bottom
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button navLogout = createNavButton("🔒  Sign Out", () -> {
            AuthService.logout();
            showLoginScreen();
        });
        navLogout.setStyle("-fx-text-fill: #f87171;");

        sidebar.getChildren().addAll(brand, staffInfo, navDash, navCust, navVehicles, navServices, navBilling, spacer, navLogout);

        // Mark dashboard active initially
        setActiveNav(navDash);
        return sidebar;
    }

    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setOnAction(e -> {
            setActiveNav(btn);
            action.run();
        });
        return btn;
    }

    private void setActiveNav(Button btn) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-button-active");
        }
        activeNavButton = btn;
        if (activeNavButton != null && !activeNavButton.getStyleClass().contains("nav-button-active")) {
            activeNavButton.getStyleClass().add("nav-button-active");
        }
    }

    private void switchView(Parent view, Button correspondingNav) {
        contentArea.getChildren().setAll(view);
        if (correspondingNav != null) {
            setActiveNav(correspondingNav);
        }
    }

    private void applyStyles(Parent root) {
        URL cssResource = getClass().getResource("/styles/style.css");
        if (cssResource != null) {
            root.getStylesheets().add(cssResource.toExternalForm());
        }
    }

    /**
     * Seeds realistic demonstration data if database is empty.
     */
    private static void seedSampleDataIfEmpty() {
        try {
            CustomerDAO customerDAO = new CustomerDAO();
            if (customerDAO.count() == 0) {
                CustomerService custService = new CustomerService();
                VehicleService vehService = new VehicleService();
                ServiceManagement srvService = new ServiceManagement();
                BillingService billService = new BillingService();

                // 1. Customers
                Customer c1 = custService.createCustomer("Rajesh Kumar", "9876543210", "rajesh@example.com", "124 Park Avenue, Indiranagar, Bengaluru");
                Customer c2 = custService.createCustomer("Priya Sharma", "9812345678", "priya.sharma@example.com", "45 Green Valley, Koramangala, Bengaluru");
                Customer c3 = custService.createCustomer("Amit Patel", "9923456789", "amit.patel@example.com", "89 MG Road, Pune");

                // 2. Vehicles
                Vehicle v1 = vehService.registerVehicle(c1.getId(), "KA01AB1234", "Honda", "City ZX", "Sedan", 2022);
                Vehicle v2 = vehService.registerVehicle(c2.getId(), "KA05XY9999", "Hyundai", "Creta SX", "SUV", 2023);
                Vehicle v3 = vehService.registerVehicle(c3.getId(), "MH12CD5678", "Royal Enfield", "Classic 350", "Motorcycle", 2021);

                // 3. Services
                // Completed service with bill
                var s1 = srvService.createServiceRequest(v1.getId(), "General Periodic Service", "10,000 km periodic maintenance & oil inspection", "2026-09-10", 800.0);
                srvService.addServiceItem(s1.getId(), "Synthetic Engine Oil (4L)", 1, 1600.0);
                srvService.addServiceItem(s1.getId(), "Engine Oil Filter", 1, 350.0);
                srvService.addServiceItem(s1.getId(), "Wiper Fluid Sachet", 2, 50.0);
                srvService.updateServiceStatus(s1.getId(), ServiceManagement.STATUS_COMPLETED);
                var bill1 = billService.generateBill(s1.getId());
                billService.markBillAsPaid(bill1.getId());

                // In Progress service
                var s2 = srvService.createServiceRequest(v2.getId(), "Brake Inspection & Repair", "Brake pedal vibrating at high speeds", "2026-09-15", 600.0);
                srvService.addServiceItem(s2.getId(), "Front Ceramic Brake Pads", 1, 1900.0);
                srvService.updateServiceStatus(s2.getId(), ServiceManagement.STATUS_IN_PROGRESS);

                // Pending service
                srvService.createServiceRequest(v3.getId(), "Wheel Alignment & Balancing", "Slight pull to the left side", "2026-09-18", 450.0);

                System.out.println("Realistic sample demonstration data seeded successfully.");
            }
        } catch (Exception e) {
            System.err.println("Sample data seeding skipped or failed: " + e.getMessage());
        }
    }
}
