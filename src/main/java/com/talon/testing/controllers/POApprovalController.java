package com.talon.testing.controllers;

import com.talon.testing.models.PurchaseOrder;
import com.talon.testing.models.Supplier; // Needed for PurchaseOrder.loadPOs
// import com.talon.testing.models.Item; // Not directly used unless updating PO items here
// import com.talon.testing.models.POItem; // Not directly used unless updating PO items here
import com.talon.testing.utils.Router;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField; // Keep if using the item quantity update section
import javafx.scene.control.TextInputDialog; // For rejection reason
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
// import java.time.format.DateTimeFormatter; // Not directly used if dates are strings from model
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class POApprovalController extends Switchable implements Initializable {
    Router router = Router.getInstance();

    @FXML private TableView<PurchaseOrder> poTable;
    @FXML private TableColumn<PurchaseOrder, String> poIdColumnPO;
    @FXML private TableColumn<PurchaseOrder, String> prIdColumnPO;
    @FXML private TableColumn<PurchaseOrder, String> poSupplierNameColumnPO; // ADDED
    @FXML private TableColumn<PurchaseOrder, String> poOrderDateColumnPO;    // ADDED
    @FXML private TableColumn<PurchaseOrder, String> statusColumnPO;
    // @FXML private TableColumn<PurchaseOrder, String> isApprovedDisplayColumnPO; // Optional

    @FXML private Button approveButton;
    @FXML private Button rejectButton;

    // FXML fields for the optional "Modify Item Quantity" section
    // @FXML private TextField itemCodeField;
    // @FXML private TextField quantityField;
    // @FXML private Button updateQuantityButton;

    private ObservableList<PurchaseOrder> poForApprovalData = FXCollections.observableArrayList();
    private Map<String, PurchaseOrder> allLoadedPOsMapCache; // Cache for ALL POs loaded from file
    private Map<String, Supplier> allSuppliersMapCache;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            allSuppliersMapCache = Supplier.loadSuppliers();
            if (allSuppliersMapCache == null) allSuppliersMapCache = new HashMap<>();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load suppliers: " + e.getMessage());
            allSuppliersMapCache = new HashMap<>();
        }

        configurePOTable();
        loadPOsForApproval();

        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        // if (updateQuantityButton != null) updateQuantityButton.setDisable(true);


        poTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean poSelectedAndActionable = (newSelection != null && isActionable(newSelection));
            approveButton.setDisable(!poSelectedAndActionable);
            rejectButton.setDisable(!poSelectedAndActionable);
            // if (updateQuantityButton != null) updateQuantityButton.setDisable(!poSelectedAndActionable);
        });
    }

    private void configurePOTable() {
        poIdColumnPO.setCellValueFactory(new PropertyValueFactory<>("poId"));
        prIdColumnPO.setCellValueFactory(new PropertyValueFactory<>("prId"));
        poSupplierNameColumnPO.setCellValueFactory(new PropertyValueFactory<>("supplierNameDisplay"));
        poOrderDateColumnPO.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        statusColumnPO.setCellValueFactory(new PropertyValueFactory<>("status"));
        // if (isApprovedDisplayColumnPO != null) {
        // isApprovedDisplayColumnPO.setCellValueFactory(new PropertyValueFactory<>("status")); // Base on status
        // isApprovedDisplayColumnPO.setCellFactory(col -> new TableCell<>() {
        // @Override
        // protected void updateItem(String itemStatus, boolean empty) {
        // super.updateItem(itemStatus, empty);
        // if (empty || getTableRow() == null || getTableRow().getItem() == null) setText(null);
        // else setText(((PurchaseOrder) getTableRow().getItem()).hasBeenPreviouslyApproved() ? "Yes" : "No");
        // }
        // });
        // }
        poTable.setItems(poForApprovalData);
    }

    private void loadPOsForApproval() {
        try {
            this.allLoadedPOsMapCache = PurchaseOrder.loadPOs(this.allSuppliersMapCache);
            if (this.allLoadedPOsMapCache == null) this.allLoadedPOsMapCache = new HashMap<>();

            poForApprovalData.clear();
            poForApprovalData.addAll(this.allLoadedPOsMapCache.values().stream()
                .filter(PurchaseOrder::isPendingFinanceApproval) // Filter for POs needing approval
                .collect(Collectors.toList()));
            poTable.refresh();
            System.out.println("POs pending approval loaded: " + poForApprovalData.size());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load Purchase Orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper to determine if approve/reject buttons should be enabled
    private boolean isActionable(PurchaseOrder po) {
        if (po == null) return false;
        return po.isPendingFinanceApproval(); // Use the method from PurchaseOrder model
    }

    @FXML
    private void handleApprovePO() {
        PurchaseOrder selectedPO = poTable.getSelectionModel().getSelectedItem();
        if (selectedPO == null || !isActionable(selectedPO)) {
            showAlert(Alert.AlertType.WARNING, "Action Not Allowed", "Please select a PO that is pending approval.");
            return;
        }

        String currentStatus = selectedPO.getStatus();
        String nextStatus = "";
        String financeManagerForApproval = "FIN_MGR_01"; // TODO: Get actual logged-in finance manager ID

        // --- THIS IS YOUR CORE APPROVAL WORKFLOW LOGIC ---
        if ("Generated - Pending Approval".equalsIgnoreCase(currentStatus)) {
            nextStatus = "Pending PM Approval"; // Or directly to "Approved by PM" if no separate PM step
        } else if ("Pending PM Approval".equalsIgnoreCase(currentStatus)) {
            nextStatus = "Approved by PM";
            // selectedPO.setPurchaseManagerId("CurrentPMUser"); // If PM ID needs to be recorded
        } else if ("Approved by PM".equalsIgnoreCase(currentStatus)) { // Assuming this means it now goes to Finance
            nextStatus = "Pending Finance Approval";
        } else if ("Pending Finance Approval".equalsIgnoreCase(currentStatus)) {
            nextStatus = "Approved by Finance";
            selectedPO.setFinanceManagerId(financeManagerForApproval); // Record who approved
            selectedPO.setApprovalDate(LocalDate.now()); // Record approval date
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Approval Info", "PO is not in a state for the next approval step (" + currentStatus + ").");
            return;
        }
        // --- END OF WORKFLOW LOGIC ---

        selectedPO.setStatus(nextStatus);

        // Update in the main cache (allLoadedPOsMapCache)
        this.allLoadedPOsMapCache.put(selectedPO.getPoId(), selectedPO);
        try {
            PurchaseOrder.savePOs(this.allLoadedPOsMapCache); // Save all POs
            showAlert(Alert.AlertType.INFORMATION, "PO Approved", "PO: " + selectedPO.getPoId() + " status updated to: " + nextStatus);
            loadPOsForApproval(); // Refresh the list (approved PO might disappear or change)
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save PO approval: " + e.getMessage());
            selectedPO.setStatus(currentStatus); // Rollback status in memory
            this.allLoadedPOsMapCache.put(selectedPO.getPoId(), selectedPO); // Rollback cache
            loadPOsForApproval(); // Refresh
        }
    }

    @FXML
    private void handleRejectPO() {
        PurchaseOrder selectedPO = poTable.getSelectionModel().getSelectedItem();
        if (selectedPO == null || !isActionable(selectedPO)) {
            showAlert(Alert.AlertType.WARNING, "Action Not Allowed", "Please select a PO that is pending approval/rejection.");
            return;
        }

        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Reject Purchase Order");
        reasonDialog.setHeaderText("Rejecting PO: " + selectedPO.getPoId() + " (Current Status: " + selectedPO.getStatus() + ")");
        reasonDialog.setContentText("Please enter reason for rejection:");
        Optional<String> reasonResult = reasonDialog.showAndWait();

        if (reasonResult.isPresent() && !reasonResult.get().trim().isEmpty()) {
            String currentStatus = selectedPO.getStatus();
            selectedPO.setStatus("Rejected");
            String rejectionNote = "Rejected by [User/Role] on " + LocalDate.now() + ": " + reasonResult.get();
            selectedPO.setNotes((selectedPO.getNotes() == null ? "" : selectedPO.getNotes() + "\n") + rejectionNote);
            // Also clear approval fields if they were set for a previous stage
            selectedPO.setFinanceManagerId(null);
            selectedPO.setApprovalDate((LocalDate)null); // Cast null to LocalDate for the setter

            this.allLoadedPOsMapCache.put(selectedPO.getPoId(), selectedPO);
            try {
                PurchaseOrder.savePOs(this.allLoadedPOsMapCache);
                showAlert(Alert.AlertType.INFORMATION, "PO Rejected", "PO: " + selectedPO.getPoId() + " has been rejected.");
                loadPOsForApproval(); // Refresh table (rejected PO will disappear)
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save PO rejection: " + e.getMessage());
                selectedPO.setStatus(currentStatus); // Rollback
                // More complex note rollback might be needed if notes are important to preserve exactly
                this.allLoadedPOsMapCache.put(selectedPO.getPoId(), selectedPO);
                loadPOsForApproval();
            }
        } else if (reasonResult.isPresent() && reasonResult.get().trim().isEmpty()){
             showAlert(Alert.AlertType.WARNING, "Input Required", "Rejection reason cannot be empty if rejecting.");
        }
    }
}