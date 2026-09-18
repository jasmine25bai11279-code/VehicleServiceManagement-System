package com.vehicleservice.controller;

import com.vehicleservice.model.ServiceItem;
import com.vehicleservice.model.ServiceRecord;
import com.vehicleservice.model.Vehicle;
import com.vehicleservice.service.ServiceManagement;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller and view for Service Management and Service Details (Jobs, Parts & Status).
 */
public class ServiceController {

    private final ServiceManagement serviceManagement;
    private final VehicleService vehicleService;
    private final TableView<ServiceRecord> table = new TableView<>();
    private final ObservableList<ServiceRecord> serviceList = FXCollections.observableArrayList();
    private final ComboBox<String> statusFilterCombo = new ComboBox<>();
    private final TextField searchField = new TextField();

    public ServiceController() {
        this.serviceManagement = new ServiceManagement();
        this.vehicleService = new VehicleService();
    }

    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label headerLabel = new Label("Service Job Management");
        headerLabel.getStyleClass().add("page-title");

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Search by Reg #, Customer, Type, Status...");
        searchField.setPrefWidth(260);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadServices());

        statusFilterCombo.setItems(FXCollections.observableArrayList("All Statuses", "Pending", "In Progress", "Completed", "Cancelled"));
        statusFilterCombo.setValue("All Statuses");
        statusFilterCombo.setOnAction(e -> loadServices());

        Button createBtn = new Button("+ New Service Request");
        createBtn.getStyleClass().add("button-primary");
        createBtn.setOnAction(e -> showCreateServiceDialog());

        Button updateStatusBtn = new Button("Update Status");
        updateStatusBtn.getStyleClass().add("button-secondary");
        updateStatusBtn.setOnAction(e -> {
            ServiceRecord selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ErrorHandler.showWarning("No Selection", "Please select a service record to update its status.");
                return;
            }
            showUpdateStatusDialog(selected);
        });

        Button manageItemsBtn = new Button("Parts & Items");
        manageItemsBtn.getStyleClass().add("button-secondary");
        manageItemsBtn.setOnAction(e -> {
            ServiceRecord selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ErrorHandler.showWarning("No Selection", "Please select a service record to manage parts.");
                return;
            }
            showManageItemsDialog(selected);
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setOnAction(e -> {
            ServiceRecord selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ErrorHandler.showWarning("No Selection", "Please select a service record to delete.");
                return;
            }
            confirmAndDelete(selected);
        });

        toolbar.getChildren().addAll(searchField, statusFilterCombo, createBtn, updateStatusBtn, manageItemsBtn, deleteBtn);

        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(headerLabel, toolbar, table);
        loadServices();
        return root;
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ServiceRecord, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<ServiceRecord, String> regCol = new TableColumn<>("Vehicle Reg.");
        regCol.setCellValueFactory(new PropertyValueFactory<>("vehicleRegNumber"));

        TableColumn<ServiceRecord, String> custCol = new TableColumn<>("Customer");
        custCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<ServiceRecord, String> typeCol = new TableColumn<>("Service Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("serviceType"));

        TableColumn<ServiceRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("serviceDate"));

        TableColumn<ServiceRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("Pending".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-pending");
                    } else if ("In Progress".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-in-progress");
                    } else if ("Completed".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-completed");
                    } else {
                        badge.getStyleClass().add("badge-cancelled");
                    }
                    setGraphic(badge);
                }
            }
        });

        TableColumn<ServiceRecord, Double> labourCol = new TableColumn<>("Labour (₹)");
        labourCol.setCellValueFactory(new PropertyValueFactory<>("labourCost"));

        TableColumn<ServiceRecord, Double> partsCol = new TableColumn<>("Parts (₹)");
        partsCol.setCellValueFactory(new PropertyValueFactory<>("partsCost"));

        TableColumn<ServiceRecord, Double> totalCol = new TableColumn<>("Total (₹)");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));

        table.getColumns().setAll(idCol, regCol, custCol, typeCol, dateCol, statusCol, labourCol, partsCol, totalCol);
        table.setItems(serviceList);
    }

    public void loadServices() {
        try {
            String status = statusFilterCombo.getValue();
            String query = searchField.getText().trim();
            List<ServiceRecord> list;

            if (!query.isEmpty()) {
                list = serviceManagement.searchServices(query);
                if (status != null && !"All Statuses".equalsIgnoreCase(status)) {
                    list = list.stream().filter(s -> status.equalsIgnoreCase(s.getStatus())).toList();
                }
            } else if (status != null && !"All Statuses".equalsIgnoreCase(status)) {
                list = serviceManagement.getServicesByStatus(status);
            } else {
                list = serviceManagement.getAllServices();
            }

            serviceList.setAll(list);
        } catch (SQLException e) {
            ErrorHandler.showError("Database Error", ErrorHandler.getFriendlyDatabaseErrorMessage(e));
        }
    }

    private void showCreateServiceDialog() {
        try {
            List<Vehicle> vehicles = vehicleService.getAllVehicles();
            if (vehicles.isEmpty()) {
                ErrorHandler.showWarning("No Vehicles", "Please register at least one vehicle before creating a service request.");
                return;
            }

            Dialog<Boolean> dialog = new Dialog<>();
            dialog.setTitle("New Service Request");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(12);
            grid.setPadding(new Insets(20));

            ComboBox<Vehicle> vehicleCombo = new ComboBox<>(FXCollections.observableArrayList(vehicles));
            vehicleCombo.getSelectionModel().selectFirst();

            ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList(
                    "General Periodic Service", "Oil & Filter Change", "Brake Inspection & Repair",
                    "Wheel Alignment & Balancing", "Engine Diagnostics & Tune-up", "AC Servicing", "Electrical Repair"
            ));
            typeCombo.getSelectionModel().selectFirst();

            DatePicker datePicker = new DatePicker(LocalDate.now());

            TextField labourField = new TextField("500.00");
            labourField.setPromptText("Labour charge amount");

            TextArea descArea = new TextArea();
            descArea.setPromptText("Enter issue description or service notes...");
            descArea.setPrefRowCount(3);

            grid.add(new Label("Select Vehicle: *"), 0, 0);
            grid.add(vehicleCombo, 1, 0);
            grid.add(new Label("Service Type: *"), 0, 1);
            grid.add(typeCombo, 1, 1);
            grid.add(new Label("Service Date: *"), 0, 2);
            grid.add(datePicker, 1, 2);
            grid.add(new Label("Initial Labour Cost (₹): *"), 0, 3);
            grid.add(labourField, 1, 3);
            grid.add(new Label("Description / Issues:"), 0, 4);
            grid.add(descArea, 1, 4);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    Vehicle selectedVehicle = vehicleCombo.getValue();
                    if (selectedVehicle == null) {
                        ErrorHandler.showError("Validation Error", "Please select a vehicle.");
                        return false;
                    }
                    try {
                        double labour = Double.parseDouble(labourField.getText().trim());
                        String dateStr = datePicker.getValue() != null ? datePicker.getValue().toString() : LocalDate.now().toString();
                        serviceManagement.createServiceRequest(
                                selectedVehicle.getId(),
                                typeCombo.getValue(),
                                descArea.getText(),
                                dateStr,
                                labour
                        );
                        ErrorHandler.showInfo("Success", "Service request created successfully.");
                        loadServices();
                        return true;
                    } catch (NumberFormatException nfe) {
                        ErrorHandler.showError("Input Error", "Please enter a valid numeric value for labour cost.");
                        return false;
                    } catch (Exception ex) {
                        ErrorHandler.showError("Creation Failed", ex.getMessage());
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

    private void showUpdateStatusDialog(ServiceRecord record) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Update Status - Service #" + record.getId());

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        Label infoLabel = new Label("Current Status: " + record.getStatus() + "\nVehicle: " + record.getVehicleRegNumber());
        infoLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(ServiceManagement.VALID_STATUSES));
        statusCombo.setValue(record.getStatus());

        box.getChildren().addAll(infoLabel, new Label("Select New Status:"), statusCombo);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    serviceManagement.updateServiceStatus(record.getId(), statusCombo.getValue());
                    ErrorHandler.showInfo("Status Updated", "Service #" + record.getId() + " is now " + statusCombo.getValue() + ".");
                    loadServices();
                    return true;
                } catch (SQLException e) {
                    ErrorHandler.showError("Update Failed", ErrorHandler.getFriendlyDatabaseErrorMessage(e));
                    return false;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showManageItemsDialog(ServiceRecord record) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Parts & Labour Details - Service #" + record.getId());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setPrefWidth(600);

        Label header = new Label("Service #" + record.getId() + " (" + record.getVehicleRegNumber() + " - " + record.getServiceType() + ")");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        // Labour cost update
        HBox labourBox = new HBox(10);
        labourBox.setAlignment(Pos.CENTER_LEFT);
        TextField labourField = new TextField(String.valueOf(record.getLabourCost()));
        labourField.setPrefWidth(120);
        Button updateLabourBtn = new Button("Update Labour Cost");
        updateLabourBtn.getStyleClass().add("button-secondary");
        updateLabourBtn.setOnAction(e -> {
            try {
                double val = Double.parseDouble(labourField.getText().trim());
                serviceManagement.updateLabourCost(record.getId(), val);
                record.setLabourCost(val);
                ErrorHandler.showInfo("Success", "Labour cost updated to ₹" + val);
                loadServices();
            } catch (NumberFormatException nfe) {
                ErrorHandler.showError("Invalid Input", "Labour cost must be numeric.");
            } catch (SQLException ex) {
                ErrorHandler.showError("Update Failed", ex.getMessage());
            }
        });
        labourBox.getChildren().addAll(new Label("Labour Cost (₹):"), labourField, updateLabourBtn);

        // Line Items Table
        TableView<ServiceItem> itemsTable = new TableView<>();
        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        itemsTable.setPrefHeight(200);

        TableColumn<ServiceItem, String> itemCol = new TableColumn<>("Item / Part Name");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        TableColumn<ServiceItem, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setMaxWidth(60);

        TableColumn<ServiceItem, Double> priceCol = new TableColumn<>("Unit Price (₹)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        TableColumn<ServiceItem, Double> totalCol = new TableColumn<>("Total (₹)");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        itemsTable.getColumns().setAll(itemCol, qtyCol, priceCol, totalCol);

        ObservableList<ServiceItem> itemsList = FXCollections.observableArrayList();
        itemsTable.setItems(itemsList);

        Runnable refreshItems = () -> {
            try {
                List<ServiceItem> list = serviceManagement.getServiceItems(record.getId());
                itemsList.setAll(list);
                Optional<ServiceRecord> updated = serviceManagement.getServiceById(record.getId());
                updated.ifPresent(serviceRecord -> record.setPartsCost(serviceRecord.getPartsCost()));
                loadServices();
            } catch (SQLException ex) {
                ErrorHandler.showError("Error", ex.getMessage());
            }
        };

        refreshItems.run();

        // Add Item Form
        HBox addBox = new HBox(8);
        addBox.setAlignment(Pos.CENTER_LEFT);
        TextField nameField = new TextField();
        nameField.setPromptText("Part/Item Name");
        nameField.setPrefWidth(180);

        TextField qField = new TextField("1");
        qField.setPromptText("Qty");
        qField.setPrefWidth(60);

        TextField uField = new TextField("0.0");
        uField.setPromptText("Price");
        uField.setPrefWidth(90);

        Button addItemBtn = new Button("+ Add Part");
        addItemBtn.getStyleClass().add("button-primary");
        addItemBtn.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                int qty = Integer.parseInt(qField.getText().trim());
                double unit = Double.parseDouble(uField.getText().trim());
                serviceManagement.addServiceItem(record.getId(), name, qty, unit);
                nameField.clear();
                qField.setText("1");
                uField.setText("0.0");
                refreshItems.run();
            } catch (NumberFormatException nfe) {
                ErrorHandler.showError("Input Error", "Please provide valid numbers for quantity and unit price.");
            } catch (Exception ex) {
                ErrorHandler.showError("Failed to Add Item", ex.getMessage());
            }
        });

        Button delItemBtn = new Button("Delete Part");
        delItemBtn.getStyleClass().add("button-danger");
        delItemBtn.setOnAction(e -> {
            ServiceItem selected = itemsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ErrorHandler.showWarning("No Selection", "Please select a part to remove.");
                return;
            }
            try {
                serviceManagement.removeServiceItem(selected.getId(), record.getId());
                refreshItems.run();
            } catch (SQLException ex) {
                ErrorHandler.showError("Failed to Delete", ex.getMessage());
            }
        });

        addBox.getChildren().addAll(nameField, qField, uField, addItemBtn, delItemBtn);

        box.getChildren().addAll(header, labourBox, new Label("Parts & Consumables:"), itemsTable, addBox);
        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }

    private void confirmAndDelete(ServiceRecord record) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete Service Record #" + record.getId() + "? Associated parts and bills will also be deleted.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceManagement.deleteService(record.getId());
                ErrorHandler.showInfo("Deleted", "Service record deleted.");
                loadServices();
            } catch (SQLException ex) {
                ErrorHandler.showError("Cannot Delete", ErrorHandler.getFriendlyDatabaseErrorMessage(ex));
            }
        }
    }
}
