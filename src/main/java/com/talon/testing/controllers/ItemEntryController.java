package com.talon.testing.controllers;

import com.talon.testing.models.Item;
import com.talon.testing.models.Supplier; // IMPORT SUPPLIER
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter; // IMPORT FOR COMBOBOX DISPLAY

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // For date parsing if createDate is used
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;

public class ItemEntryController extends Switchable implements Initializable {

    // FXML Table and Columns
    @FXML private TableView<Item> poTable; // Consider renaming fx:id to itemTable

    @FXML private TableColumn<Item, String> poIdCol;        // Item Code
    @FXML private TableColumn<Item, String> prIdCol;        // Item Name
    @FXML private TableColumn<Item, String> statusCol;      // Description
    @FXML private TableColumn<Item, String> supplierIdColTable; // To display supplierId in table
    @FXML private TableColumn<Item, String> approvedCol;    // Unit Price (as String)
    @FXML private TableColumn<Item, String> approvedCol1;  // Current Stock (as String)
    @FXML private TableColumn<Item, String> approvedCol2;  // Minimum Stock (as String)


    // FXML Form Fields
    @FXML private TextField itemCodeField;
    @FXML private TextField itemNameField;
    @FXML private TextField descriptionField;
    @FXML private ComboBox<Supplier> supplierComboBox; // ADDED
    @FXML private TextField unitPriceField;
    @FXML private TextField currentStockField;
    @FXML private TextField minimumStockField;
    // @FXML private TextField createDateField; // If you add a field for createDate

    // FXML Action Buttons
    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    // Data store
    private ObservableList<Item> itemData = FXCollections.observableArrayList();
    private Map<String, Item> itemMapCache; // Renamed for clarity
    private ObservableList<Supplier> allSuppliers = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadAllSuppliers(); // Load suppliers for the ComboBox
        configureItemTable();
        configureSupplierComboBox();
        loadItemData();

