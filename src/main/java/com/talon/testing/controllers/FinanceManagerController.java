package com.talon.testing.controllers;

import com.talon.testing.models.DailySales;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.io.*;
import java.util.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

public class FinanceManagerController {
    
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
    private final File poFile = new File("PO.txt");
    
    
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
    
    //*****************************************************************//
    //******************* PR PAGE ******************************//
    //*****************************************************************//    


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
    
    
    private void loadPOs() {
        poList.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(poFile))) {
            String line;
            PO currentPO = null;

            while ((line = reader.readLine()) != null) {
                if (line.equals("END")) {
                    if (currentPO != null) {
                        poList.add(currentPO);
                        currentPO = null;
                    }
                } else if (line.contains("|")) {
                    String[] parts = line.split("\\|");
                    currentPO = new PO();
                    currentPO.purchaseOrderId = parts[0];
                    currentPO.purchaseRequisitionId = parts[1];
                    currentPO.status = parts[2];
                    currentPO.financeManagerApproved = Boolean.parseBoolean(parts[3]);
                    currentPO.items = new ArrayList<>();
                } else {
                    if (currentPO != null) {
                        String[] parts = line.split(",");
                        POItem item = new POItem();
                        item.itemCode = parts[0];
                        item.quantity = parts[1];
                        item.supplierId = parts[2];
                        currentPO.items.add(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load POs: " + e.getMessage());
        }
    }

    private void savePOs() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(poFile))) {
            for (PO po : poList) {
                writer.write(po.purchaseOrderId + "|" + po.purchaseRequisitionId + "|" + po.status + "|" + po.financeManagerApproved);
                writer.newLine();
                for (POItem item : po.items) {
                    writer.write(item.itemCode + "," + item.quantity + "," + item.supplierId);
                    writer.newLine();
                }
                writer.write("END");
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save POs: " + e.getMessage());
        }
    }

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
    }

    public static class POItem {
        String itemCode;
        String quantity;
        String supplierId;
    }
    
    //*****************************************************************//
    //*************************GENERATE REPORT*************************// 
    //*****************************************************************//
    
    
    private void loadSalesData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("sales.txt"))) {
            String line;
            StringBuilder jsonContent = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line.trim());
            }
            
            // Simple parsing (since we're not using JSON library)
            String content = jsonContent.toString();
            content = content.substring(content.indexOf('{') + 1, content.lastIndexOf('}'));
            
            // Split into individual sales records
            String[] salesRecords = content.split("(?=\\s*\"SLS\\d+\":\\s*\\{)");
            
            for (String record : salesRecords) {
                if (record.trim().isEmpty()) continue;
                
                // Extract fields from each record
                String salesId = extractField(record, "salesId");
                String itemCode = extractField(record, "itemCode");
                int quantitySold = Integer.parseInt(extractField(record, "quantitySold"));
                String salesDate = extractField(record, "salesDate");
                String salesManagerId = extractField(record, "salesManagerId");
                
                salesDataList.add(new DailySales(salesId, itemCode, quantitySold, salesDate, salesManagerId));
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load sales data: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    private String extractField(String record, String fieldName) {
        int startIndex = record.indexOf("\"" + fieldName + "\":") + fieldName.length() + 3;
        if (startIndex < fieldName.length() + 3) return "";
        
        String remaining = record.substring(startIndex).trim();
        if (remaining.startsWith("\"")) {
            // String value
            int endIndex = remaining.indexOf("\"", 1);
            return remaining.substring(1, endIndex);
        } else {
            // Numeric value
            int endIndex = remaining.indexOf(",");
            if (endIndex == -1) endIndex = remaining.indexOf("}");
            return remaining.substring(0, endIndex).trim();
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
    
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    //***********************************************************************//
    //************************ VIEW PR *************************************//
    //********************************************************************//

}
