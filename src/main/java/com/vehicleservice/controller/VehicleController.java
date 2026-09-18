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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * Controller and view for Vehicle Management (CRUD, Search & Customer Association).
 */
public class VehicleController {

    private final VehicleService vehicleService;
    private final CustomerService customerService;
    private final TableView<Vehicle> table = new TableView<>();
    private final ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();
    private final TextField searchField = new TextField();

    public VehicleController() {
        this.vehicleService = new VehicleService();
        this.customerService = new CustomerService();
    }

    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label headerLabel = new Label("Vehicle Management");
        headerLabel.getStyleClass().add("page-title");

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Search by Reg #, Brand, Model or Customer...");
        searchField.setPrefWidth(320);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadVehicles(newVal));

        Button addBtn = new Button("+ Register Vehicle");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setOnAction(e -> showVehicleForm(null));

        Button editBtn = new Button("Edit Selected");
        editBtn.getStyleClass().add("button-secondary");
        editBtn.setOnAction(e -> {
            Vehicle selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ErrorHandler.showWarning("No Selection", "Please select a vehicle to edit.");
                return;
            }
            showVehicleForm(selected);
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> {
            Vehicle selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ErrorHandler.showWarning("No Selection", "Please select a vehicle to delete.");
                return;
            }
            confirmAndDelete(selected);
        });

        toolbar.getChildren().addAll(searchField, addBtn, editBtn, deleteBtn);

        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(headerLabel, toolbar, table);
        loadVehicles("");
        return root;
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Vehicle, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Vehicle, String> regCol = new TableColumn<>("Registration No.");
        regCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));

        TableColumn<Vehicle, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<Vehicle, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("vehicleType"));

        TableColumn<Vehicle, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("manufacturingYear"));
        yearCol.setMaxWidth(80);

        TableColumn<Vehicle, String> customerCol = new TableColumn<>("Owner Name");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        table.getColumns().setAll(idCol, regCol, brandCol, modelCol, typeCol, yearCol, customerCol);
        table.setItems(vehicleList);
    }

    public void loadVehicles(String filter) {
        try {
            List<Vehicle> list = vehicleService.searchVehicles(filter);
            vehicleList.setAll(list);
        } catch (SQLException e) {
            ErrorHandler.showError("Database Error", ErrorHandler.getFriendlyDatabaseErrorMessage(e));
        }
    }

    private void showVehicleForm(Vehicle existing) {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            if (customers.isEmpty()) {
                ErrorHandler.showWarning("No Customers", "Please add at least one customer before registering a vehicle.");
                return;
            }

            Dialog<Boolean> dialog = new Dialog<>();
            dialog.setTitle(existing == null ? "Register New Vehicle" : "Edit Vehicle #" + existing.getId());

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(12);
            grid.setPadding(new Insets(20));

            ComboBox<Customer> customerCombo = new ComboBox<>(FXCollections.observableArrayList(customers));
            if (existing != null) {
                for (Customer c : customers) {
                    if (c.getId() == existing.getCustomerId()) {
                        customerCombo.setValue(c);
                        break;
                    }
                }
            } else {
                customerCombo.getSelectionModel().selectFirst();
            }

            TextField regField = new TextField(existing != null ? existing.getRegistrationNumber() : "");
            regField.setPromptText("e.g. KA01AB1234");

            TextField brandField = new TextField(existing != null ? existing.getBrand() : "");
            brandField.setPromptText("e.g. Toyota");

            TextField modelField = new TextField(existing != null ? existing.getModel() : "");
            modelField.setPromptText("e.g. Corolla");

            ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList(
                    "Sedan", "SUV", "Hatchback", "Coupe", "Motorcycle", "Van", "Truck"
            ));
            typeCombo.setValue(existing != null ? existing.getVehicleType() : "Sedan");

            int currentYear = Year.now().getValue();
            Spinner<Integer> yearSpinner = new Spinner<>(1900, currentYear + 1, existing != null ? existing.getManufacturingYear() : currentYear);
            yearSpinner.setEditable(true);

            grid.add(new Label("Customer (Owner): *"), 0, 0);
            grid.add(customerCombo, 1, 0);
            grid.add(new Label("Registration No: *"), 0, 1);
            grid.add(regField, 1, 1);
            grid.add(new Label("Brand: *"), 0, 2);
            grid.add(brandField, 1, 2);
            grid.add(new Label("Model: *"), 0, 3);
            grid.add(modelField, 1, 3);
            grid.add(new Label("Vehicle Type: *"), 0, 4);
            grid.add(typeCombo, 1, 4);
            grid.add(new Label("Manufacturing Year: *"), 0, 5);
            grid.add(yearSpinner, 1, 5);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    Customer selectedCust = customerCombo.getValue();
                    if (selectedCust == null) {
                        ErrorHandler.showError("Validation Error", "Please select a customer.");
                        return false;
                    }
                    try {
                        if (existing == null) {
                            vehicleService.registerVehicle(
                                    selectedCust.getId(),
                                    regField.getText(),
                                    brandField.getText(),
                                    modelField.getText(),
                                    typeCombo.getValue(),
                                    yearSpinner.getValue()
                            );
                            ErrorHandler.showInfo("Success", "Vehicle registered successfully.");
                        } else {
                            vehicleService.updateVehicle(
                                    existing.getId(),
                                    selectedCust.getId(),
                                    regField.getText(),
                                    brandField.getText(),
                                    modelField.getText(),
                                    typeCombo.getValue(),
                                    yearSpinner.getValue()
                            );
                            ErrorHandler.showInfo("Success", "Vehicle details updated successfully.");
                        }
                        loadVehicles(searchField.getText());
                        return true;
                    } catch (IllegalArgumentException | SQLException ex) {
                        ErrorHandler.showError("Operation Failed", ex.getMessage());
                        return false;
                    }
                }
                return null;
            });

            dialog.showAndWait();
        } catch (SQLException e) {
            ErrorHandler.showError("Database Error", ErrorHandler.getFriendlyDatabaseErrorMessage(e));
        }
    }

    private void confirmAndDelete(Vehicle vehicle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete vehicle '" + vehicle.getRegistrationNumber() + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                vehicleService.deleteVehicle(vehicle.getId());
                ErrorHandler.showInfo("Deleted", "Vehicle was successfully removed.");
                loadVehicles(searchField.getText());
            } catch (SQLException ex) {
                ErrorHandler.showError("Cannot Delete", ErrorHandler.getFriendlyDatabaseErrorMessage(ex));
            }
        }
    }
}
