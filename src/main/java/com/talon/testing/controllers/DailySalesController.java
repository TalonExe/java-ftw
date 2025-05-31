package com.talon.testing.controllers;

import com.talon.testing.models.Item;   // IMPORT ITEM MODEL
import com.talon.testing.models.Sales;
import com.talon.testing.utils.Router;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter; // FOR COMBOBOX DISPLAY

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;

public class DailySalesController extends Switchable implements Initializable {

    // FXML Table
    @FXML private TableView<Sales> salesTableView; // Renamed fx:id
    @FXML private TableColumn<Sales, String> SalesIdCol;
    @FXML private TableColumn<Sales, String> ItemIdCol;     // Displays itemCode from Sales model
    @FXML private TableColumn<Sales, Integer> QuantitySCol;
    @FXML private TableColumn<Sales, String> SalesDateCol;
    @FXML private TableColumn<Sales, String> SalesManagerIDCol1;

    // FXML Form Fields - Updated fx:ids
    @FXML private TextField salesIdField;
    @FXML private ComboBox<Item> itemComboBox; // REPLACED TextField with ComboBox
    @FXML private TextField quantitySoldField;
    @FXML private TextField salesDateField;
    @FXML private TextField salesManagerIdField;

    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    // Data
    private ObservableList<Sales> salesData = FXCollections.observableArrayList();
    private Map<String, Sales> salesMapCache;
    private ObservableList<Item> allItemsForComboBox = FXCollections.observableArrayList(); // For Item ComboBox
    private Map<String, Item> allItemsMapCache; // To quickly find Item objects

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Router router = Router.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        loadMasterItemData(); // Load items for ComboBox first

        if (salesTableView != null) {
            SalesIdCol.setCellValueFactory(new PropertyValueFactory<>("salesId"));
            ItemIdCol.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
            QuantitySCol.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
            SalesDateCol.setCellValueFactory(new PropertyValueFactory<>("salesDate"));
            SalesManagerIDCol1.setCellValueFactory(new PropertyValueFactory<>("salesManagerId"));
            salesTableView.setItems(salesData);
        }

        configureItemComboBox(); // Setup the item dropdown
        loadSalesDataFromFile();

