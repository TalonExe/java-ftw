package com.talon.testing.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.talon.testing.models.Item;
import com.talon.testing.models.POItem;
import com.talon.testing.models.PurchaseOrder;
import com.talon.testing.models.StockTransaction; // MAKE SURE THIS IS IMPORTED
import com.talon.testing.models.Supplier;       // MAKE SURE THIS IS IMPORTED
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
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

public class StockManagementController extends Switchable implements Initializable {

    // For "All Items" Tab
    @FXML private TableView<Item> itemTableView;
    @FXML private TableColumn<Item, String> itemCodeColumn;
    @FXML private TableColumn<Item, String> itemNameColumn;
    @FXML private TableColumn<Item, String> descriptionColumn;
    @FXML private TableColumn<Item, String> unitPriceColumn;
    @FXML private TableColumn<Item, String> currentStockColumn;
    @FXML private TableColumn<Item, String> minimumStockColumn;
    @FXML private TableColumn<Item, String> itemSupplierIdColumn;
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
    private Map<String, Item> itemMapCache;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureAllItemsTable();
        configureLowStockItemsTable();
        loadAllItemsData();
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
    public void loadAllItemsData() {
        try {
            itemMapCache = Item.loadItems();
            if (itemMapCache == null) itemMapCache = new HashMap<>();
            allItemsData.setAll(itemMapCache.values());
            itemTableView.refresh();
            System.out.println("All items loaded. Count: " + allItemsData.size());
            handleViewLowStock(null);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load items: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateStock(ActionEvent event) {
        Item currentSelectedItem = itemTableView.getSelectionModel().getSelectedItem();
        if (currentSelectedItem == null) {
            currentSelectedItem = lowStockTableView.getSelectionModel().getSelectedItem();
        }
        final Item itemToUpdateInLambda = currentSelectedItem;

        if (itemToUpdateInLambda != null) {
            TextInputDialog dialog = new TextInputDialog(itemToUpdateInLambda.getCurrentStock());
            dialog.setTitle("Update Stock");
            dialog.setHeaderText("Update stock for: " + itemToUpdateInLambda.getItemName());
            dialog.setContentText("Enter new current stock value:");
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(newStockStr -> {
                try {
                    int newStockVal = Integer.parseInt(newStockStr);
                    if (newStockVal < 0) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input", "Stock cannot be negative.");
                        return;
                    }
                    Item itemInCache = itemMapCache.get(itemToUpdateInLambda.getItemCode());
                    if (itemInCache != null) {
                        itemInCache.setCurrentStock(newStockStr);
                        // Log this manual adjustment as a stock transaction
                        StockTransaction manualAdj = new StockTransaction(
                                itemInCache.getItemCode(),
                                itemInCache.getItemName(),
                                null, // No PO for manual adjustment
                                "MANUAL_ADJUSTMENT",
                                newStockVal - Integer.parseInt(itemToUpdateInLambda.getCurrentStock()), // Calculate change
                                newStockVal, // Stock level after
                                "InventoryManager_Manual" // User
                        );
                        StockTransaction.addTransaction(manualAdj);
                        Item.saveItems(itemMapCache);
                        showAlert(Alert.AlertType.INFORMATION, "Stock Updated", "Stock for " + itemToUpdateInLambda.getItemName() + " updated to " + newStockStr);
                        loadAllItemsData();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Data Sync Error", "Selected item not found in cache for update.");
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number for stock.");
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Save Error", "Could not save updated stock or transaction: " + e.getMessage());
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to update stock.");
        }
    }

    @FXML
    private void handleViewLowStock(ActionEvent event) {
        if (itemMapCache == null) {
            loadAllItemsData();
            if (itemMapCache == null) return;
        }
        lowStockItemsData.clear();
        lowStockItemsData.addAll(
            itemMapCache.values().stream()
                .filter(item -> {
                    try {
                        int current = Integer.parseInt(item.getCurrentStock());
                        int min = Integer.parseInt(item.getMinimumStock());
                        return current < min;
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse stock for item " + item.getItemCode() + " for low stock check.");
                        return false;
                    }
                })
                .collect(Collectors.toList())
        );
        lowStockTableView.refresh();
        System.out.println("Low stock items displayed. Count: " + lowStockItemsData.size());
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        if (itemMapCache == null || itemMapCache.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Data", "No item data loaded to generate a report.");
            return;
        }
        Gson gsonReport = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("stock_report.json")) {
            gsonReport.toJson(new ArrayList<>(itemMapCache.values()), writer);
            showAlert(Alert.AlertType.INFORMATION, "Report Generated", "Stock report saved to stock_report.json in the project directory.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Report Error", "Could not generate stock report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateStockFromPOs(ActionEvent event) {
        Map<String, PurchaseOrder> poMap;
        Map<String, Supplier> suppliersMapForPOLoad; // Explicitly for loading POs

        try {
            if (itemMapCache == null) {
                loadAllItemsData();
                if (itemMapCache == null) {
                    showAlert(Alert.AlertType.ERROR, "Data Error", "Item data not available.");
                    return;
                }
            }
            // Load suppliers specifically for the PO loading process, as PO.loadPOs requires it
            suppliersMapForPOLoad = Supplier.loadSuppliers();
            if (suppliersMapForPOLoad == null) suppliersMapForPOLoad = new HashMap<>();

            poMap = PurchaseOrder.loadPOs(suppliersMapForPOLoad); // Pass the loaded suppliers
        } catch (IOException e) { // Catch IOException specifically
            showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load POs or Suppliers: " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (Exception e) { // Catch other potential exceptions during loading
            showAlert(Alert.AlertType.ERROR, "Unexpected Load Error", "An unexpected error occurred while loading data: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (poMap == null || poMap.isEmpty()) {
             showAlert(Alert.AlertType.INFORMATION, "No POs", "No purchase orders found to process.");
            return;
        }

        List<PurchaseOrder> approvedPOsToProcess = poMap.values().stream()
            .filter(po -> po.isConsideredApprovedForPayment()) // Ensure PO model has this
            .filter(po -> !"Received".equalsIgnoreCase(po.getStatus()) && !"Cancelled".equalsIgnoreCase(po.getStatus()))
            .collect(Collectors.toList());

        if (approvedPOsToProcess.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Action", "No approved POs found needing stock update (or they are already received/cancelled).");
            return;
        }

        boolean overallStockUpdated = false;
        boolean overallPoStatusUpdated = false;

        for (PurchaseOrder po : approvedPOsToProcess) {
            if (po.getItems() == null || po.getItems().isEmpty()) {
                System.out.println("PO " + po.getPoId() + " has no items. Skipping.");
                continue;
            }

            boolean currentPoItemsProcessedSuccessfully = true;
            for (POItem poItem : po.getItems()) {
                Item inventoryItem = itemMapCache.get(poItem.getItemCode());
                if (inventoryItem != null) {
                    try {
                        int stockBeforeReceipt = Integer.parseInt(inventoryItem.getCurrentStock());
                        int receivedQty = poItem.getQuantity();
                        int stockAfterReceipt = stockBeforeReceipt + receivedQty;

                        inventoryItem.setCurrentStock(String.valueOf(stockAfterReceipt));
                        overallStockUpdated = true;

                        // Create and save a stock transaction record
                        StockTransaction transaction = new StockTransaction(
                                inventoryItem.getItemCode(),
                                inventoryItem.getItemName(), // Get item name for display
                                po.getPoId(),               // Link to the PO
                                "PO_RECEIPT",               // Transaction type
                                receivedQty,                // Quantity changed (positive)
                                stockAfterReceipt,          // Stock level after this transaction
                                "InventoryMgmt_POReceive"   // User ID or system process
                        );
                        StockTransaction.addTransaction(transaction); // Add and save transaction

                        System.out.println("Stock updated for " + inventoryItem.getItemCode() + " from PO " + po.getPoId() +
                                           ": +" + receivedQty + ". New stock: " + stockAfterReceipt);

                    } catch (NumberFormatException e) {
                        System.err.println("Invalid stock format for item: " + inventoryItem.getItemCode() + ". Skipping stock update for this PO item.");
                        currentPoItemsProcessedSuccessfully = false; // Mark this PO item as failed
                    } catch (IOException e) {
                        System.err.println("Failed to log stock transaction for item " + inventoryItem.getItemCode() + " from PO " + po.getPoId() + ": " + e.getMessage());
                        currentPoItemsProcessedSuccessfully = false; // Mark as failed
                        // Decide if this failure should prevent the stock update or just be logged
                    }
                } else {
                    System.err.println("Item " + poItem.getItemCode() + " from PO " + po.getPoId() + " not found in inventory master. Cannot update stock.");
                    currentPoItemsProcessedSuccessfully = false; // Mark as failed
                }
            } // End of POItem loop

            // Only update PO status if all its items were processed for stock successfully (or if partial receipt is allowed)
            if (currentPoItemsProcessedSuccessfully) { // Or more complex logic for partial receipts
                po.setStatus("Received");
                overallPoStatusUpdated = true;
            } else {
                System.err.println("PO " + po.getPoId() + " stock update encountered issues. PO status not changed to 'Received'.");
                // You might set a "Partially Received" or "Receipt Error" status here if desired
                // po.setStatus("Receipt Error");
                // overallPoStatusUpdated = true; // If you change status to an error state
            }
        } // End of PurchaseOrder loop

        // Save changes if any were made
        try {
            if (overallStockUpdated) {
                Item.saveItems(itemMapCache);
            }
            if (overallPoStatusUpdated) {
                PurchaseOrder.savePOs(poMap);
            }
            if (overallStockUpdated || overallPoStatusUpdated) {
                showAlert(Alert.AlertType.INFORMATION, "Process Complete", "Stock update from POs processed. Check console for details.");
                loadAllItemsData(); // Refresh item views to show new stock levels
            } else {
                 showAlert(Alert.AlertType.INFORMATION, "No Change", "No stock changes were made from POs, or issues occurred.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Could not save updated stock or PO statuses: " + e.getMessage());
            e.printStackTrace();
        }
    }
}