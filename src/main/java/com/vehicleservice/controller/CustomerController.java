package com.vehicleservice.controller;

import com.vehicleservice.model.Customer;
import com.vehicleservice.model.Vehicle;
import com.vehicleservice.service.CustomerService;
import com.vehicleservice.service.VehicleService;
import com.vehicleservice.util.ErrorHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Controller and view for Customer Management (CRUD + Search).
 */
public class CustomerController {

    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final TableView<Customer> table = new TableView<>();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private final TextField searchField = new TextField();

    public CustomerController() {
        this.customerService = new CustomerService();
        this.vehicleService = new VehicleService();
    }

    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Header
        Label headerLabel = new Label("Customer Management");
        headerLabel.getStyleClass().add("page-title");

        // Action Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Search by name, phone or email...");
        searchField.setPrefWidth(280);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadCustomers(newVal));

        Button addBtn = new Button("+ Add Customer");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setOnAction(e -> showCustomerForm(null));

        Button editBtn = new Button("Edit Selected");
        editBtn.getStyleClass().add("button-secondary");
        editBtn.setOnAction(e -> {
            Customer selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ErrorHandler.showWarning("No Selection", "Please select a customer to edit.");
                return;
            }
            showCustomerForm(selected);
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> {
            Customer selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ErrorHandler.showWarning("No Selection", "Please select a customer to delete.");
                return;
            }
            confirmAndDelete(selected);
        });

        Button viewVehiclesBtn = new Button("View Vehicles");
        viewVehiclesBtn.getStyleClass().add("button-secondary");
        viewVehiclesBtn.setOnAction(e -> {
            Customer selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ErrorHandler.showWarning("No Selection", "Please select a customer to view their vehicles.");
                return;
            }
            showCustomerVehicles(selected);
        });

        toolbar.getChildren().addAll(searchField, addBtn, editBtn, deleteBtn, viewVehiclesBtn);

        // Table setup
        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(headerLabel, toolbar, table);
        loadCustomers("");
        return root;
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Customer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Customer, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone Number");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email Address");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Customer, String> addrCol = new TableColumn<>("Address");
        addrCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Customer, String> dateCol = new TableColumn<>("Registered At");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        table.getColumns().setAll(idCol, nameCol, phoneCol, emailCol, addrCol, dateCol);
        table.setItems(customerList);
    }

    public void loadCustomers(String filter) {
        try {
            List<Customer> list = customerService.searchCustomers(filter);
            customerList.setAll(list);
        } catch (SQLException e) {
            ErrorHandler.showError("Database Error", ErrorHandler.getFriendlyDatabaseErrorMessage(e));
        }
    }

    private void showCustomerForm(Customer existing) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Customer" : "Edit Customer #" + existing.getId());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        TextField phoneField = new TextField(existing != null ? existing.getPhone() : "");
        TextField emailField = new TextField(existing != null ? existing.getEmail() : "");
        TextField addrField = new TextField(existing != null ? existing.getAddress() : "");

        grid.add(new Label("Full Name: *"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Phone: *"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Address:"), 0, 3);
        grid.add(addrField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    if (existing == null) {
                        customerService.createCustomer(nameField.getText(), phoneField.getText(), emailField.getText(), addrField.getText());
                        ErrorHandler.showInfo("Success", "Customer successfully registered.");
                    } else {
                        customerService.updateCustomer(existing.getId(), nameField.getText(), phoneField.getText(), emailField.getText(), addrField.getText());
                        ErrorHandler.showInfo("Success", "Customer details updated.");
                    }
                    loadCustomers(searchField.getText());
                    return true;
                } catch (IllegalArgumentException | SQLException ex) {
                    ErrorHandler.showError("Validation / DB Error", ex.getMessage());
                    return false;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void confirmAndDelete(Customer customer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete customer '" + customer.getName() + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                customerService.deleteCustomer(customer.getId());
                ErrorHandler.showInfo("Deleted", "Customer was successfully removed.");
                loadCustomers(searchField.getText());
            } catch (IllegalStateException | SQLException ex) {
                ErrorHandler.showError("Cannot Delete", ex.getMessage());
            }
        }
    }

    private void showCustomerVehicles(Customer customer) {
        try {
            List<Vehicle> vehicles = vehicleService.getVehiclesByCustomerId(customer.getId());
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Vehicles for " + customer.getName());
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            VBox box = new VBox(10);
            box.setPadding(new Insets(20));
            box.setPrefWidth(500);

            if (vehicles.isEmpty()) {
                box.getChildren().add(new Label("No vehicles registered for this customer yet."));
            } else {
                for (Vehicle v : vehicles) {
                    Label vLabel = new Label("• " + v.getRegistrationNumber() + " - " + v.getBrand() + " " + v.getModel() + " (" + v.getVehicleType() + ", " + v.getManufacturingYear() + ")");
                    vLabel.setStyle("-fx-font-size: 13px; -fx-padding: 3px 0;");
                    box.getChildren().add(vLabel);
                }
            }

            dialog.getDialogPane().setContent(box);
            dialog.showAndWait();
        } catch (SQLException e) {
            ErrorHandler.showError("Database Error", ErrorHandler.getFriendlyDatabaseErrorMessage(e));
        }
    }
}
