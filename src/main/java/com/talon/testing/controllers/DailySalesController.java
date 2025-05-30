package com.talon.testing.controllers;

import com.talon.testing.models.Sales; // Import your Sales model
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.ResourceBundle;

public class DailySalesController extends Switchable {

    // FXML Table and Columns (names from your FXML)
    @FXML private TableView<Sales> poTable; // Consider renaming fx:id to salesTable in FXML for clarity
    @FXML private TableColumn<Sales, String> SalesIdCol;
    @FXML private TableColumn<Sales, String> ItemIdCol;
    @FXML private TableColumn<Sales, Integer> QuantitySCol; // Quantity Sold
    @FXML private TableColumn<Sales, String> SalesDateCol;
    @FXML private TableColumn<Sales, String> SalesManagerIDCol1; // Sales Manager ID

    // FXML Form Fields (names from your FXML)
    @FXML private TextField itemCodeField;      // Corresponds to "Sales ID" label in form
    @FXML private TextField itemNameField;      // Corresponds to "Item Code" label in form
    @FXML private TextField descriptionField;   // Corresponds to "Quantity Sold" label in form
    @FXML private TextField unitPriceField;     // Corresponds to "Sales Date" label in form
    @FXML private TextField currentStockField;  // Corresponds to "Sales Manager ID" label in form

    // FXML Action Buttons
    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    // Data store for Sales
    private ObservableList<Sales> salesData = FXCollections.observableArrayList();
    private Map<String, Sales> salesMap; // To hold data loaded from/saved to file
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configure table columns to map to Sales object properties
        SalesIdCol.setCellValueFactory(new PropertyValueFactory<>("salesId"));
        ItemIdCol.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        QuantitySCol.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        SalesDateCol.setCellValueFactory(new PropertyValueFactory<>("salesDate"));
        SalesManagerIDCol1.setCellValueFactory(new PropertyValueFactory<>("salesManagerId"));

        poTable.setItems(salesData); // Bind observable list to the table

        loadSalesDataFromFile(); // Load initial data

        // Listener to populate form when a row is selected
        poTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            } else {
                clearFormAction(null); // Clear form if selection is removed
            }
        });

        // Set default sales date if field is empty
        if (unitPriceField.getText().isEmpty()) {
            unitPriceField.setText(LocalDate.now().format(DATE_FORMATTER));
        }

        // Add action handlers for buttons
        addButton.setOnAction(this::handleAddButtonAction);
        deleteButton.setOnAction(this::handleDeleteButtonAction);
        clearButton.setOnAction(this::clearFormAction);

        // FXML Label: The labels "SUPPLIER ENTRY" and "SUPPLIER DETAILS" seem incorrect for a sales entry screen.
        // You might want to change them in FXML to "DAILY SALES ENTRY" and "SALES DETAILS" respectively.
    }

    private void loadSalesDataFromFile() {
        try {
            salesMap = Sales.loadSales();
            salesData.setAll(salesMap.values());
            poTable.refresh();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load sales data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveSalesDataToFile() {
        try {
            // Rebuild map from observable list before saving
            salesMap.clear();
            for(Sales sale : salesData){
                salesMap.put(sale.getSalesId(), sale);
            }
            //Sales.saveSales(salesMap);
            System.out.println("help me");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Saving Error", "Could not save sales data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateForm(Sales sale) {
        itemCodeField.setText(sale.getSalesId());
        itemNameField.setText(sale.getItemCode());
        descriptionField.setText(String.valueOf(sale.getQuantitySold()));
        unitPriceField.setText(sale.getSalesDate());
        currentStockField.setText(sale.getSalesManagerId());
        itemCodeField.setEditable(false); // Sales ID usually not editable once set
    }

    @FXML
    private void handleAddButtonAction(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        String salesId = itemCodeField.getText().trim();
        String itemCode = itemNameField.getText().trim();
        int quantitySold = Integer.parseInt(descriptionField.getText().trim());
        LocalDate salesDate;
        try {
            salesDate = LocalDate.parse(unitPriceField.getText().trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid date format for 'Sales Date'. Use YYYY-MM-DD.");
            return;
        }
        String salesManagerId = currentStockField.getText().trim();

        Sales selectedSale = poTable.getSelectionModel().getSelectedItem();

        if (selectedSale != null && selectedSale.getSalesId().equals(salesId)) { // Update existing
            selectedSale.setItemCode(itemCode);
            selectedSale.setQuantitySold(quantitySold);
            selectedSale.setSalesDate(salesDate.toString()); // Model's setter takes LocalDate
            selectedSale.setSalesManagerId(salesManagerId);

            poTable.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Sales record '" + salesId + "' updated.");
        } else { // Add new
            if (salesMap.containsKey(salesId)) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Sales ID '" + salesId + "' already exists. Choose a different ID or select to update.");
                return;
            }
            Sales newSale = new Sales(salesId, itemCode, quantitySold, salesDate.toString(), salesManagerId);
            salesData.add(newSale);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Sales record '" + salesId + "' added.");
        }

        saveSalesDataToFile();
        clearFormAction(null);
        itemCodeField.setEditable(true); // Allow Sales ID editing for next new entry
    }

    @FXML
    private void handleDeleteButtonAction(ActionEvent event) {
        Sales selectedSale = poTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a sales record to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Delete sales record '" + selectedSale.getSalesId() + "'?", ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                salesData.remove(selectedSale);
                saveSalesDataToFile();
                clearFormAction(null);
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Sales record '" + selectedSale.getSalesId() + "' deleted.");
            }
        });
    }

    @FXML
    private void clearFormAction(ActionEvent event) {
        itemCodeField.clear();
        itemNameField.clear();
        descriptionField.clear();
        unitPriceField.setText(LocalDate.now().format(DATE_FORMATTER)); // Reset to today
        currentStockField.clear();
        poTable.getSelectionModel().clearSelection();
        itemCodeField.setEditable(true);
    }

    private boolean validateInput() {
        // Match TextField fx:id to the actual data they represent
        String salesId = itemCodeField.getText().trim();         // This field is for Sales ID
        String itemCodeVal = itemNameField.getText().trim();   // This field is for Item Code
        String quantitySoldStr = descriptionField.getText().trim(); // This field is for Quantity Sold
        String salesDateStr = unitPriceField.getText().trim();     // This field is for Sales Date
        String salesManagerIdVal = currentStockField.getText().trim(); // This field is for Sales Manager ID

        if (salesId.isEmpty() || itemCodeVal.isEmpty() || quantitySoldStr.isEmpty() || salesDateStr.isEmpty() || salesManagerIdVal.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "All fields are required.");
            return false;
        }

        try {
            Integer.parseInt(quantitySoldStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Quantity Sold must be a valid number.");
            return false;
        }

        try {
            LocalDate.parse(salesDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid date format for 'Sales Date'. Use YYYY-MM-DD.");
            return false;
        }
        return true;
    }


    // Placeholder for navigation buttons - these should have onAction="#methodName" in FXML
    // if handled by this controller.
    @FXML
    private void test(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        System.out.println("Action for: " + clickedButton.getText() + " (not yet implemented for navigation)");
        // Example: if(clickedButton.getText().equals("Item Entry")) { /* load Item Entry FXML */ }
    }
}