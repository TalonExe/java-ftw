package com.talon.testing.controllers;

import com.talon.testing.models.PurchaseRequisition;
import com.talon.testing.models.Supplier;
import com.talon.testing.models.Item;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CreatePRController extends Switchable implements Initializable {

    // --- FXML Elements ---
    @FXML private TableView<PurchaseRequisition> poTable; // Consider renaming to prTable
    @FXML private TableColumn<PurchaseRequisition, String> prIdCol;
    @FXML private TableColumn<PurchaseRequisition, String> SMIdCol;
    // NEW TableColumn FXML injections
    @FXML private TableColumn<PurchaseRequisition, String> supplierNameCol;
    @FXML private TableColumn<PurchaseRequisition, String> itemNameCol;
    @FXML private TableColumn<PurchaseRequisition, String> ItemIDCol; // Keeps showing Item Code
    @FXML private TableColumn<PurchaseRequisition, Integer> quantityCol;
    @FXML private TableColumn<PurchaseRequisition, String> CreatedDateCol;
    @FXML private TableColumn<PurchaseRequisition, String> statusCol; // For PR status

    @FXML private TextField prIDField;
    @FXML private TextField SMIDField;
    @FXML private TextField QuantityField;
    @FXML private TextField CreateDateField;

    @FXML private ComboBox<Supplier> supplierComboBox;
    @FXML private ComboBox<Item> itemComboBox;

    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    // --- Data ---
    private ObservableList<PurchaseRequisition> prData = FXCollections.observableArrayList();
    private Map<String, PurchaseRequisition> prMapCache;

    private ObservableList<Supplier> allSuppliers = FXCollections.observableArrayList();
    private ObservableList<Item> allItemsMasterList = FXCollections.observableArrayList();
    private ObservableList<Item> itemsForSelectedSupplier = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadInitialSuppliersAndItems();

        if (poTable != null) {
            configurePRTable();
            loadPRData();
            setupPRFormListeners();
        }

        configureComboBoxes();

        if (CreateDateField != null && CreateDateField.getText().isEmpty()) {
            CreateDateField.setText(LocalDate.now().format(DATE_FORMATTER));
        }

        if (addButton != null) addButton.setOnAction(this::handlePRAddButtonAction);
        if (deleteButton != null) deleteButton.setOnAction(this::handlePRDeleteButtonAction);
        if (clearButton != null) clearButton.setOnAction(this::clearPRFormAction);
    }

    private void loadInitialSuppliersAndItems() {
        try {
            Map<String, Supplier> loadedSuppliers = Supplier.loadSuppliers();
            if (loadedSuppliers != null) allSuppliers.setAll(loadedSuppliers.values());
            else allSuppliers.clear();

            Map<String, Item> loadedItems = Item.loadItems(); // Assumes Item.java has supplierId
            if (loadedItems != null) allItemsMasterList.setAll(loadedItems.values());
            else allItemsMasterList.clear();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Initial Data Load Error", "Could not load suppliers or items: " + e.getMessage());
        }
    }

    private void configureComboBoxes() {
        supplierComboBox.setItems(allSuppliers);
        supplierComboBox.setConverter(new StringConverter<>() { // Using lambda for conciseness
            @Override public String toString(Supplier s) { return s == null ? null : s.getSupplierName() + " (" + s.getSupplierId() + ")"; }
            @Override public Supplier fromString(String s) { return null; }
        });

        itemComboBox.setItems(itemsForSelectedSupplier);
        itemComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Item i) { return i == null ? null : i.getItemName() + " (" + i.getItemCode() + ")"; }
            @Override public Item fromString(String s) { return null; }
        });
        itemComboBox.setDisable(true);

        supplierComboBox.getSelectionModel().selectedItemProperty().addListener((obs, o, newSupplier) -> {
            if (newSupplier != null) {
                updateItemComboBoxForSupplier(newSupplier);
                itemComboBox.setDisable(false);
                itemComboBox.getSelectionModel().clearSelection();
            } else {
                itemsForSelectedSupplier.clear();
                itemComboBox.setDisable(true);
                itemComboBox.getSelectionModel().clearSelection();
            }
        });
    }

    private void updateItemComboBoxForSupplier(Supplier selectedSupplier) {
        itemsForSelectedSupplier.clear();
        if (selectedSupplier != null && allItemsMasterList != null) {
            itemsForSelectedSupplier.addAll(
                allItemsMasterList.stream()
                        .filter(item -> selectedSupplier.getSupplierId().equals(item.getSupplierId()))
                        .collect(Collectors.toList())
            );
        }
    }

    private void configurePRTable() {
        prIdCol.setCellValueFactory(new PropertyValueFactory<>("prId"));
        SMIdCol.setCellValueFactory(new PropertyValueFactory<>("salesManagerId"));
        // Bind new columns to the display fields in PurchaseRequisition model
        supplierNameCol.setCellValueFactory(new PropertyValueFactory<>("supplierNameDisplay"));
        itemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemNameDisplay"));
        ItemIDCol.setCellValueFactory(new PropertyValueFactory<>("ItemID")); // This is the item's code
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        CreatedDateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status")); // For PR status
        poTable.setItems(prData);
    }

    private void loadPRData() {
        try {
            this.prMapCache = FinanceManagerController.loadPRsFromFileStatic();
            this.prData.clear();
            if (this.prMapCache != null) {
                // Populate display names if not stored in JSON
                for (PurchaseRequisition pr : this.prMapCache.values()) {
                    if (pr.getItemNameDisplay() == null || pr.getSupplierNameDisplay() == null) {
                        populateDisplayNamesForPR(pr);
                    }
                }
                this.prData.addAll(this.prMapCache.values());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load PRs: " + e.getMessage());
        }
    }
    
    // Helper to populate display names for a PR if they weren't stored in the PR file
    private void populateDisplayNamesForPR(PurchaseRequisition pr) {
        if (pr == null || pr.getItemID() == null) return;

        Item item = allItemsMasterList.stream()
            .filter(i -> pr.getItemID().equals(i.getItemCode()))
            .findFirst().orElse(null);

        if (item != null) {
            pr.setItemNameDisplay(item.getItemName());
            if (item.getSupplierId() != null) {
                Supplier supplier = allSuppliers.stream()
                    .filter(s -> item.getSupplierId().equals(s.getSupplierId()))
                    .findFirst().orElse(null);
                if (supplier != null) {
                    pr.setSupplierNameDisplay(supplier.getSupplierName());
                } else {
                    pr.setSupplierNameDisplay("Unknown Supplier");
                }
            } else {
                 pr.setSupplierNameDisplay("N/A");
            }
        } else {
            pr.setItemNameDisplay("Unknown Item");
            pr.setSupplierNameDisplay("N/A");
        }
    }


    private void setupPRFormListeners() {
        poTable.getSelectionModel().selectedItemProperty().addListener((obs, o, newPR) -> {
            if (newPR != null) populatePRForm(newPR);
            else clearPRFormAction(null);
        });
    }

    private void populatePRForm(PurchaseRequisition pr) {
        prIDField.setText(pr.getPrId());
        SMIDField.setText(pr.getSalesManagerId());
        QuantityField.setText(String.valueOf(pr.getQuantity()));
        CreateDateField.setText(pr.getCreatedAt());
        prIDField.setEditable(false);

        Item prItem = allItemsMasterList.stream().filter(i -> i.getItemCode().equals(pr.getItemID())).findFirst().orElse(null);
        if (prItem != null && prItem.getSupplierId() != null) {
            Supplier prSupplier = allSuppliers.stream().filter(s -> s.getSupplierId().equals(prItem.getSupplierId())).findFirst().orElse(null);
            supplierComboBox.setValue(prSupplier); // This triggers itemComboBox update
            // Ensure itemComboBox is updated before setting its value
            if (itemComboBox.getItems().stream().anyMatch(i -> i.getItemCode().equals(pr.getItemID()))) {
                 itemComboBox.setValue(prItem);
            } else {
                // If item not in list after supplier set (shouldn't happen if data is consistent), clear item selection
                itemComboBox.getSelectionModel().clearSelection();
            }
        } else {
            supplierComboBox.getSelectionModel().clearSelection();
            itemComboBox.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void handlePRAddButtonAction(ActionEvent event) {
        if (!validatePRInputWithComboBoxes()) return;

        String prId = prIDField.getText().trim();
        String smId = SMIDField.getText().trim();
        Supplier selectedSupplier = supplierComboBox.getSelectionModel().getSelectedItem();
        Item selectedItem = itemComboBox.getSelectionModel().getSelectedItem();
        int quantity = Integer.parseInt(QuantityField.getText().trim());
        LocalDate createdAtDate;
        try {
            createdAtDate = LocalDate.parse(CreateDateField.getText().trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid date: YYYY-MM-DD."); return;
        }

        PurchaseRequisition selectedPR = poTable.getSelectionModel().getSelectedItem();

        // Get display names
        String supplierNameDisp = (selectedSupplier != null) ? selectedSupplier.getSupplierName() : "N/A";
        String itemNameDisp = (selectedItem != null) ? selectedItem.getItemName() : "N/A";


        if (selectedPR != null && selectedPR.getPrId().equals(prId)) { // Update
            selectedPR.setSalesManagerId(smId);
            selectedPR.setItemID(selectedItem.getItemCode());
            selectedPR.setItemNameDisplay(itemNameDisp);             // SET DISPLAY NAME
            selectedPR.setSupplierNameDisplay(supplierNameDisp);     // SET DISPLAY NAME
            selectedPR.setQuantity(quantity);
            selectedPR.setCreatedAt(createdAtDate);
            // selectedPR.setStatus("Updated"); // Example status update
            poTable.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Success", "PR '" + prId + "' updated.");
            savePRData();
        } else { // Add new
            if (selectedItem == null) { /* already handled by validation */ return; }
            // Use the constructor that takes display names
            boolean success = addPR(prId, smId, selectedItem.getItemCode(), itemNameDisp, supplierNameDisp, quantity, "Pending", createdAtDate);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "PR '" + prId + "' added.");
            }
        }
        clearPRFormAction(null);
        prIDField.setEditable(true);
    }

    // Updated addPR method signature
    public boolean addPR(String prId, String salesManagerId, String itemId,
                         String itemNameDisplay, String supplierNameDisplay, // Added display names
                         int quantity, String status, LocalDate createdAt) {
        if (this.prMapCache == null) this.prMapCache = new HashMap<>();
        if (this.prMapCache.containsKey(prId)) {
            showAlert(Alert.AlertType.ERROR, "Duplicate ID", "PR ID '" + prId + "' already exists.");
            return false;
        }
        // Use the constructor that accepts display names
        PurchaseRequisition newPR = new PurchaseRequisition(prId, salesManagerId, itemId, itemNameDisplay, supplierNameDisplay, quantity, status, createdAt);
        this.prMapCache.put(newPR.getPrId(), newPR);
        this.prData.add(newPR);
        return savePRData();
    }

    @FXML
    private void handlePRDeleteButtonAction(ActionEvent event) {
        // ... (delete logic remains the same) ...
        PurchaseRequisition selectedPR = poTable.getSelectionModel().getSelectedItem();
        if (selectedPR == null) { /* ... */ return; }
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION, "Delete PR?", ButtonType.OK, ButtonType.CANCEL);
        confirmationDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (deletePR(selectedPR.getPrId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "PR deleted.");
                    clearPRFormAction(null);
                }
            }
        });
    }
    
    public boolean deletePR(String prId) {
        // ... (delete logic remains the same) ...
        if (prId == null || prId.trim().isEmpty()) { return false; }
        if (this.prMapCache == null || !this.prMapCache.containsKey(prId)) { return false; }
        PurchaseRequisition prToRemove = this.prMapCache.remove(prId);
        this.prData.remove(prToRemove);
        return savePRData();
    }

    private boolean savePRData() {
        try {
            FinanceManagerController.savePRsToFileStatic(this.prMapCache);
            return true;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Saving Error", "Could not save PRs: " + e.getMessage());
            return false;
        }
    }

    @FXML
    private void clearPRFormAction(ActionEvent event) {
        // ... (clear logic remains the same, including ComboBoxes) ...
        prIDField.clear(); SMIDField.clear();
        supplierComboBox.getSelectionModel().clearSelection();
        itemComboBox.getSelectionModel().clearSelection(); itemComboBox.getItems().clear(); itemComboBox.setDisable(true);
        QuantityField.clear(); CreateDateField.setText(LocalDate.now().format(DATE_FORMATTER));
        if (poTable != null) poTable.getSelectionModel().clearSelection();
        prIDField.setEditable(true);
    }

    private boolean validatePRInputWithComboBoxes() {
        // ... (validation logic remains the same) ...
        String prId = prIDField.getText().trim(); String smId = SMIDField.getText().trim();
        Supplier s = supplierComboBox.getSelectionModel().getSelectedItem(); Item i = itemComboBox.getSelectionModel().getSelectedItem();
        String qStr = QuantityField.getText().trim(); String cDateStr = CreateDateField.getText().trim();
        if(prId.isEmpty()||smId.isEmpty()||qStr.isEmpty()||cDateStr.isEmpty()){ showAlert(Alert.AlertType.ERROR, "Input Error", "Required fields missing."); return false; }
        if(s==null){ showAlert(Alert.AlertType.ERROR, "Input Error", "Select supplier."); return false; }
        if(i==null){ showAlert(Alert.AlertType.ERROR, "Input Error", "Select item."); return false; }
        try { Integer.parseInt(qStr); } catch (NumberFormatException e){ showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid quantity."); return false; }
        try { LocalDate.parse(cDateStr, DATE_FORMATTER); } catch (DateTimeParseException e){ showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid date."); return false; }
        return true;
    }
    
    public void test(){ System.out.println("CreatePRController testing"); }
}