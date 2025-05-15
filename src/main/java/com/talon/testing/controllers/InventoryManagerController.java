package com.talon.testing.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.talon.testing.models.InventoryManagerModel;
import com.talon.testing.models.InventoryManagerModel.Item;
import com.talon.testing.models.PurchaseOrder;
import com.talon.testing.models.PurchaseOrderModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class InventoryManagerController {

    @FXML
    private TableView<Item> itemTableView;
    @FXML
    private TableColumn<Item, String> itemCodeColumn;
    @FXML
    private TableColumn<Item, String> itemNameColumn;
    @FXML
    private TableColumn<Item, String> currentStockColumn;
    @FXML
    private TableColumn<Item, String> minimumStockColumn;
    @FXML
    private TableColumn<Item, String> descriptionColumn;
    @FXML
    private TableColumn<Item, String> unitPriceColumn;
    @FXML
    private TableColumn<Item, String> createDateColumn;

    private InventoryManagerModel model;
    private ObservableList<Item> itemList;

    public InventoryManagerController() {
        model = new InventoryManagerModel();
        itemList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        itemCodeColumn.setCellValueFactory(cellData -> cellData.getValue().itemCodeProperty());
        itemNameColumn.setCellValueFactory(cellData -> cellData.getValue().itemNameProperty());
        currentStockColumn.setCellValueFactory(cellData -> cellData.getValue().currentStockProperty());
        minimumStockColumn.setCellValueFactory(cellData -> cellData.getValue().minimumStockProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        unitPriceColumn.setCellValueFactory(cellData -> cellData.getValue().unitPriceProperty());
        createDateColumn.setCellValueFactory(cellData -> cellData.getValue().createDateProperty());

        loadItems();
    }

    private void loadItems() {
        itemList.clear();
        itemList.addAll(model.loadItems());
        itemTableView.setItems(itemList);
    }

    @FXML
    private void updateStock() {
        Item selectedItem = itemTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                int updatedStock = Integer.parseInt(selectedItem.getCurrentStock()) + 10;
                model.updateStock(selectedItem.getItemCode(), String.valueOf(updatedStock));
                loadItems();
            } catch (NumberFormatException e) {
                showAlert("Invalid Stock Value", "Please ensure the stock value is a valid number.");
            }
        } else {
            showAlert("No Selection", "Please select an item to update the stock.");
        }
    }

    @FXML
    private void viewLowStock() {
        itemList.clear();
        itemList.addAll(model.getLowStockItems());
        itemTableView.setItems(itemList);
    }

    @FXML
    private void generateStockReport() {
        List<Item> items = model.loadItems();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("stock_report.json")) {
            gson.toJson(items, writer);
            showAlert("Report Generated", "Stock report saved to stock_report.json");
        } catch (IOException e) {
            showAlert("Error", "Could not generate stock report.");
        }
    }

    @FXML
    private void updateStockFromApprovedPOs() {
        PurchaseOrderModel poModel = new PurchaseOrderModel();
        List<PurchaseOrder> approvedPOs = poModel.getApprovedOrders();
        List<Item> items = model.loadItems();

        boolean updated = false;

        for (PurchaseOrder po : approvedPOs) {
            for (Item item : items) {
                if (item.getItemCode().equalsIgnoreCase(po.getItemCode())) {
                    try {
                        int current = Integer.parseInt(item.getCurrentStock());
                        item.setCurrentStock(String.valueOf(current + po.getQuantity()));
                        po.setStatus("Received");
                        updated = true;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid stock for item: " + item.getItemCode());
                    }
                }
            }
        }

        if (updated) {
            model.saveItems(items);
            poModel.savePurchaseOrders(approvedPOs);
            showAlert("Success", "Stock updated from approved POs.");
            loadItems();
        } else {
            showAlert("Info", "No stock changes made.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
