package com.talon.testing.controllers;

import com.talon.testing.models.Sales;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // IMPORT THIS
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button; // Keep if 'test' method uses it
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle; // IMPORT THIS
import java.util.stream.Collectors;
import java.util.Comparator; // For sorting chart data
import java.util.HashMap;

public class FinanceReportController extends Switchable implements Initializable { // IMPLEMENT Initializable

    // TableView and Columns
    @FXML private TableView<Sales> salesTableView;
    @FXML private TableColumn<Sales, String> salesIdColumn;
    @FXML private TableColumn<Sales, String> itemCodeColumn;
    @FXML private TableColumn<Sales, Integer> quantitySoldColumn; // Assuming Sales.quantitySold is int
    @FXML private TableColumn<Sales, String> salesDateColumn;
    @FXML private TableColumn<Sales, String> managerIdColumn;

    // Chart Components
    @FXML private BarChart<String, Number> salesBarChart;
    @FXML private CategoryAxis xAxis; // For X-axis of BarChart (categories)
    @FXML private NumberAxis yAxis;   // For Y-axis of BarChart (numerical values)
    @FXML private ComboBox<String> chartTypeComboBox;
    @FXML private Button generatePdfButton;


    private ObservableList<Sales> salesData = FXCollections.observableArrayList();
    private Map<String, Sales> salesMapCache; // Cache loaded sales
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");


    @Override // ADDED @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { // Correct signature
        configureSalesTable();
        configureChartControls();

        loadSalesDataFromFile(); // This will populate salesData and salesMapCache

        // Initially populate chart if data is available
        if (!salesData.isEmpty() && chartTypeComboBox.getSelectionModel().getSelectedItem() != null) {
            updateChart(chartTypeComboBox.getSelectionModel().getSelectedItem());
        } else if (!salesData.isEmpty()) {
            // Default chart type if none selected initially but data exists
            chartTypeComboBox.getSelectionModel().selectFirst(); // This will trigger the listener
        }
    }

    private void configureSalesTable() {
        if (salesIdColumn != null) salesIdColumn.setCellValueFactory(new PropertyValueFactory<>("salesId"));
        if (itemCodeColumn != null) itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        if (quantitySoldColumn != null) quantitySoldColumn.setCellValueFactory(new PropertyValueFactory<>("quantitySold")); // Assumes Sales model has int getQuantitySold()
        if (salesDateColumn != null) salesDateColumn.setCellValueFactory(new PropertyValueFactory<>("salesDate"));
        if (managerIdColumn != null) managerIdColumn.setCellValueFactory(new PropertyValueFactory<>("salesManagerId"));

        if (salesTableView != null) salesTableView.setItems(salesData);
    }

    private void configureChartControls() {
        if (chartTypeComboBox != null) {
            chartTypeComboBox.setItems(FXCollections.observableArrayList(
                    "Quantity Sold Per Item",
                    "Quantity Sold Per Month",
                    "Sales Count Per Manager"
            ));
            chartTypeComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        updateChart(newVal);
                    }
                }
            );
            chartTypeComboBox.getSelectionModel().selectFirst(); // Default selection
        }
    }

    private void loadSalesDataFromFile() {
        try {
            salesMapCache = Sales.loadSales(); // Assumes static Sales.loadSales()
            if (salesMapCache != null) {
                salesData.setAll(salesMapCache.values());
            } else {
                salesMapCache = new HashMap<>(); // Ensure not null
                salesData.clear();
            }
            if (salesTableView != null) salesTableView.refresh(); // Refresh table
            System.out.println("Loaded " + salesData.size() + " sales records.");

            // After loading data, update chart based on current ComboBox selection
            if (chartTypeComboBox != null && chartTypeComboBox.getSelectionModel().getSelectedItem() != null) {
                updateChart(chartTypeComboBox.getSelectionModel().getSelectedItem());
            } else if (chartTypeComboBox != null && !salesData.isEmpty()){
                 // If no selection but data exists, select first and let listener update
                chartTypeComboBox.getSelectionModel().selectFirst();
            }


        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load sales data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateChart(String chartType) {
        if (salesBarChart == null || salesData.isEmpty()) {
            if (salesBarChart != null) salesBarChart.getData().clear();
            return;
        }

        salesBarChart.getData().clear(); // Clear previous data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(chartType); // Set series name based on chart type

        switch (chartType) {
            case "Quantity Sold Per Item":
                xAxis.setLabel("Item Code");
                yAxis.setLabel("Total Quantity Sold");
                Map<String, Integer> quantityPerItem = salesData.stream()
                    .collect(Collectors.groupingBy(
                        Sales::getItemCode,
                        Collectors.summingInt(Sales::getQuantitySold)
                    ));
                quantityPerItem.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // Sort by quantity desc
                    .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));
                break;

            case "Quantity Sold Per Month":
                xAxis.setLabel("Month-Year");
                yAxis.setLabel("Total Quantity Sold");
                Map<YearMonth, Integer> quantityPerMonth = salesData.stream()
                    .collect(Collectors.groupingBy(
                        sale -> YearMonth.from(LocalDate.parse(sale.getSalesDate(), DATE_FORMATTER)),
                        Collectors.summingInt(Sales::getQuantitySold)
                    ));
                quantityPerMonth.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()) // Sort by month-year
                    .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey().format(MONTH_YEAR_FORMATTER), entry.getValue())));
                break;

            case "Sales Count Per Manager":
                xAxis.setLabel("Sales Manager ID");
                yAxis.setLabel("Number of Sales Transactions");
                Map<String, Long> salesPerManager = salesData.stream()
                    .collect(Collectors.groupingBy(
                        Sales::getSalesManagerId,
                        Collectors.counting()
                    ));
                salesPerManager.entrySet().stream()
                     .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // Sort by count desc
                    .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));
                break;

            default:
                System.err.println("Unknown chart type: " + chartType);
                return;
        }
        salesBarChart.getData().add(series);
    }


    @FXML
    private void handleGeneratePdfReport(ActionEvent event) {
        System.out.println("Attempting to generate PDF report...");
        if (salesData.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "PDF Report", "No data available to generate a report.");
            return;
        }
        // TODO: Implement PDF generation.
        // This would involve:
        // 1. Taking a snapshot of the chart: salesBarChart.snapshot(null, null);
        // 2. Iterating through salesData for tabular data.
        // 3. Using a PDF library like Apache PDFBox or iText 7 Community.
        showAlert(Alert.AlertType.INFORMATION, "PDF Report", "PDF report generation not yet implemented.");
    }
}