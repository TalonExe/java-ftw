package com.talon.testing.controllers;

import com.talon.testing.models.Supplier; // Your Supplier model
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent; // For navigation placeholders
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // IMPORT THIS
import javafx.scene.control.Alert; // For showing alerts
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL; // For Initializable
import java.util.Map;
import java.util.ResourceBundle; // For Initializable

public class ListSupplierController extends Switchable implements Initializable { // IMPLEMENT Initializable

    @FXML
    private TableView<Supplier> supplierTableView;

    @FXML
    private TableColumn<Supplier, String> supplierIdColumn;
    @FXML
    private TableColumn<Supplier, String> supplierNameColumn;
    @FXML
    private TableColumn<Supplier, String> contactPersonColumn;
    @FXML
    private TableColumn<Supplier, String> emailColumn;
    @FXML
    private TableColumn<Supplier, String> phoneNumberColumn;
    @FXML
    private TableColumn<Supplier, String> addressColumn;
    @FXML
    private TableColumn<Supplier, String> registrationDateColumn;

    // INSTANCE variable for the TableView's data
    private ObservableList<Supplier> supplierObservableListData = FXCollections.observableArrayList();

    @Override // ADDED @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { // Correct signature
        // Initialize table columns
        if (supplierIdColumn != null) supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        if (supplierNameColumn != null) supplierNameColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        if (contactPersonColumn != null) contactPersonColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        if (emailColumn != null) emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (phoneNumberColumn != null) phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber")); // Matches your Supplier model
        if (addressColumn != null) addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        if (registrationDateColumn != null) registrationDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));

        // Bind the instance ObservableList to the TableView
        if (supplierTableView != null) {
            supplierTableView.setItems(supplierObservableListData);
        }

        loadSupplierData(); // Load data into the instance list
    }

    public void loadSupplierData() {
        try {
            Map<String, Supplier> suppliersMap = Supplier.loadSuppliers(); // Assumes static loadSuppliers in your model
            this.supplierObservableListData.clear(); // Clear existing data from the instance list
            if (suppliersMap != null) {
                this.supplierObservableListData.addAll(suppliersMap.values()); // Add new data
            }
            // TableView updates automatically because it's bound to supplierObservableListData
            // if (supplierTableView != null) supplierTableView.refresh(); // Usually not needed
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load supplier data: " + e.getMessage());
            e.printStackTrace();
        }
    }

}