package com.talon.testing.controllers;

import com.talon.testing.models.*; // Import all your models
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CreatePOController extends Switchable implements Initializable {

    // FXML for Approved PRs Table
    @FXML private TableView<PurchaseRequisition> approvedPRTableView;
    @FXML private TableColumn<PurchaseRequisition, String> prID_PRColumn;
    @FXML private TableColumn<PurchaseRequisition, String> prItemName_PRColumn;
    @FXML private TableColumn<PurchaseRequisition, Integer> prQuantity_PRColumn;
    @FXML private TableColumn<PurchaseRequisition, String> prSupplierName_PRColumn;
    @FXML private TableColumn<PurchaseRequisition, String> prCreatedAt_PRColumn;
    @FXML private Button createPOButton;

    // FXML for Purchase Orders Table
    @FXML private TableView<PurchaseOrder> purchaseOrderTableView;
    @FXML private TableColumn<PurchaseOrder, String> poID_POColumn;
    @FXML private TableColumn<PurchaseOrder, String> poPRID_POColumn;
    @FXML private TableColumn<PurchaseOrder, String> poSupplier_POColumn;
    @FXML private TableColumn<PurchaseOrder, String> poDate_POColumn;
    @FXML private TableColumn<PurchaseOrder, String> poStatus_POColumn;
    @FXML private Button updatePOButton;
    @FXML private Button deletePOButton;

    // FXML for PO Details Pane (for updating status)
    @FXML private VBox poDetailsPane;
    @FXML private TextField poDetailIdField;
    @FXML private TextField poDetailCurrentStatusField;
    @FXML private ComboBox<String> poDetailNewStatusComboBox;
    @FXML private Button savePOChangesButton;

    // Data collections
    private ObservableList<PurchaseRequisition> approvedPRData = FXCollections.observableArrayList();
    private Map<String, PurchaseRequisition> allPRsMap; // All PRs

    private ObservableList<PurchaseOrder> purchaseOrderData = FXCollections.observableArrayList();
    private Map<String, PurchaseOrder> allPOsMap; // All POs

    private Map<String, Item> allItemsMap; // To get item details
    private Map<String, Supplier> allSuppliersMap; // To get supplier details

    private PurchaseOrder selectedPOForUpdate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMasterData(); // Load Items and Suppliers first
        configurePRTable();
        configurePOTable();
        configurePODetailsPane();

        loadApprovedPRs();
        loadExistingPOs();

        // Listeners for table selections to enable/disable buttons
        approvedPRTableView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            createPOButton.setDisable(n == null);
        });
        purchaseOrderTableView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            updatePOButton.setDisable(n == null);
            deletePOButton.setDisable(n == null);
            if (n != null) {
                populatePODetailsPane(n);
                poDetailsPane.setVisible(true);
                poDetailsPane.setManaged(true);
            } else {
                poDetailsPane.setVisible(false);
                poDetailsPane.setManaged(false);
            }
        });
    }

    private void loadMasterData() {
        try {
            allItemsMap = Item.loadItems(); // From Item.java
            allSuppliersMap = Supplier.loadSuppliers(); // From Supplier.java
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Master Data Load Error", "Could not load items or suppliers: " + e.getMessage());
        }
        if (allItemsMap == null) allItemsMap = new HashMap<>();
        if (allSuppliersMap == null) allSuppliersMap = new HashMap<>();
    }


    private void configurePRTable() {
        prID_PRColumn.setCellValueFactory(new PropertyValueFactory<>("prId"));
        prItemName_PRColumn.setCellValueFactory(new PropertyValueFactory<>("itemNameDisplay"));
        prQuantity_PRColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        prSupplierName_PRColumn.setCellValueFactory(new PropertyValueFactory<>("supplierNameDisplay"));
        prCreatedAt_PRColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        approvedPRTableView.setItems(approvedPRData);
    }

    private void configurePOTable() {
        poID_POColumn.setCellValueFactory(new PropertyValueFactory<>("poId"));
        poPRID_POColumn.setCellValueFactory(new PropertyValueFactory<>("prId")); // Assuming PO stores original PR ID
        poSupplier_POColumn.setCellValueFactory(new PropertyValueFactory<>("supplierNameDisplay")); // Assuming PO stores supplier name
        poDate_POColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        poStatus_POColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        purchaseOrderTableView.setItems(purchaseOrderData);
    }

    private void configurePODetailsPane() {
        // Possible PO statuses for update
        poDetailNewStatusComboBox.setItems(FXCollections.observableArrayList(
                "Pending PM Approval", "Approved by PM", "Pending Finance Approval", "Approved by Finance", "Paid", "Shipped", "Received", "Cancelled"
        ));
    }


    private void loadApprovedPRs() {
        try {
            allPRsMap = FinanceManagerController.loadPRsFromFileStatic(); // Your static PR loader
            if (allPRsMap == null) allPRsMap = new HashMap<>();

            approvedPRData.clear();
            approvedPRData.addAll(
                allPRsMap.values().stream()
                    .filter(pr -> "Approved".equalsIgnoreCase(pr.getStatus())) // Filter for "Approved" PRs
                    .peek(this::ensurePRDisplayNames) // Populate display names if missing
                    .collect(Collectors.toList())
            );
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "PR Load Error", "Could not load purchase requisitions: " + e.getMessage());
        }
    }
    
    // Helper to ensure PR display names are populated
    private void ensurePRDisplayNames(PurchaseRequisition pr) {
        if (pr.getItemNameDisplay() == null || pr.getSupplierNameDisplay() == null) {
            Item item = allItemsMap.get(pr.getItemID());
            if (item != null) {
                pr.setItemNameDisplay(item.getItemName());
                Supplier supplier = allSuppliersMap.get(item.getSupplierId());
                if (supplier != null) {
                    pr.setSupplierNameDisplay(supplier.getSupplierName());
                } else {
                    pr.setSupplierNameDisplay("Unknown Supplier");
                }
            } else {
                pr.setItemNameDisplay("Unknown Item");
                pr.setSupplierNameDisplay("N/A");
            }
        }
    }


    private void loadExistingPOs() {
        try {
            allPOsMap = PurchaseOrder.loadPOs(allSuppliersMap); // From PurchaseOrder.java
             if (allPOsMap == null) allPOsMap = new HashMap<>();
            purchaseOrderData.clear();
            purchaseOrderData.addAll(allPOsMap.values());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "PO Load Error", "Could not load purchase orders: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreatePOFromPR(ActionEvent event) {
        PurchaseRequisition selectedPR = approvedPRTableView.getSelectionModel().getSelectedItem();
        if (selectedPR == null) {
            showAlert(Alert.AlertType.WARNING, "No PR Selected", "Please select an approved PR to create a PO.");
            return;
        }

        // Find item and supplier details
        Item itemDetails = allItemsMap.get(selectedPR.getItemID());
        if (itemDetails == null) {
            showAlert(Alert.AlertType.ERROR, "Item Error", "Details for item " + selectedPR.getItemID() + " not found.");
            return;
        }
        Supplier supplierDetails = allSuppliersMap.get(itemDetails.getSupplierId());
        if (supplierDetails == null) {
            showAlert(Alert.AlertType.ERROR, "Supplier Error", "Supplier details for item " + itemDetails.getItemName() + " not found.");
            return;
        }

        // Generate a new PO ID (simple example, ensure uniqueness in a real app)
        String newPoId = "PO" + (allPOsMap.size() + 1001); // Example: PO1001, PO1002
        while(allPOsMap.containsKey(newPoId)){ // Ensure uniqueness
            newPoId = "PO" + (Integer.parseInt(newPoId.substring(2)) + 1);
        }


        // Create PurchaseOrder object using the conceptual constructor
        PurchaseOrder newPO = new PurchaseOrder(newPoId, selectedPR, supplierDetails, itemDetails);
        // newPO.setStatus("Generated - Pending Approval"); // Or set in constructor

        // Add to PO collections
        allPOsMap.put(newPO.getPoId(), newPO);
        purchaseOrderData.add(newPO);

        // Update PR status
        selectedPR.setStatus("Processed to PO");
        // Since PR is from allPRsMap, updating it here updates the cached map.
        // We also need to remove it from the 'approvedPRData' list as it's no longer just "Approved"
        approvedPRData.remove(selectedPR);


        // Save both POs and PRs
        try {
            PurchaseOrder.savePOs(allPOsMap);
            FinanceManagerController.savePRsToFileStatic(allPRsMap); // Save updated PR map
            showAlert(Alert.AlertType.INFORMATION, "PO Created", "Purchase Order " + newPO.getPoId() + " created successfully.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save PO or update PR: " + e.getMessage());
            // Basic rollback (consider more sophisticated transaction if needed)
            allPOsMap.remove(newPO.getPoId());
            purchaseOrderData.remove(newPO);
            selectedPR.setStatus("Approved"); // Revert status
            approvedPRData.add(selectedPR); // Add back to approved list
            // Don't save PRs again here as it might overwrite other changes
        }
    }

    private void populatePODetailsPane(PurchaseOrder po) {
        selectedPOForUpdate = po;
        poDetailIdField.setText(po.getPoId());
        poDetailCurrentStatusField.setText(po.getStatus());
        poDetailNewStatusComboBox.getSelectionModel().clearSelection(); // Clear previous selection
        poDetailNewStatusComboBox.setValue(po.getStatus()); // Set current status as default in combo
    }


    @FXML
    private void handleUpdatePOStatus(ActionEvent event) {
        PurchaseOrder selectedPO = purchaseOrderTableView.getSelectionModel().getSelectedItem();
        if (selectedPO == null) {
            showAlert(Alert.AlertType.WARNING, "No PO Selected", "Please select a PO from the table to update its status.");
            return;
        }
        // Make the details pane visible if it's not already (or handle this via selection listener)
        populatePODetailsPane(selectedPO);
        poDetailsPane.setVisible(true);
        poDetailsPane.setManaged(true);
    }


    @FXML
    private void handleSavePOChanges(ActionEvent event) {
        if (selectedPOForUpdate == null) {
            showAlert(Alert.AlertType.ERROR, "Update Error", "No PO selected for update.");
            return;
        }
        String newStatus = poDetailNewStatusComboBox.getSelectionModel().getSelectedItem();
        if (newStatus == null || newStatus.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please select a new status for the PO.");
            return;
        }

        String oldStatus = selectedPOForUpdate.getStatus();
        selectedPOForUpdate.setStatus(newStatus);
        // Potentially set financeManagerId and approvalDate if status indicates approval
        // if (newStatus.contains("Approved by Finance")) {
        //    selectedPOForUpdate.setFinanceManagerId("CurrentUserID"); // Get current user
        //    selectedPOForUpdate.setApprovalDate(LocalDate.now().toString());
        // }


        // Update in map and list (TableView refreshes)
        allPOsMap.put(selectedPOForUpdate.getPoId(), selectedPOForUpdate);
        purchaseOrderTableView.refresh(); // Force refresh of the row

        try {
            PurchaseOrder.savePOs(allPOsMap);
            showAlert(Alert.AlertType.INFORMATION, "PO Updated", "PO " + selectedPOForUpdate.getPoId() + " status updated to " + newStatus);
            poDetailsPane.setVisible(false);
            poDetailsPane.setManaged(false);
            selectedPOForUpdate = null;
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save PO changes: " + e.getMessage());
            selectedPOForUpdate.setStatus(oldStatus); // Rollback status in memory
            purchaseOrderTableView.refresh();
        }
    }


    @FXML
    private void handleDeletePO(ActionEvent event) {
        PurchaseOrder selectedPO = purchaseOrderTableView.getSelectionModel().getSelectedItem();
        if (selectedPO == null) {
            showAlert(Alert.AlertType.WARNING, "No PO Selected", "Please select a PO to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Delete PO " + selectedPO.getPoId() + "? This may have implications if linked to invoices or payments.", ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm PO Deletion");
        confirmation.setHeaderText(null);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            PurchaseRequisition originalPR = allPRsMap.get(selectedPO.getPrId());

            allPOsMap.remove(selectedPO.getPoId());
            purchaseOrderData.remove(selectedPO);

            // Optionally, revert the status of the original PR
            if (originalPR != null) {
                originalPR.setStatus("Approved"); // Or "Re-Opened"
                // No need to re-add to approvedPRData unless you explicitly removed it earlier based on status
                // and want it to reappear. loadApprovedPRs() will pick it up on next load.
            }

            try {
                PurchaseOrder.savePOs(allPOsMap);
                if (originalPR != null) { // Save PRs only if originalPR was found and status changed
                    FinanceManagerController.savePRsToFileStatic(allPRsMap);
                }
                showAlert(Alert.AlertType.INFORMATION, "PO Deleted", "PO " + selectedPO.getPoId() + " deleted.");
                poDetailsPane.setVisible(false);
                poDetailsPane.setManaged(false);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save changes after PO deletion: " + e.getMessage());
                // Rollback (more complex here)
                allPOsMap.put(selectedPO.getPoId(), selectedPO);
                purchaseOrderData.add(selectedPO);
                if (originalPR != null) originalPR.setStatus("Processed to PO"); // Revert PR status
                // Consider full data reload on critical save failure
            }
        }
    }
}