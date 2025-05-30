package com.talon.testing.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.talon.testing.models.Item;
import com.talon.testing.models.POItem;       // For processing POs
import com.talon.testing.models.PurchaseOrder; // For processing POs
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType; // For confirmation/dialogs
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog; // For simple stock update input
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.talon.testing.models.Supplier;
public class StockManagementController extends Switchable implements Initializable {

    // For "All Items" Tab
    @FXML private TableView<Item> itemTableView;
    @FXML private TableColumn<Item, String> itemCodeColumn;
    @FXML private TableColumn<Item, String> itemNameColumn;
    @FXML private TableColumn<Item, String> descriptionColumn;
    @FXML private TableColumn<Item, String> unitPriceColumn;    // Item model stores as String
    @FXML private TableColumn<Item, String> currentStockColumn; // Item model stores as String
    @FXML private TableColumn<Item, String> minimumStockColumn; // Item model stores as String
    @FXML private TableColumn<Item, String> itemSupplierIdColumn; // To display supplierId
    @FXML private TableColumn<Item, String> createDateColumn;

    // For "Low Stock Items" Tab
    @FXML private TableView<Item> lowStockTableView;
    @FXML private TableColumn<Item, String> lowItemCodeCol;
    @FXML private TableColumn<Item, String> lowItemNameCol;
    @FXML private TableColumn<Item, String> lowDescriptionCol;
    @FXML private TableColumn<Item, String> lowUnitPriceCol;
    @FXML private TableColumn<Item, String> lowCurrentStockCol;
    @FXML private TableColumn<Item, String> lowMinimumStockCol;
    @FXML private TableColumn<Item, String> lowItemSupplierIdCol;
    @FXML private TableColumn<Item, String> lowCreateDateCol;

