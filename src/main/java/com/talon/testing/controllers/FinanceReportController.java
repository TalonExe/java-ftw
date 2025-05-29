package com.talon.testing.controllers;

import com.talon.testing.models.Sales; // Make sure this path is correct
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class FinanceReportController extends Switchable{

    // TableView and Columns from FXML
    @FXML
    private TableView<Sales> salesTableView;

    @FXML
    private TableColumn<Sales, String> salesIdColumn;

    @FXML
    private TableColumn<Sales, String> itemCodeColumn;

    @FXML
    private TableColumn<Sales, String> quantitySoldColumn; // If Sales.quantitySold is String
    // If Sales.quantitySold is Integer/Double, change to TableColumn<Sales, Integer/Double>

    @FXML
    private TableColumn<Sales, String> salesDateColumn; // If Sales.salesDate is String
    // If Sales.salesDate is LocalDate, change to TableColumn<Sales, LocalDate>

    @FXML
    private TableColumn<Sales, String> managerIdColumn;


    // This is the ObservableList that will hold the data for the TableView
    private ObservableList<Sales> salesData = FXCollections.observableArrayList();

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    public void initialize() {
        // 1. Set up the cell value factories for the table columns.
        // These tell JavaFX how to get the data for each cell from the Sales object.
        // The string argument in PropertyValueFactory must match the property name in your Sales class
        // (e.g., "salesId" for a getSalesId() method).
        salesIdColumn.setCellValueFactory(new PropertyValueFactory<>("salesId"));
        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        quantitySoldColumn.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        salesDateColumn.setCellValueFactory(new PropertyValueFactory<>("salesDate"));
        managerIdColumn.setCellValueFactory(new PropertyValueFactory<>("salesManagerId")); // Assuming your Sales model has 'salesManagerId'

        // 2. Load data into the table
        loadSalesData();

        // 3. Set the items for the table
        salesTableView.setItems(salesData);
    }

    /**
     * Loads sales data from the Sales model and populates the salesData ObservableList.
     */
    private void loadSalesData() {
        try {
            Map<String, Sales> loadedSalesMap = Sales.loadSales();
            if (loadedSalesMap != null && !loadedSalesMap.isEmpty()) {
                salesData.addAll(loadedSalesMap.values());
                System.out.println("Loaded " + salesData.size() + " sales records.");
            } else {
                System.out.println("No sales data found or sales.txt is empty/invalid.");
                // Optionally, show an info alert to the user
                // showAlert(Alert.AlertType.INFORMATION, "Data Loading", "No sales data found.");
            }
        } catch (IOException e) {
            System.err.println("Error loading sales data: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load sales data: " + e.getMessage());
        }
    }


    /**
     * Generic handler for buttons that are currently linked to "#test".
     * You'll want to replace this or add more specific handlers.
     *
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    private void test(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText();
        System.out.println("Action triggered for: " + buttonText);

        if ("Generate PDF Report".equals(buttonText)) {
            handleGeneratePdfReport();
        } else if ("PO Approval".equals(buttonText)) {
            // Handle PO Approval logic
            System.out.println("PO Approval action...");
        } else if ("Finance Report".equals(buttonText)) {
            // This button might just be for displaying this view, or it could refresh data
            System.out.println("Finance Report (view) action...");
            // If you want to refresh:
            // salesData.clear();
            // loadSalesData();
        } else if ("Purchase Requisitions".equals(buttonText)) {
            // Handle Purchase Requisitions logic
            System.out.println("Purchase Requisitions action...");
        } else if ("Process Payment".equals(buttonText)) {
            // Handle Process Payment logic
            System.out.println("Process Payment action...");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Action", "Action for '" + buttonText + "' is not yet implemented.");
        }
    }

    /**
     * Placeholder for PDF generation logic.
     */
    private void handleGeneratePdfReport() {
        System.out.println("Attempting to generate PDF report...");
        if (salesData.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "PDF Report", "No data available to generate a report.");
            return;
        }
        // TODO: Implement PDF generation logic here
        // You might use a library like Apache PDFBox or iText.
        // Example:
        // PdfGenerator.generateSalesReport(salesData, "finance_report.pdf");
        showAlert(Alert.AlertType.INFORMATION, "PDF Report", "PDF report generation initiated (not yet implemented).");
    }
}