        if (poTable != null) {
            poTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    populateForm(newSelection);
                } else {
                    clearFormAction(null);
                }
            });
        }

        if (addButton != null) addButton.setOnAction(this::handleAddButtonAction);
        if (deleteButton != null) deleteButton.setOnAction(this::handleDeleteButtonAction);
        if (clearButton != null) clearButton.setOnAction(this::clearFormAction);
    }

    private void loadAllSuppliers() {
        try {
            Map<String, Supplier> loadedSuppliers = Supplier.loadSuppliers();
            if (loadedSuppliers != null) {
                allSuppliers.setAll(loadedSuppliers.values());
            } else {
                allSuppliers.clear();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Supplier Load Error", "Could not load suppliers: " + e.getMessage());
        }
    }

    private void configureSupplierComboBox() {
        if (supplierComboBox != null) {
            supplierComboBox.setItems(allSuppliers);
            supplierComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Supplier supplier) {
                    return supplier == null ? null : supplier.getSupplierName() + " (" + supplier.getSupplierId() + ")";
                }
                @Override
                public Supplier fromString(String string) { return null; /* Not needed for selection */ }
            });
        }
    }

    private void configureItemTable() {
        if (poIdCol != null) poIdCol.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        if (prIdCol != null) prIdCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        if (statusCol != null) statusCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (supplierIdColTable != null) supplierIdColTable.setCellValueFactory(new PropertyValueFactory<>("supplierId")); // Bind to supplierId
        if (approvedCol != null) approvedCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice")); // String
        if (approvedCol1 != null) approvedCol1.setCellValueFactory(new PropertyValueFactory<>("currentStock")); // String
        if (approvedCol2 != null) approvedCol2.setCellValueFactory(new PropertyValueFactory<>("minimumStock")); // String
        if (poTable != null) poTable.setItems(itemData);
    }

    private void loadItemData() {
        try {
            this.itemMapCache = Item.loadItems();
            if (this.itemMapCache != null) {
                itemData.setAll(this.itemMapCache.values());
            } else {
                itemData.clear();
            }
            if (poTable != null) poTable.refresh();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load item data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean saveItemData() { // Changed to return boolean
        try {
            if (this.itemMapCache == null) this.itemMapCache = new HashMap<>();
            this.itemMapCache.clear(); // Rebuild map from observable list to ensure consistency
            for (Item item : itemData) {
                if (item != null && item.getItemCode() != null) {
                    this.itemMapCache.put(item.getItemCode(), item);
                }
            }
            Item.saveItems(this.itemMapCache);
            System.out.println("Item data saved successfully.");
            return true;
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Saving Error", "Could not save item data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void populateForm(Item item) {
        itemCodeField.setText(item.getItemCode());
        itemNameField.setText(item.getItemName());
        descriptionField.setText(item.getDescription());

        // Find and select the supplier in ComboBox
        if (item.getSupplierId() != null && allSuppliers != null) {
            Supplier itemSupplier = allSuppliers.stream()
                                     .filter(s -> item.getSupplierId().equals(s.getSupplierId()))
                                     .findFirst().orElse(null);
            supplierComboBox.setValue(itemSupplier);
        } else {
            supplierComboBox.getSelectionModel().clearSelection();
        }

        unitPriceField.setText(item.getUnitPrice()); // Already string
        currentStockField.setText(item.getCurrentStock()); // Already string
        minimumStockField.setText(item.getMinimumStock()); // Already string
        // if (createDateField != null && item.getCreateDate() != null) createDateField.setText(item.getCreateDate());
        itemCodeField.setEditable(false);
    }

    @FXML
    private void handleAddButtonAction(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        String itemCode = itemCodeField.getText().trim();
        String itemName = itemNameField.getText().trim();
        String description = descriptionField.getText().trim();
        Supplier selectedSupplier = supplierComboBox.getSelectionModel().getSelectedItem();
        String supplierId = (selectedSupplier != null) ? selectedSupplier.getSupplierId() : null;

        String unitPriceStr = unitPriceField.getText().trim();
        String currentStockStr = currentStockField.getText().trim();
        String minimumStockStr = minimumStockField.getText().trim();
        String createDate = LocalDate.now().format(DATE_FORMATTER); // Default createDate to today

        // Validate numeric fields (since model stores them as strings, validation is key)
        try {
            Double.parseDouble(unitPriceStr); // Just to validate format
            Integer.parseInt(currentStockStr);
            Integer.parseInt(minimumStockStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Price and stock fields must be valid numbers.");
            return;
        }
        if (supplierId == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a supplier.");
            return;
        }


        Item selectedItemFromTable = poTable.getSelectionModel().getSelectedItem();

        if (this.itemMapCache == null) this.itemMapCache = new HashMap<>();

        if (selectedItemFromTable != null && selectedItemFromTable.getItemCode().equals(itemCode)) { // Update existing
            selectedItemFromTable.setItemName(itemName);
            selectedItemFromTable.setDescription(description);
            selectedItemFromTable.setSupplierId(supplierId); // UPDATE SUPPLIER ID
            selectedItemFromTable.setUnitPrice(unitPriceStr);
            selectedItemFromTable.setCurrentStock(currentStockStr);
            selectedItemFromTable.setMinimumStock(minimumStockStr);
            // selectedItemFromTable.setCreateDate(createDate); // Only update if createDate is editable

            itemData.set(itemData.indexOf(selectedItemFromTable), selectedItemFromTable); // Trigger list update
            this.itemMapCache.put(itemCode, selectedItemFromTable); // Update in cache
            showAlert(Alert.AlertType.INFORMATION, "Success", "Item '" + itemCode + "' updated.");
        } else { // Add new
            if (this.itemMapCache.containsKey(itemCode)) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Item Code '" + itemCode + "' already exists.");
                return;
            }
            Item newItem = new Item(itemCode, itemName, description, unitPriceStr, currentStockStr, minimumStockStr, createDate, supplierId);
            itemData.add(newItem);
            this.itemMapCache.put(itemCode, newItem); // Add to cache
            showAlert(Alert.AlertType.INFORMATION, "Success", "Item '" + itemCode + "' added.");
        }

        if (saveItemData()) {
            clearFormAction(null);
        } else {
            loadItemData(); // Revert on save failure
        }
        if (itemCodeField != null) itemCodeField.setEditable(true);
    }

    @FXML
    private void handleDeleteButtonAction(ActionEvent event) {
        Item selectedItem = poTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Delete item '" + selectedItem.getItemName() + "'?", ButtonType.YES, ButtonType.NO);
        // ... (dialog setup)
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                itemData.remove(selectedItem);
                if (this.itemMapCache != null) this.itemMapCache.remove(selectedItem.getItemCode());
                if (saveItemData()) {
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Item deleted.");
                    clearFormAction(null);
                } else {
                    loadItemData(); // Revert
                }
            }
        });
    }

    @FXML
    private void clearFormAction(ActionEvent event) {
        if (itemCodeField != null) itemCodeField.clear();
        if (itemNameField != null) itemNameField.clear();
        if (descriptionField != null) descriptionField.clear();
        if (supplierComboBox != null) supplierComboBox.getSelectionModel().clearSelection();
        if (unitPriceField != null) unitPriceField.clear();
        if (currentStockField != null) currentStockField.clear();
        if (minimumStockField != null) minimumStockField.clear();
        // if (createDateField != null) createDateField.clear();
        if (poTable != null && poTable.getSelectionModel() != null) {
            poTable.getSelectionModel().clearSelection();
        }
        if (itemCodeField != null) itemCodeField.setEditable(true);
    }

    private boolean validateInput() {
        String itemCode = itemCodeField.getText().trim();
        String itemName = itemNameField.getText().trim();
        Supplier selectedSupplier = supplierComboBox.getSelectionModel().getSelectedItem();
        String unitPriceStr = unitPriceField.getText().trim();
        String currentStockStr = currentStockField.getText().trim();
        String minimumStockStr = minimumStockField.getText().trim();

        if (itemCode.isEmpty() || itemName.isEmpty() || unitPriceStr.isEmpty() || currentStockStr.isEmpty() || minimumStockStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Item Code, Name, Supplier, Price, and Stock fields are required.");
            return false;
        }
        if (selectedSupplier == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a supplier.");
            return false;
        }
        try {
            Double.parseDouble(unitPriceStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Inputz  Error", "Unit Price must be a valid number."); return false;
        }
        try {
            Integer.parseInt(currentStockStr);
            Integer.parseInt(minimumStockStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Stock fields must be valid integers."); return false;
        }
        return true;
    }
    
}