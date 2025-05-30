package com.talon.testing.controllers;

import com.talon.testing.models.PurchaseRequisition; // Import your PR model
import com.talon.testing.controllers.FinanceManagerController;
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
import java.util.ResourceBundle;

public class CreatePRController extends Switchable { // Or your specific controller name

    // --- FXML Elements for "Create PR" section ---
    @FXML private TableView<PurchaseRequisition> poTable; // Consider renaming fx:id to prTable in FXML
    @FXML private TableColumn<PurchaseRequisition, String> prIdCol;
    @FXML private TableColumn<PurchaseRequisition, String> SMIdCol; // Sales Manager ID
    @FXML private TableColumn<PurchaseRequisition, String> ItemIDCol;
    @FXML private TableColumn<PurchaseRequisition, Integer> quantityCol;
    @FXML private TableColumn<PurchaseRequisition, String> CreatedDateCol;

    @FXML private TextField prIDField;
    @FXML private TextField SMIDField;
    @FXML private TextField ItemIDField;
    @FXML private TextField QuantityField;
    @FXML private TextField CreateDateField;

    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    // Data store for Purchase Requisitions
    private ObservableList<PurchaseRequisition> prData = FXCollections.observableArrayList();
    private Map<String, PurchaseRequisition> prMap; // To hold data loaded from file
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    // ... other FXML elements and methods from your FinanceManagerController if combining ...

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // ... initialization for other parts of FinanceManagerController if any ...

        // Initialization for the "Create PR" section
        // Check if the FXML elements for PR are present before configuring them
        // This is important if this controller manages multiple FXML views.
        if (poTable != null) {
            configurePRTable();
            loadPRData();
            setupPRFormListeners();

            // Set default create date to today if field is empty
            if (CreateDateField != null && CreateDateField.getText().isEmpty()) {
                CreateDateField.setText(LocalDate.now().format(DATE_FORMATTER));
            }
             // Make PR ID field non-editable if it's auto-generated or for updates
            // prIDField.setEditable(false); // Example: if PR ID is generated on add
        }
    }

    private void configurePRTable() {
        prIdCol.setCellValueFactory(new PropertyValueFactory<>("prId"));
        SMIdCol.setCellValueFactory(new PropertyValueFactory<>("salesManagerId"));
        ItemIDCol.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        CreatedDateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        poTable.setItems(prData);
    }
    

    private void loadPRData() {
        try {
            prMap = FinanceManagerController.loadPR();
            prData.setAll(prMap.values());
            poTable.refresh(); // Ensure table displays loaded data
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load Purchase Requisitions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void savePRData() {
        try {
            // Update the map from the observable list before saving
            // This is crucial if items are added/deleted directly from prData
            prMap.clear();
            for(PurchaseRequisition pr : prData){
                prMap.put(pr.getPrId(), pr);
            }
            //PurchaseRequisition.savePurchaseRequisitions(prMap);
            System.out.println("supposed to save here");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Saving Error", "Could not save Purchase Requisitions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupPRFormListeners() {
        // Listener to populate form when a row is selected
        poTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populatePRForm(newSelection);
            } else {
                clearPRFormAction(null); // Clear form if selection is removed
            }
        });
    }

    private void populatePRForm(PurchaseRequisition pr) {
        prIDField.setText(pr.getPrId());
        SMIDField.setText(pr.getSalesManagerId());
        ItemIDField.setText(pr.getItemID());
        QuantityField.setText(String.valueOf(pr.getQuantity()));
        CreateDateField.setText(pr.getCreatedAt());
        prIDField.setEditable(false); // Don't allow editing of PR ID once loaded
    }

    @FXML
    private void handlePRAddButtonAction(ActionEvent event) { // Renamed to be specific
        if (!validatePRInput()) {
            return;
        }

        String prId = prIDField.getText().trim();
        String smId = SMIDField.getText().trim();
        String itemId = ItemIDField.getText().trim();
        int quantity = Integer.parseInt(QuantityField.getText().trim());
        LocalDate createdAt;
        try {
            createdAt = LocalDate.parse(CreateDateField.getText().trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid date format for 'Create At'. Use YYYY-MM-DD.");
            return;
        }

        PurchaseRequisition selectedPR = poTable.getSelectionModel().getSelectedItem();

        if (selectedPR != null && selectedPR.getPrId().equals(prId)) { // Update existing
            selectedPR.setSalesManagerId(smId);
            selectedPR.setItemID(itemId);
            selectedPR.setQuantity(quantity);
            selectedPR.setCreatedAt(createdAt); // This will format it to string

            poTable.refresh(); // Refresh table to show updated item
            showAlert(Alert.AlertType.INFORMATION, "Success", "Purchase Requisition '" + prId + "' updated.");

        } else { // Add new
            if (prMap.containsKey(prId)) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "PR ID '" + prId + "' already exists. Choose a different ID or select to update.");
                return;
            }
            PurchaseRequisition newPR = new PurchaseRequisition(prId, smId, itemId, quantity, "New",  createdAt);
            prData.add(newPR);
            // prMap.put(prId, newPR); // This will be handled by savePRData's reconstruction of the map
            showAlert(Alert.AlertType.INFORMATION, "Success", "Purchase Requisition '" + prId + "' added.");
        }

        savePRData();
        clearPRFormAction(null); // Clear form and selection
        prIDField.setEditable(true); // Allow PR ID editing for next new entry
    }

    @FXML
    private void handlePRDeleteButtonAction(ActionEvent event) { // Renamed
        PurchaseRequisition selectedPR = poTable.getSelectionModel().getSelectedItem();
        if (selectedPR == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a Purchase Requisition to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Delete PR '" + selectedPR.getPrId() + "'?", ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                prData.remove(selectedPR);
                // prMap.remove(selectedPR.getPrId()); // This will be handled by savePRData's reconstruction
                savePRData();
                clearPRFormAction(null);
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Purchase Requisition '" + selectedPR.getPrId() + "' deleted.");
            }
        });
    }

    @FXML
    private void clearPRFormAction(ActionEvent event) { // Renamed
        prIDField.clear();
        SMIDField.clear();
        ItemIDField.clear();
        QuantityField.clear();
        CreateDateField.setText(LocalDate.now().format(DATE_FORMATTER)); // Reset to today
        poTable.getSelectionModel().clearSelection();
        prIDField.setEditable(true); // Make PR ID field editable again for new entries
    }

    private boolean validatePRInput() {
        String prId = prIDField.getText().trim();
        String smId = SMIDField.getText().trim();
        String itemId = ItemIDField.getText().trim();
        String quantityStr = QuantityField.getText().trim();
        String createDateStr = CreateDateField.getText().trim();

        if (prId.isEmpty() || smId.isEmpty() || itemId.isEmpty() || quantityStr.isEmpty() || createDateStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "All fields are required.");
            return false;
        }

        try {
            Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Quantity must be a valid number.");
            return false;
        }

        try {
            LocalDate.parse(createDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid date format for 'Create At'. Use YYYY-MM-DD.");
            return false;
        }
        return true;
    }
    
    public void test(){
        System.out.println("testing");
    }

}