        if (salesTableView != null) {
            salesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    populateForm(newSelection);
                    salesIdField.setEditable(false);
                    addButton.setText("Update");
                } else {
                    clearFormAction(null);
                }
            });
        }

        if (salesDateField != null && salesDateField.getText().isEmpty()) {
            salesDateField.setText(LocalDate.now().format(DATE_FORMATTER));
        }

        if (addButton != null) addButton.setOnAction(this::handleAddButtonAction);
        if (deleteButton != null) deleteButton.setOnAction(this::handleDeleteButtonAction);
        if (clearButton != null) clearButton.setOnAction(this::clearFormAction);

        clearFormAction(null); // Initial state
    }

    private void loadMasterItemData() {
        try {
            allItemsMapCache = Item.loadItems(); // From Item.java
            if (allItemsMapCache != null) {
                allItemsForComboBox.setAll(allItemsMapCache.values());
            } else {
                allItemsMapCache = new HashMap<>();
                allItemsForComboBox.clear();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Item Load Error", "Could not load items for selection: " + e.getMessage());
            allItemsMapCache = new HashMap<>(); // Ensure not null on error
        }
    }

    private void configureItemComboBox() {
        if (itemComboBox != null) {
            itemComboBox.setItems(allItemsForComboBox);
            itemComboBox.setConverter(new StringConverter<Item>() {
                @Override
                public String toString(Item item) {
                    // Uses Item.toString() method for display
                    return item == null ? null : item.toString();
                }
                @Override
                public Item fromString(String string) { return null; /* Not needed for selection only */ }
            });
        }
    }


    private void loadSalesDataFromFile() {
        try {
            this.salesMapCache = Sales.loadSales();
            if (this.salesMapCache == null) this.salesMapCache = new HashMap<>();
            salesData.setAll(this.salesMapCache.values());
            if (salesTableView != null) salesTableView.refresh();
            System.out.println("Loaded " + salesData.size() + " sales records.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load sales data: " + e.getMessage());
        }
    }

    private boolean saveSalesDataToFile() {
        try {
            if (this.salesMapCache == null) this.salesMapCache = new HashMap<>();
            this.salesMapCache.clear();
            for (Sales sale : salesData) {
                if (sale != null && sale.getSalesId() != null) {
                    this.salesMapCache.put(sale.getSalesId(), sale);
                }
            }
            Sales.saveSales(this.salesMapCache);
            System.out.println("Sales data saved successfully.");
            return true;
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Saving Error", "Could not save sales data: " + e.getMessage());
            return false;
        }
    }

    private void populateForm(Sales sale) {
        salesIdField.setText(sale.getSalesId());

        // Find and select the Item in ComboBox
        if (sale.getItemCode() != null && allItemsMapCache != null) {
            Item itemToSelect = allItemsMapCache.get(sale.getItemCode());
            itemComboBox.setValue(itemToSelect);
        } else {
            itemComboBox.getSelectionModel().clearSelection();
        }

        quantitySoldField.setText(String.valueOf(sale.getQuantitySold()));
        salesDateField.setText(sale.getSalesDate());
        salesManagerIdField.setText(sale.getSalesManagerId());
    }

    @FXML
    private void handleAddButtonAction(ActionEvent event) {
        if (!validateInputWithComboBox()) return;

        String salesId = salesIdField.getText().trim();
        Item selectedItem = itemComboBox.getSelectionModel().getSelectedItem();
        String itemCode = selectedItem.getItemCode(); // Get itemCode from selected Item
        int quantitySold = Integer.parseInt(quantitySoldField.getText().trim());
        String salesDateStr = salesDateField.getText().trim();
        String salesManagerId = salesManagerIdField.getText().trim();

        Sales selectedSaleFromTable = salesTableView.getSelectionModel().getSelectedItem();

        if (this.salesMapCache == null) this.salesMapCache = new HashMap<>();

        if (selectedSaleFromTable != null && selectedSaleFromTable.getSalesId().equals(salesId)) { // Update
            selectedSaleFromTable.setItemCode(itemCode);
            selectedSaleFromTable.setQuantitySold(quantitySold);
            selectedSaleFromTable.setSalesDate(salesDateStr);
            selectedSaleFromTable.setSalesManagerId(salesManagerId);

            int index = salesData.indexOf(selectedSaleFromTable);
            if (index != -1) salesData.set(index, selectedSaleFromTable);
            else salesTableView.refresh();

            this.salesMapCache.put(salesId, selectedSaleFromTable);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Sales record '" + salesId + "' updated.");
        } else { // Add new
            if (this.salesMapCache.containsKey(salesId)) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Sales ID '" + salesId + "' already exists.");
                return;
            }
            Sales newSale = new Sales(salesId, itemCode, quantitySold, salesDateStr, salesManagerId);
            salesData.add(newSale);
            this.salesMapCache.put(salesId, newSale);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Sales record '" + salesId + "' added.");
        }

        if (saveSalesDataToFile()) {
            clearFormAction(null);
        } else {
            loadSalesDataFromFile(); // Revert
        }
    }

    @FXML
    private void handleDeleteButtonAction(ActionEvent event) {
        Sales selectedSale = salesTableView.getSelectionModel().getSelectedItem();
        if (selectedSale == null) { /* ... */ return; }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Delete sales record?", ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                salesData.remove(selectedSale);
                if (this.salesMapCache != null) this.salesMapCache.remove(selectedSale.getSalesId());
                if (saveSalesDataToFile()) {
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Sales record deleted.");
                    clearFormAction(null);
                } else {
                    loadSalesDataFromFile(); // Revert
                }
            }
        });
    }

    @FXML
    private void clearFormAction(ActionEvent event) {
        salesIdField.clear();
        itemComboBox.getSelectionModel().clearSelection(); // Clear ComboBox
        quantitySoldField.clear();
        salesDateField.setText(LocalDate.now().format(DATE_FORMATTER));
        salesManagerIdField.clear();
        if (salesTableView != null) salesTableView.getSelectionModel().clearSelection();
        salesIdField.setEditable(true);
        addButton.setText("Add");
    }

    // Updated validation method
    private boolean validateInputWithComboBox() {
        String salesId = salesIdField.getText().trim();
        Item selectedItem = itemComboBox.getSelectionModel().getSelectedItem();
        String quantitySoldStr = quantitySoldField.getText().trim();
        String salesDateStr = salesDateField.getText().trim();
        String salesManagerIdVal = salesManagerIdField.getText().trim();

        if (salesId.isEmpty() || quantitySoldStr.isEmpty() || salesDateStr.isEmpty() || salesManagerIdVal.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Sales ID, Quantity, Date, and Manager ID are required.");
            return false;
        }
        if (selectedItem == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select an Item.");
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
}