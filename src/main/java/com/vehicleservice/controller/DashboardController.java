package com.vehicleservice.controller;

import com.vehicleservice.service.ReportService;
import com.vehicleservice.service.ReportService.DashboardSummary;
import com.vehicleservice.util.ErrorHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.Map;

/**
 * Controller and view for the executive Dashboard and visual analytics.
 */
public class DashboardController {

    private final ReportService reportService;
    private final Label custCountLabel = new Label("0");
    private final Label vehicleCountLabel = new Label("0");
    private final Label activeServiceLabel = new Label("0");
    private final Label revenueLabel = new Label("₹0.00");
    private final Label completedServiceLabel = new Label("0");
    private final Label pendingRevenueLabel = new Label("₹0.00");

    private final PieChart statusPieChart = new PieChart();
    private final BarChart<String, Number> revenueBarChart;

    public DashboardController() {
        this.reportService = new ReportService();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        revenueBarChart = new BarChart<>(xAxis, yAxis);
    }

    public Parent getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));

        Label headerLabel = new Label("Dashboard & Business Overview");
        headerLabel.getStyleClass().add("page-title");

        // KPI Cards Grid
        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(15);
        kpiGrid.setVgap(15);

        kpiGrid.add(createCard("TOTAL CUSTOMERS", custCountLabel, "#0284c7"), 0, 0);
        kpiGrid.add(createCard("REGISTERED VEHICLES", vehicleCountLabel, "#6366f1"), 1, 0);
        kpiGrid.add(createCard("ACTIVE SERVICES (IN PROGRESS)", activeServiceLabel, "#f59e0b"), 2, 0);
        kpiGrid.add(createCard("COMPLETED SERVICES", completedServiceLabel, "#10b981"), 3, 0);
        kpiGrid.add(createCard("REVENUE COLLECTED", revenueLabel, "#059669"), 0, 1);
        kpiGrid.add(createCard("PENDING PAYMENTS", pendingRevenueLabel, "#ef4444"), 1, 1);

        // Charts Row
        HBox chartsBox = new HBox(20);
        chartsBox.setPrefHeight(340);

        VBox pieBox = new VBox(10);
        pieBox.getStyleClass().add("card");
        Label pieTitle = new Label("Service Status Breakdown");
        pieTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        statusPieChart.setTitle("");
        statusPieChart.setLabelsVisible(true);
        pieBox.getChildren().addAll(pieTitle, statusPieChart);
        HBox.setHgrow(pieBox, Priority.ALWAYS);

        VBox barBox = new VBox(10);
        barBox.getStyleClass().add("card");
        Label barTitle = new Label("Financial Revenue Summary (₹)");
        barTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        revenueBarChart.setTitle("");
        revenueBarChart.setLegendVisible(false);
        barBox.getChildren().addAll(barTitle, revenueBarChart);
        HBox.setHgrow(barBox, Priority.ALWAYS);

        chartsBox.getChildren().addAll(pieBox, barBox);

        root.getChildren().addAll(headerLabel, kpiGrid, chartsBox);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        refreshDashboard();
        return scrollPane;
    }

    private VBox createCard(String titleText, Label valLabel, String accentColor) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setMinWidth(180);
        card.setPrefWidth(210);

        Label title = new Label(titleText);
        title.getStyleClass().add("stat-card-title");

        valLabel.getStyleClass().add("stat-card-value");
        valLabel.setStyle("-fx-text-fill: " + accentColor + ";");

        card.getChildren().addAll(title, valLabel);
        return card;
    }

    public void refreshDashboard() {
        try {
            DashboardSummary s = reportService.getDashboardSummary();
            custCountLabel.setText(String.valueOf(s.totalCustomers));
            vehicleCountLabel.setText(String.valueOf(s.totalVehicles));
            activeServiceLabel.setText(String.valueOf(s.pendingServices + s.inProgressServices));
            completedServiceLabel.setText(String.valueOf(s.completedServices));
            revenueLabel.setText(String.format("₹%.2f", s.collectedRevenue));
            pendingRevenueLabel.setText(String.format("₹%.2f", s.pendingRevenue));

            // Populate Pie Chart
            Map<String, Integer> dist = reportService.getServiceStatusDistribution();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            dist.forEach((status, count) -> {
                if (count > 0) {
                    pieData.add(new PieChart.Data(status + " (" + count + ")", count));
                }
            });
            statusPieChart.setData(pieData);

            // Populate Bar Chart
            revenueBarChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue");
            series.getData().add(new XYChart.Data<>("Collected", s.collectedRevenue));
            series.getData().add(new XYChart.Data<>("Pending", s.pendingRevenue));
            revenueBarChart.getData().add(series);

        } catch (SQLException e) {
            ErrorHandler.showError("Dashboard Error", ErrorHandler.getFriendlyDatabaseErrorMessage(e));
        }
    }
}
