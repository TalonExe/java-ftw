package com.talon.testing.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.talon.testing.models.DailySales;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class FinanceManagerController {
    
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    //*****************************************************************//
    //*************************PO APPROVAL*****************************// 
    //*****************************************************************//
    
    @FXML private TableView<PO> poTable;
    @FXML private TableColumn<PO, String> poIdCol;
    @FXML private TableColumn<PO, String> prIdCol;
    @FXML private TableColumn<PO, String> statusCol;
    @FXML private TableColumn<PO, String> approvedCol;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private TextField itemCodeField;
    @FXML private TextField quantityField;
    @FXML private Button updateQuantityButton;

    private final ObservableList<PO> poList = FXCollections.observableArrayList();
    private final File poFile = new File("PO.json");
    
    
    //*****************************************************************//
    //*******************Generate Report ******************************//
    //*****************************************************************//
    
    @FXML private TableView<DailySales> salesTableView;
    @FXML private TableColumn<DailySales, String> salesIdColumn;
    @FXML private TableColumn<DailySales, String> itemCodeColumn;
    @FXML private TableColumn<DailySales, Integer> quantitySoldColumn;
    @FXML private TableColumn<DailySales, String> salesDateColumn;
    @FXML private TableColumn<DailySales, String> managerIdColumn;
    
    private final ObservableList<DailySales> salesDataList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        //*****************************************************************//
        //*************************PO APPROVAL*****************************// 
        //*****************************************************************//
        
        poIdCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().purchaseOrderId));
        prIdCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().purchaseRequisitionId));
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().status));
        approvedCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().financeManagerApproved ? "Yes" : "No"));

        loadPOs();
        poTable.setItems(poList);

        approveButton.setOnAction(e -> updatePOStatus("Approved"));
        rejectButton.setOnAction(e -> updatePOStatus("Rejected"));
        updateQuantityButton.setOnAction(e -> updateItemQuantity());
        
        
       //*****************************************************************//
       //*******************Generate Report ******************************//
       //*****************************************************************//
     
        salesIdColumn.setCellValueFactory(new PropertyValueFactory<>("salesId"));
        itemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        quantitySoldColumn.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        salesDateColumn.setCellValueFactory(new PropertyValueFactory<>("salesDate"));
        managerIdColumn.setCellValueFactory(new PropertyValueFactory<>("salesManagerId"));
        
        // Load data from file
        loadSalesData();
        
        // Add data to table
        salesTableView.setItems(salesDataList);
    }
    
    //*****************************************************************//
    //*************************PO APPROVAL*****************************// 
    //*****************************************************************//
    @FXML
    private void loadPOs() {
        poList.clear();
        if (!poFile.exists()) {
            return;
        }

        try (Reader reader = new FileReader(poFile)) {
            Type poListType = new TypeToken<List<PO>>() {}.getType();
            List<PO> loadedPOs = gson.fromJson(reader, poListType);
            if (loadedPOs != null) {
                poList.addAll(loadedPOs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load POs: " + e.getMessage());
        }
    }
    @FXML
    private void savePOs() {
        try (Writer writer = new FileWriter(poFile)) {
            gson.toJson(poList.stream().collect(Collectors.toList()), writer);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save POs: " + e.getMessage());
        }
    }
    @FXML
    private void updatePOStatus(String newStatus) {
        PO selected = poTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a PO first");
            return;
        }

        selected.status = newStatus;
        selected.financeManagerApproved = newStatus.equals("Approved");

        savePOs();
        poTable.refresh();
        showAlert("Success", "PO status updated to: " + newStatus);
    }
    @FXML
    private void updateItemQuantity() {
        PO selected = poTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a PO first");
            return;
        }

        String itemCode = itemCodeField.getText().trim();
        String newQuantity = quantityField.getText().trim();

        if (itemCode.isEmpty() || newQuantity.isEmpty()) {
            showAlert("Invalid Input", "Please enter both item code and quantity");
            return;
        }

        boolean itemFound = false;
        for (POItem item : selected.items) {
            if (item.itemCode.equals(itemCode)) {
                item.quantity = newQuantity;
                itemFound = true;
                break;
            }
        }

        if (!itemFound) {
            showAlert("Item Not Found", "No item with code " + itemCode + " found in selected PO");
            return;
        }

        savePOs();
        showAlert("Success", "Quantity updated for item " + itemCode + " to " + newQuantity);
        itemCodeField.clear();
        quantityField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class PO {
        String purchaseOrderId;
        String purchaseRequisitionId;
        String status;
        boolean financeManagerApproved;
        List<POItem> items;
        
        // Empty constructor for GSON
        public PO() {
            items = new ArrayList<>();
        }
    }

    public static class POItem {
        String itemCode;
        String quantity;
        String supplierId;
    }
    
    //*****************************************************************//
    //*************************GENERATE REPORT*************************// 
    //*****************************************************************//
    @FXML
    private void loadSalesData() {
        File salesFile = new File("sales.json");
        if (!salesFile.exists()) {
            return;
        }

        try (Reader reader = new FileReader(salesFile)) {
            Type salesListType = new TypeToken<List<DailySales>>() {}.getType();
            List<DailySales> loadedSales = gson.fromJson(reader, salesListType);
            if (loadedSales != null) {
                salesDataList.addAll(loadedSales);
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load sales data: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    @FXML
    private void generatePDFReport() {
        try {
            // Create a simple PDF report (using basic file operations)
            String fileName = "FinanceReport_" + System.currentTimeMillis() + ".txt";
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                writer.println("Finance Report");
                writer.println("Generated on: " + new Date());
                writer.println("====================================");
                writer.printf("%-10s %-10s %-15s %-12s %-10s%n", 
                    "Sales ID", "Item Code", "Quantity Sold", "Date", "Manager ID");
                
                for (DailySales data : salesDataList) {
                    writer.printf("%-10s %-10s %-15d %-12s %-10s%n", 
                        data.getSalesId(), 
                        data.getItemCode(), 
                        data.getQuantitySold(), 
                        data.getSalesDate(), 
                        data.getSalesManagerId());
                }
                
                writer.println("====================================");
                writer.println("Total Records: " + salesDataList.size());
            }
            
            showAlert("Success", "Report generated as " + fileName, AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Error", "Failed to generate report: " + e.getMessage(), AlertType.ERROR);
        }
    }
    @FXML
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}