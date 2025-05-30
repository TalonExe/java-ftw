package com.talon.testing.controllers;

import com.talon.testing.models.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.HashMap; // Import HashMap
import java.util.ResourceBundle;

public class SupplierEntryController extends Switchable implements Initializable {

    // FXML TableView and Columns
    @FXML private TableView<Supplier> supplierTableView;
    @FXML private TableColumn<Supplier, String> supplierIdCol;
    @FXML private TableColumn<Supplier, String> supplierNameCol;
    @FXML private TableColumn<Supplier, String> contactPersonCol;
    @FXML private TableColumn<Supplier, String> emailCol;
    @FXML private TableColumn<Supplier, String> phoneCol; // Matches FXML fx:id
    @FXML private TableColumn<Supplier, String> addressCol;
    @FXML private TableColumn<Supplier, String> regDateCol; // Matches FXML fx:id

    // FXML Form TextFields
    @FXML private TextField supplierIdField;
    @FXML private TextField supplierNameField;
    @FXML private TextField contactPersonField;
    @FXML private TextField emailField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField addressField;
    @FXML private TextField registrationDateField;

    // FXML Buttons
    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    // Data
    private ObservableList<Supplier> supplierData = FXCollections.observableArrayList();
    private Map<String, Supplier> supplierMapCache; // Cache for loaded suppliers
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureSupplierTable();
        loadSupplierData();

        supplierTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    populateForm(newSelection);
                } else {
                    handleClearFormAction(null);
                }
            });

        // Button actions are set in FXML via onAction attributes
    }

    private void configureSupplierTable() {
        supplierIdCol.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        supplierNameCol.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        contactPersonCol.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber")); // Maps to getPhoneNumber()
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        regDateCol.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        supplierTableView.setItems(supplierData);
    }

    private void loadSupplierData() {
        try {
            this.supplierMapCache = Supplier.loadSuppliers(); // From your Supplier model
            supplierData.clear();
            if (this.supplierMapCache != null) {
                supplierData.addAll(this.supplierMapCache.values());
            }
            supplierTableView.refresh(); // Ensure table updates
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load supplier data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean saveSupplierData() {
        try {
            if (this.supplierMapCache == null) this.supplierMapCache = new HashMap<>(); // Should not happen if loaded
            // Ensure supplierMapCache reflects changes from supplierData (if any direct list manipulation happened)
            // For CRUD via form, updating supplierMapCache directly is usually sufficient.
            // If adding directly to supplierData then removing, then map needs full rebuild:
            // this.supplierMapCache.clear();
            // for (Supplier s : supplierData) { this.supplierMapCache.put(s.getSupplierId(), s); }

            Supplier.saveSuppliers(this.supplierMapCache); // From your Supplier model
            return true;
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Could not save supplier data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void populateForm(Supplier supplier) {
        supplierIdField.setText(supplier.getSupplierId());
        supplierNameField.setText(supplier.getSupplierName());
        contactPersonField.setText(supplier.getContactPerson());
        emailField.setText(supplier.getEmail());
        phoneNumberField.setText(supplier.getPhoneNumber());
        addressField.setText(supplier.getAddress());
        registrationDateField.setText(supplier.getRegistrationDate());
        supplierIdField.setEditable(false); // Don't edit PK
    }

    @FXML
    private void handleAddSupplierAction(ActionEvent event) {
        if (!validateSupplierInput()) return;

        String id = supplierIdField.getText().trim();
        String name = supplierNameField.getText().trim();
        String contact = contactPersonField.getText().trim();
        String emailVal = emailField.getText().trim(); // Renamed to avoid conflict
        String phone = phoneNumberField.getText().trim();
        String addressVal = addressField.getText().trim(); // Renamed
        String regDate = registrationDateField.getText().trim();

        // Validate date format for registrationDate
        try {
            if (!regDate.isEmpty()) LocalDate.parse(regDate, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid Registration Date format. Use YYYY-MM-DD or leave empty.");
            return;
        }


        Supplier selectedSupplier = supplierTableView.getSelectionModel().getSelectedItem();

        if (this.supplierMapCache == null) this.supplierMapCache = new HashMap<>(); // Safety init

        if (supplierIdField.isEditable() == false && selectedSupplier != null && selectedSupplier.getSupplierId().equals(id)) { // UPDATE mode
            selectedSupplier.setSupplierName(name);
            selectedSupplier.setContactPerson(contact);
            selectedSupplier.setEmail(emailVal);
            selectedSupplier.setPhoneNumber(phone);
            selectedSupplier.setAddress(addressVal);
            selectedSupplier.setRegistrationDate(regDate);

            supplierData.set(supplierData.indexOf(selectedSupplier), selectedSupplier); // Refresh item in list
            // No need to update map key, but ensure object in map is the same instance or has same values
            this.supplierMapCache.put(id, selectedSupplier);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier '" + id + "' updated.");
        } else { // ADD mode
            if (this.supplierMapCache.containsKey(id)) {
                showAlert(Alert.AlertType.ERROR, "Duplicate ID", "Supplier ID '" + id + "' already exists.");
                return;
            }
            Supplier newSupplier = new Supplier(id, name, contact, emailVal, phone, addressVal, regDate);
            this.supplierMapCache.put(id, newSupplier);
            supplierData.add(newSupplier); // Add to observable list
            showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier '" + id + "' added.");
        }

        if (saveSupplierData()) {
            handleClearFormAction(null);
        } else {
            loadSupplierData(); // Revert to last saved state if save failed
        }
    }

    @FXML
    private void handleDeleteSupplierAction(ActionEvent event) {
        Supplier selectedSupplier = supplierTableView.getSelectionModel().getSelectedItem();
        if (selectedSupplier == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a supplier to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Delete supplier '" + selectedSupplier.getSupplierName() + "'?", ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (this.supplierMapCache != null) this.supplierMapCache.remove(selectedSupplier.getSupplierId());
                supplierData.remove(selectedSupplier); // This updates the TableView

                if (saveSupplierData()) {
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Supplier '" + selectedSupplier.getSupplierName() + "' deleted.");
                    handleClearFormAction(null);
                } else {
                    loadSupplierData(); // Revert if save failed
                }
            }
        });
    }

    @FXML
    private void handleClearFormAction(ActionEvent event) {
        supplierIdField.clear();
        supplierNameField.clear();
        contactPersonField.clear();
        emailField.clear();
        phoneNumberField.clear();
        addressField.clear();
        registrationDateField.clear();
        supplierTableView.getSelectionModel().clearSelection();
        supplierIdField.setEditable(true); // Allow editing for new entry
        registrationDateField.setPromptText("YYYY-MM-DD"); // Reset prompt
    }

    private boolean validateSupplierInput() {
        String id = supplierIdField.getText().trim();
        String name = supplierNameField.getText().trim();
        String emailVal = emailField.getText().trim(); // Basic check
        String regDateStr = registrationDateField.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Supplier ID and Name are required.");
            return false;
        }
        // Basic email validation (can be more sophisticated)
        if (!emailVal.isEmpty() && !emailVal.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid email format.");
            return false;
        }
        if (!regDateStr.isEmpty()) {
            try {
                LocalDate.parse(regDateStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid Registration Date. Use YYYY-MM-DD or leave empty.");
                return false;
            }
        }
        return true;
    }
}