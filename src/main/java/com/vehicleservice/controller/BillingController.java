package com.vehicleservice.controller;

import com.vehicleservice.model.Bill;
import com.vehicleservice.model.ServiceItem;
import com.vehicleservice.model.ServiceRecord;
import com.vehicleservice.service.BillingService;
import com.vehicleservice.service.ServiceManagement;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Controller and view for Billing, Invoices & Payment Tracking.
 */
public class BillingController {

    private final BillingService billingService;
    private final ServiceManagement serviceManagement;
    private final TableView<Bill> table = new TableView<>();
    private final ObservableList<Bill> billList = FXCollections.observableArrayList();

    public BillingController() {
        this.billingService = new BillingService();
        this.serviceManagement = new ServiceManagement();
    }

    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label headerLabel = new Label("Billing & Payment Management");
        headerLabel.getStyleClass().add("page-title");

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button generateBillBtn = new Button("+ Generate New Bill");
        generateBillBtn.getStyleClass().add("button-primary");
        generateBillBtn.setOnAction(e -> showGenerateBillDialog());

        Button markPaidBtn = new Button("Mark as Paid");
        markPaidBtn.getStyleClass().add("button-success");
        markPaidBtn.setOnAction(e -> {
            Bill selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ErrorHandler.showWarning("No Selection", "Please select a bill to mark as paid.");
                return;
            }
            confirmAndMarkPaid(selected);
        });

        Button viewInvoiceBtn = new Button("View Invoice Details");
        viewInvoiceBtn.getStyleClass().add("button-secondary");
        viewInvoiceBtn.setOnAction(e -> {
            Bill selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                ErrorHandler.showWarning("No Selection", "Please select a bill to inspect.");
                return;
            }
            showInvoiceDetails(selected);
        });

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("button-secondary");
        refreshBtn.setOnAction(e -> loadBills());

        toolbar.getChildren().addAll(generateBillBtn, markPaidBtn, viewInvoiceBtn, refreshBtn);

        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(headerLabel, toolbar, table);
        loadBills();
        return root;
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Bill, Integer> idCol = new TableColumn<>("Invoice #");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(80);

        TableColumn<Bill, Integer> serviceIdCol = new TableColumn<>("Service #");
        serviceIdCol.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        serviceIdCol.setMaxWidth(80);

        TableColumn<Bill, String> custCol = new TableColumn<>("Customer");
        custCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<Bill, String> regCol = new TableColumn<>("Vehicle");
        regCol.setCellValueFactory(new PropertyValueFactory<>("vehicleRegNumber"));

        TableColumn<Bill, Double> subtotalCol = new TableColumn<>("Subtotal (₹)");
        subtotalCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        TableColumn<Bill, Double> taxCol = new TableColumn<>("Tax 18% (₹)");
        taxCol.setCellValueFactory(new PropertyValueFactory<>("tax"));

        TableColumn<Bill, Double> totalCol = new TableColumn<>("Total Amount (₹)");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        TableColumn<Bill, String> statusCol = new TableColumn<>("Payment Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("Paid".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #059669; -fx-padding: 3px 10px; -fx-background-radius: 12px; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-padding: 3px 10px; -fx-background-radius: 12px; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                }
            }
        });

        TableColumn<Bill, String> dateCol = new TableColumn<>("Bill Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("billDate"));

        table.getColumns().setAll(idCol, serviceIdCol, custCol, regCol, subtotalCol, taxCol, totalCol, statusCol, dateCol);
        table.setItems(billList);
    }

    public void loadBills() {
        try {
            List<Bill> list = billingService.getAllBills();
            billList.setAll(list);
        } catch (SQLException e) {
            ErrorHandler.showError("Database Error", ErrorHandler.getFriendlyDatabaseErrorMessage(e));
        }
    }

    private void showGenerateBillDialog() {
        try {
            // Find all completed services that don't have a bill yet
            List<ServiceRecord> completedServices = serviceManagement.getServicesByStatus(ServiceManagement.STATUS_COMPLETED);
            List<Bill> existingBills = billingService.getAllBills();
            List<Integer> billedServiceIds = existingBills.stream().map(Bill::getServiceId).toList();

            List<ServiceRecord> unbilledCompleted = completedServices.stream()
                    .filter(s -> !billedServiceIds.contains(s.getId()))
                    .toList();

            if (unbilledCompleted.isEmpty()) {
                ErrorHandler.showWarning("No Unbilled Services", "No completed services awaiting billing were found. Only 'Completed' services without an existing invoice can be billed.");
                return;
            }

            Dialog<Boolean> dialog = new Dialog<>();
            dialog.setTitle("Generate Bill for Completed Service");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(12);
            grid.setPadding(new Insets(20));

            ComboBox<ServiceRecord> serviceCombo = new ComboBox<>(FXCollections.observableArrayList(unbilledCompleted));
            serviceCombo.getSelectionModel().selectFirst();

            Label previewParts = new Label("Parts: ₹0.00");
            Label previewLabour = new Label("Labour: ₹0.00");
            Label previewSubtotal = new Label("Subtotal: ₹0.00");
            Label previewTax = new Label("Tax (18%): ₹0.00");
            Label previewTotal = new Label("Total: ₹0.00");
            previewTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");

            Runnable updatePreview = () -> {
                ServiceRecord sel = serviceCombo.getValue();
                if (sel != null) {
                    try {
                        List<ServiceItem> items = serviceManagement.getServiceItems(sel.getId());
                        double parts = billingService.calculatePartsCost(items);
                        double labour = sel.getLabourCost();
                        double subtotal = billingService.calculateSubtotal(parts, labour);
                        double tax = billingService.calculateTax(subtotal);
                        double total = billingService.calculateTotal(subtotal, tax);

                        previewParts.setText(String.format("Parts Cost: ₹%.2f (%d items)", parts, items.size()));
                        previewLabour.setText(String.format("Labour Cost: ₹%.2f", labour));
                        previewSubtotal.setText(String.format("Subtotal: ₹%.2f", subtotal));
                        previewTax.setText(String.format("GST / Tax (18%%): ₹%.2f", tax));
                        previewTotal.setText(String.format("Final Total: ₹%.2f", total));
                    } catch (Exception ex) {
                        previewTotal.setText("Calculation Error: " + ex.getMessage());
                    }
                }
            };

            serviceCombo.setOnAction(e -> updatePreview.run());
            updatePreview.run();

            grid.add(new Label("Select Completed Service:"), 0, 0);
            grid.add(serviceCombo, 1, 0);
            grid.add(new Label("Calculation Breakdown:"), 0, 1);
            VBox summaryBox = new VBox(6, previewParts, previewLabour, previewSubtotal, previewTax, previewTotal);
            summaryBox.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 12px; -fx-background-radius: 6px;");
            grid.add(summaryBox, 1, 1);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    ServiceRecord sel = serviceCombo.getValue();
                    if (sel == null) return false;
                    try {
                        Bill bill = billingService.generateBill(sel.getId());
                        ErrorHandler.showInfo("Invoice Generated", "Bill #" + bill.getId() + " created for ₹" + bill.getTotalAmount());
                        loadBills();
                        return true;
                    } catch (Exception ex) {
                        ErrorHandler.showError("Generation Failed", ex.getMessage());
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

    private void confirmAndMarkPaid(Bill bill) {
        if ("Paid".equalsIgnoreCase(bill.getPaymentStatus())) {
            ErrorHandler.showInfo("Already Paid", "This invoice is already marked as Paid.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Payment");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Record payment of ₹%.2f for Invoice #%d (Customer: %s)?",
                bill.getTotalAmount(), bill.getId(), bill.getCustomerName()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                billingService.markBillAsPaid(bill.getId());
                ErrorHandler.showInfo("Payment Recorded", "Invoice #" + bill.getId() + " is now marked as PAID.");
                loadBills();
            } catch (SQLException e) {
                ErrorHandler.showError("Update Failed", ErrorHandler.getFriendlyDatabaseErrorMessage(e));
            }
        }
    }

    private void showInvoiceDetails(Bill bill) {
        try {
            Optional<ServiceRecord> recordOpt = serviceManagement.getServiceById(bill.getServiceId());
            List<ServiceItem> items = serviceManagement.getServiceItems(bill.getServiceId());

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Tax Invoice #" + bill.getId());
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            VBox box = new VBox(15);
            box.setPadding(new Insets(25));
            box.setPrefWidth(550);
            box.setStyle("-fx-background-color: #ffffff;");

            Label title = new Label("VEHICLE SERVICE CENTER - TAX INVOICE");
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

            Label details = new Label(String.format(
                    "Invoice #: %d        Date: %s\nCustomer: %s\nVehicle: %s\nService Type: %s",
                    bill.getId(), bill.getBillDate(), bill.getCustomerName(), bill.getVehicleRegNumber(), bill.getServiceType()
            ));
            details.setStyle("-fx-font-size: 13px; -fx-line-spacing: 4px;");

            VBox itemsBox = new VBox(5);
            itemsBox.getChildren().add(new Label("Parts & Materials Breakdown:"));
            if (items.isEmpty()) {
                itemsBox.getChildren().add(new Label("  (No spare parts replaced)"));
            } else {
                for (ServiceItem item : items) {
                    Label iLbl = new Label(String.format("  • %s  x%d  @ ₹%.2f = ₹%.2f",
                            item.getItemName(), item.getQuantity(), item.getUnitPrice(), item.getTotalPrice()));
                    itemsBox.getChildren().add(iLbl);
                }
            }

            double labour = recordOpt.map(ServiceRecord::getLabourCost).orElse(0.0);
            Label labourLbl = new Label(String.format("Labour Charges: ₹%.2f", labour));
            labourLbl.setStyle("-fx-font-weight: bold;");

            Label mathBreakdown = new Label(String.format(
                    "-------------------------------------------\n" +
                    "Subtotal (Parts + Labour): ₹%.2f\n" +
                    "GST / Tax (18%%):            ₹%.2f\n" +
                    "-------------------------------------------\n" +
                    "TOTAL AMOUNT DUE:          ₹%.2f\n" +
                    "Payment Status:            %s",
                    bill.getSubtotal(), bill.getTax(), bill.getTotalAmount(), bill.getPaymentStatus().toUpperCase()
            ));
            mathBreakdown.setStyle("-fx-font-family: monospace; -fx-font-size: 13px; -fx-font-weight: bold;");

            box.getChildren().addAll(title, details, itemsBox, labourLbl, mathBreakdown);
            dialog.getDialogPane().setContent(box);
            dialog.showAndWait();
        } catch (SQLException e) {
            ErrorHandler.showError("Database Error", ErrorHandler.getFriendlyDatabaseErrorMessage(e));
        }
    }
}
