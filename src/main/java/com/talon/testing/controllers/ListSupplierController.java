package com.talon.testing.controllers;

import com.talon.testing.models.Supplier;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.Map;
import javafx.collections.FXCollections;

public class ListSupplierController {

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

    public void initialize() {
        // Initialize table columns
        supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        supplierNameColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        contactPersonColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        registrationDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
    }

    public void loadSupplierData() throws IOException {
        Map<String, Supplier> suppliers = Supplier.loadSuppliers();
        ObservableList<Supplier> supplierData = FXCollections.observableArrayList();
        supplierData.setAll(suppliers.values());
        supplierTableView.setItems(supplierData);
    }
}