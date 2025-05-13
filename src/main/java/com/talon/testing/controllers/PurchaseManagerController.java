package com.talon.testing.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class PurchaseManagerController {

    @FXML
    private AnchorPane contentArea;
    @FXML
    private TableView<Supplier> suppliersTableView; // Add TableView for suppliers
    @FXML
    private TableColumn<Supplier, String> supplierId;
    @FXML
    private TableColumn<Supplier, String> supplierName;
    @FXML
    private TableColumn<Supplier, String> contactPerson;
    @FXML
    private TableColumn<Supplier, String> email;
    @FXML
    private TableColumn<Supplier, String> phoneNumber;
    @FXML
    private TableColumn<Supplier, String> address;
    @FXML
    private TableColumn<Supplier, String> registrationDate;

    @FXML
    public void initialize() {
        // Initialization (if needed)
    }

    public void showItemList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/talon/testing/listItemView.fxml"));
            loader.setController(this);
            AnchorPane listItemView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(listItemView);

            AnchorPane.setTopAnchor(listItemView, 0.0);
            AnchorPane.setBottomAnchor(listItemView, 0.0);
            AnchorPane.setLeftAnchor(listItemView, 0.0);
            AnchorPane.setRightAnchor(listItemView, 0.0);

            // Initialize the table with data (You'll need to adapt this to your Item class)
            // initializeItemListData(); //  Adapt this
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error
        }
    }

    public void showSupplierList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/talon/testing/listSupplierView.fxml"));
            loader.setController(this); // Set the controller
            AnchorPane listSupplierView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(listSupplierView);

            AnchorPane.setTopAnchor(listSupplierView, 0.0);
            AnchorPane.setBottomAnchor(listSupplierView, 0.0);
            AnchorPane.setLeftAnchor(listSupplierView, 0.0);
            AnchorPane.setRightAnchor(listSupplierView, 0.0);

            // Initialize the supplier table with data
            initializeSupplierListData();

        } catch (IOException e) {
            e.printStackTrace();
            // Handle error
        }
    }

    private void initializeSupplierListData() {
        if (suppliersTableView == null) {
            System.err.println("suppliersTableView is null. FXML injection failed.");
            return;
        }

        ObservableList<Supplier> suppliers = FXCollections.observableArrayList();
        String filePath = new File(System.getProperty("user.dir"), "target/classes/data/suppliers.txt").getAbsolutePath();
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("Error: suppliers.txt file not found at: " + filePath);
            suppliersTableView.setItems(FXCollections.emptyObservableList());
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            StringBuilder fileContent = new StringBuilder();
            while ((line = br.readLine()) != null) {
                fileContent.append(line);
            }
            String jsonContent = fileContent.toString();

            // Remove the outermost curly braces to get individual supplier entries
            if (jsonContent.startsWith("{") && jsonContent.endsWith("}")) {
                jsonContent = jsonContent.substring(1, jsonContent.length() - 1);
            }

            // Split the JSON string into key-value pairs
            String[] entries = jsonContent.split("\\},\\s*\"");  // Split by "}," and next key
            for (String entry : entries) {
                entry = entry.replace("\"", ""); //remove "
                if(!entry.contains(":")) continue;
                String key = entry.substring(0, entry.indexOf(":")).trim();
                String value = entry.substring(entry.indexOf(":") + 1).trim();
                if(value.endsWith("}"))
                    value = value.substring(0,value.length()-1);

                String supplierId = null, supplierName = null, contactPerson = null, email = null, phoneNumber = null, address = null, registrationDate = null;
                String[] records = value.split(",\\s*");

                for(String record : records){
                    String[] parts = record.split(":");
                    if(parts.length == 2){
                        String recordKey = parts[0].trim();
                        String recordValue = parts[1].trim();
                        if(recordKey.contains("supplierId")) supplierId = recordValue;
                        else if(recordKey.contains("supplierName")) supplierName = recordValue;
                        else if(recordKey.contains("contactPerson")) contactPerson = recordValue;
                        else if(recordKey.contains("email")) email = recordValue;
                        else if(recordKey.contains("phoneNumber"))  phoneNumber = recordValue;
                        else if(recordKey.contains("address")) address = recordValue;
                        else if(recordKey.contains("registrationDate")) registrationDate = recordValue;
                    }
                }
                if (supplierId != null && supplierName != null && contactPerson != null && email != null && phoneNumber != null && address != null && registrationDate != null) {
                    Supplier supplier = new Supplier(supplierId, supplierName, contactPerson, email, phoneNumber, address, registrationDate);
                    suppliers.add(supplier);
                }
                else{
                    System.err.println("Skipping incomplete record for supplier: " + key );
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading suppliers.txt: " + e.getMessage());
            suppliersTableView.setItems(FXCollections.emptyObservableList());
            return;
        }

        suppliersTableView.setItems(suppliers);
        supplierId.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        supplierName.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        contactPerson.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        address.setCellValueFactory(new PropertyValueFactory<>("address"));
        registrationDate.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
    }

    public void showDisplayRequisition() {
        System.out.println("Display Requisition clicked");
        loadView("/com/talon/testing/displayRequisitionView.fxml");
    }

    public void showGeneratePo() {
        System.out.println("Generate Purchase Order clicked");
        loadView("/com/talon/testing/generatePoView.fxml");
    }

    public void showListPo() {
        System.out.println("List Purchase Orders clicked");
        loadView("/com/talon/testing/listPoView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error
        }
    }
    public static class Supplier {
        private String supplierId;
        private String supplierName;
        private String contactPerson;
        private String email;
        private String phoneNumber;
        private String address;
        private String registrationDate;

        public Supplier(String supplierId, String supplierName, String contactPerson, String email, String phoneNumber, String address, String registrationDate) {
            this.supplierId = supplierId;
            this.supplierName = supplierName;
            this.contactPerson = contactPerson;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.address = address;
            this.registrationDate = registrationDate;
        }

        public String getSupplierId() {
            return supplierId;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public String getContactPerson() {
            return contactPerson;
        }

        public String getEmail() {
            return email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getAddress() {
            return address;
        }

        public String getRegistrationDate() {
            return registrationDate;
        }
    }
}