    // Data
    private ObservableList<Item> allItemsData = FXCollections.observableArrayList();
    private ObservableList<Item> lowStockItemsData = FXCollections.observableArrayList();
    private Map<String, Item> itemMapCache; // In-memory cache of all items

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureAllItemsTable();
        configureLowStockItemsTable();
        loadAllItemsData(); // Load data into itemMapCache and allItemsData
    }

    private void configureAllItemsTable() {
        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        currentStockColumn.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        minimumStockColumn.setCellValueFactory(new PropertyValueFactory<>("minimumStock"));
        itemSupplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        createDateColumn.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        itemTableView.setItems(allItemsData);
    }

    private void configureLowStockItemsTable() {
        lowItemCodeCol.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        lowItemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        lowDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        lowUnitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        lowCurrentStockCol.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        lowMinimumStockCol.setCellValueFactory(new PropertyValueFactory<>("minimumStock"));
        lowItemSupplierIdCol.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        lowCreateDateCol.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        lowStockTableView.setItems(lowStockItemsData);
    }

    @FXML
    public void loadAllItemsData() { // Renamed to be more specific
        try {
            itemMapCache = Item.loadItems(); // Load from static method in Item model
            if (itemMapCache == null) itemMapCache = new HashMap<>(); // Ensure not null

            allItemsData.setAll(itemMapCache.values());
            // itemTableView.setItems(allItemsData); // Already set in configure method
            itemTableView.refresh();
            System.out.println("All items loaded. Count: " + allItemsData.size());
             // Also update low stock view if it was active
            handleViewLowStock(null); // Pass null for ActionEvent if called internally
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load items: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateStock(ActionEvent event) {
        Item currentSelectedItem = itemTableView.getSelectionModel().getSelectedItem(); // Initial attempt
        if (currentSelectedItem == null) {
            currentSelectedItem = lowStockTableView.getSelectionModel().getSelectedItem(); // Second attempt
        }

        // Now, 'currentSelectedItem' holds the actual item to work with, or null.
        // Create a final or effectively final reference for the lambda.
        final Item itemToUpdateInLambda = currentSelectedItem;

        if (itemToUpdateInLambda != null) { // Check the effectively final variable
            TextInputDialog dialog = new TextInputDialog(itemToUpdateInLambda.getCurrentStock());
            dialog.setTitle("Update Stock");
            dialog.setHeaderText("Update stock for: " + itemToUpdateInLambda.getItemName());
            dialog.setContentText("Enter new current stock value:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(newStockStr -> { // Lambda now uses itemToUpdateInLambda
                try {
                    int newStockVal = Integer.parseInt(newStockStr);
                    if (newStockVal < 0) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input", "Stock cannot be negative.");
                        return;
                    }

                    // Use itemToUpdateInLambda inside the lambda
                    Item itemInCache = itemMapCache.get(itemToUpdateInLambda.getItemCode());
                    if (itemInCache != null) {
                        itemInCache.setCurrentStock(newStockStr); // Update the cached item
                        try {
                            Item.saveItems(itemMapCache); // Save the whole map
                            showAlert(Alert.AlertType.INFORMATION, "Stock Updated", "Stock for " + itemToUpdateInLambda.getItemName() + " updated to " + newStockStr);
                            loadAllItemsData(); // Refresh views
                        } catch (IOException e) {
                            showAlert(Alert.AlertType.ERROR, "Save Error", "Could not save updated stock: " + e.getMessage());
                        }
                    } else {
                        // This case should ideally not happen if itemToUpdateInLambda came from itemMapCache initially via loadAllItemsData
                        showAlert(Alert.AlertType.ERROR, "Data Sync Error", "Selected item not found in cache for update.");
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number for stock.");
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item from either table to update its stock.");
        }
    }

    @FXML
    private void handleViewLowStock(ActionEvent event) { // Renamed to match FXML
        if (itemMapCache == null) {
            loadAllItemsData(); // Ensure data is loaded
            if (itemMapCache == null) return; // If still null, can't proceed
        }

        lowStockItemsData.clear();
        lowStockItemsData.addAll(
            itemMapCache.values().stream()
                .filter(item -> {
                    try {
                        // Item model stores stock as String, needs parsing for comparison
                        int current = Integer.parseInt(item.getCurrentStock());
                        int min = Integer.parseInt(item.getMinimumStock());
                        return current < min;
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse stock for item " + item.getItemCode() + " for low stock check.");
                        return false; // Exclude items with invalid stock numbers from low stock list
                    }
                })
                .collect(Collectors.toList())
        );
        // lowStockTableView.setItems(lowStockItemsData); // Already bound
        lowStockTableView.refresh();
        System.out.println("Low stock items displayed. Count: " + lowStockItemsData.size());
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) { // Renamed
        if (itemMapCache == null || itemMapCache.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Data", "No item data loaded to generate a report.");
            return;
        }
        Gson gsonReport = new GsonBuilder().setPrettyPrinting().create();
        // Convert map values to a list for nicer JSON array output
        try (FileWriter writer = new FileWriter("stock_report.json")) { // Writes to project root
            gsonReport.toJson(new ArrayList<>(itemMapCache.values()), writer);
            showAlert(Alert.AlertType.INFORMATION, "Report Generated", "Stock report saved to stock_report.json in the project directory.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Report Error", "Could not generate stock report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateStockFromPOs(ActionEvent event) { // Renamed
        Map<String, PurchaseOrder> poMap;
        Map<String, Supplier> tempSupplierMap; // Needed for loadPOs

        try {
            // Load necessary data
             if (itemMapCache == null) { // Ensure items are loaded
                loadAllItemsData();
                if (itemMapCache == null) {
                    showAlert(Alert.AlertType.ERROR, "Data Error", "Item data not available.");
                    return;
                }
            }
            tempSupplierMap = com.talon.testing.models.Supplier.loadSuppliers(); // Assuming this static method exists and works
            poMap = PurchaseOrder.loadPOs(tempSupplierMap); // Assumes PurchaseOrder.loadPOs needs supplier map
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load POs or Suppliers: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        if (poMap == null || poMap.isEmpty()) {
             showAlert(Alert.AlertType.INFORMATION, "No POs", "No purchase orders found to process.");
            return;
        }


        List<PurchaseOrder> approvedPOsToProcess = poMap.values().stream()
            .filter(po -> po.isConsideredApprovedForPayment()) // Use a method from PO model
            .filter(po -> !"Received".equalsIgnoreCase(po.getStatus()) && !"Cancelled".equalsIgnoreCase(po.getStatus())) // Process only if not yet fully received or cancelled
            .collect(Collectors.toList());

        if (approvedPOsToProcess.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Action", "No approved POs found needing stock update (or they are already received/cancelled).");
            return;
        }

        boolean stockWasUpdated = false;
        boolean poStatusWasUpdated = false;

        for (PurchaseOrder po : approvedPOsToProcess) {
            if (po.getItems() == null) continue;

            for (POItem poItem : po.getItems()) {
                Item inventoryItem = itemMapCache.get(poItem.getItemCode());
                if (inventoryItem != null) {
                    try {
                        int currentInvStock = Integer.parseInt(inventoryItem.getCurrentStock());
                        int receivedQty = poItem.getQuantity(); // Quantity from this PO item
                        inventoryItem.setCurrentStock(String.valueOf(currentInvStock + receivedQty));
                        stockWasUpdated = true;
                        System.out.println("Stock updated for " + inventoryItem.getItemCode() + ": " + receivedQty + " units received. New stock: " + inventoryItem.getCurrentStock());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid stock format for item: " + inventoryItem.getItemCode() + ". Skipping stock update for this item.");
                    }
                } else {
                    System.err.println("Item " + poItem.getItemCode() + " from PO " + po.getPoId() + " not found in inventory master.");
                }
            }
            // After processing all items in a PO, update PO status
            po.setStatus("Received"); // Or "Partially Received" if you track that
            poStatusWasUpdated = true;
        }

        // Save changes if any were made
        try {
            if (stockWasUpdated) {
                Item.saveItems(itemMapCache);
            }
            if (poStatusWasUpdated) {
                PurchaseOrder.savePOs(poMap); // Save the entire PO map as statuses have changed
            }
            if (stockWasUpdated || poStatusWasUpdated) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Stock updated from received POs.");
                loadAllItemsData(); // Refresh item views
            } else {
                 showAlert(Alert.AlertType.INFORMATION, "No Change", "No applicable stock changes from POs.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Could not save updated stock or PO statuses: " + e.getMessage());
            e.printStackTrace();
            // Consider rollback logic here if needed
        }
    }
}