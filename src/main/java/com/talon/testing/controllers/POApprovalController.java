package com.talon.testing.controllers;

import com.google.gson.Gson; // Keep for potential other uses, but not direct PO deserialization here
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.talon.testing.models.Item; // For updating stock from PO
import com.talon.testing.models.POItem;
import com.talon.testing.models.PurchaseOrder;
import com.talon.testing.models.PurchaseRequisition; // If needed for context
import com.talon.testing.models.StockTransaction; // If logging stock changes
import com.talon.testing.models.Supplier; // Needed for PurchaseOrder.loadPOs
import com.talon.testing.utils.Router; // Assuming you use this
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent; // For button handlers
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // Add this
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL; // For Initializable
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional; // For TextInputDialog
import java.util.ResourceBundle; // For Initializable
import javafx.scene.control.TextInputDialog;

public class POApprovalController extends Switchable implements Initializable { // Implement Initializable
    Router router = Router.getInstance();

    // NOTE: PO_FILE_PATH and PO_MAP_TYPE are not strictly needed in this controller
    // if all file I/O is handled by the PurchaseOrder model's static methods.
    // private static final String PO_FILE_PATH = "/data/PO.txt"; // Or purchase_orders.txt from PO model
    // private static final Type PO_MAP_TYPE = new TypeToken<Map<String, PurchaseOrder>>() {}.getType();

    @FXML
    private TableView<PurchaseOrder> poTable;

    @FXML
    private TableColumn<PurchaseOrder, String> poIdColumnPO;
    @FXML
    private TableColumn<PurchaseOrder, String> prIdColumnPO;
    @FXML
    private TableColumn<PurchaseOrder, String> poSupplierNameColumnPO; // To display Supplier Name
    @FXML
    private TableColumn<PurchaseOrder, String> statusColumnPO;
    @FXML
    private TableColumn<PurchaseOrder, String> poOrderDateColumnPO; // To display PO Order Date
    // No 'approvedColumnPO' in your latest FXML, but logic for approval/rejection is needed.

    @FXML
    private Button approveButton;
    @FXML
    private Button rejectButton;

    // These fields seem unrelated to just PO Approval List.
    // If this view also allows item-specific actions based on selected PO, they are needed.
    // @FXML private TextField itemCodeField;
    // @FXML private TextField quantityField;
    // @FXML private Button updateQuantityButton;

    private ObservableList<PurchaseOrder> poData = FXCollections.observableArrayList();
    private Map<String, PurchaseOrder> poMapCache; // In-memory cache for all POs
    private Map<String, Supplier> allSuppliersMapCache; // To pass to PurchaseOrder.loadPOs

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Override // Add Initializable method
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load suppliers first as PurchaseOrder.loadPOs might need it
        try {
            allSuppliersMapCache = Supplier.loadSuppliers();
            if (allSuppliersMapCache == null) allSuppliersMapCache = new HashMap<>();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load suppliers: " + e.getMessage());
            allSuppliersMapCache = new HashMap<>(); // Ensure it's not null
        }

        // Setup poTable columns
        if (poIdColumnPO != null) poIdColumnPO.setCellValueFactory(new PropertyValueFactory<>("poId"));
        if (prIdColumnPO != null) prIdColumnPO.setCellValueFactory(new PropertyValueFactory<>("prId"));
        if (poSupplierNameColumnPO != null) poSupplierNameColumnPO.setCellValueFactory(new PropertyValueFactory<>("supplierNameDisplay"));
        if (statusColumnPO != null) statusColumnPO.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (poOrderDateColumnPO != null) poOrderDateColumnPO.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        // If you had an "Approved" column showing Yes/No:
        // if (approvedColumnPO != null) {
        //    approvedColumnPO.setCellValueFactory(new PropertyValueFactory<>("status")); // Base it on status
        //    approvedColumnPO.setCellFactory(col -> new TableCell<PurchaseOrder, String>() {
        //        @Override
        //        protected void updateItem(String itemStatus, boolean empty) {
        //            super.updateItem(itemStatus, empty);
        //            if (empty || getTableRow() == null || getTableRow().getItem() == null) setText(null);
        //            else setText(((PurchaseOrder) getTableRow().getItem()).isConsideredApprovedForPayment() ? "Yes" : "No");
        //        }
        //    });
        // }


        if (poTable != null) {
            poTable.setItems(poData);
        }

        loadPOData(); // Load POs

        // Add event handlers for buttons
        if (approveButton != null) approveButton.setOnAction(event -> handleApprovePO());
        if (rejectButton != null) rejectButton.setOnAction(event -> handleRejectPO());
        // if (updateQuantityButton != null) updateQuantityButton.setOnAction(event -> handleUpdateQuantity());

        // Disable buttons initially until a PO is selected
        if (approveButton != null) approveButton.setDisable(true);
        if (rejectButton != null) rejectButton.setDisable(true);

        // Listener for table selection
        if (poTable != null) {
            poTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                boolean poSelected = (newSelection != null);
                if (approveButton != null) approveButton.setDisable(!poSelected || !canApprove(newSelection));
                if (rejectButton != null) rejectButton.setDisable(!poSelected || !canReject(newSelection));
            });
        }
    }
    
    private void loadPOData() {
        try {
            // CALL THE STATIC METHOD FROM PurchaseOrder model which handles DTOs
            this.poMapCache = PurchaseOrder.loadPOs(this.allSuppliersMapCache);
            
            System.out.println("Loaded POs in POApprovalController: " + this.poMapCache);
            poData.clear();
            if (this.poMapCache != null) {
                // Filter for POs that need approval (e.g., "Generated - Pending Approval", "Pending PM Approval")
                poData.addAll(this.poMapCache.values().stream()
                    .filter(po -> "Generated - Pending Approval".equalsIgnoreCase(po.getStatus()) ||
                                  "Pending PM Approval".equalsIgnoreCase(po.getStatus()) ||
                                  "Pending Finance Approval".equalsIgnoreCase(po.getStatus())) // Add statuses relevant for this screen
                    .collect(Collectors.toList()));
            }
            if (poTable != null) poTable.refresh();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load Purchase Orders: " + e.getMessage());
        }
    }
    
    // Removed static loadPO() from here, as it should be in PurchaseOrder.java model
    // Removed static determineFilePath() from here.

    private boolean canApprove(PurchaseOrder po) {
        if (po == null) return false;
        // Define statuses that can be approved from this screen
        String status = po.getStatus();
        return "Generated - Pending Approval".equalsIgnoreCase(status) ||
               "Pending PM Approval".equalsIgnoreCase(status) ||
               "Pending Finance Approval".equalsIgnoreCase(status);
    }

    private boolean canReject(PurchaseOrder po) {
        if (po == null) return false;
        String status = po.getStatus();
        // Define statuses that can be rejected
        return "Generated - Pending Approval".equalsIgnoreCase(status) ||
               "Pending PM Approval".equalsIgnoreCase(status) ||
               "Pending Finance Approval".equalsIgnoreCase(status);
    }


    private void handleApprovePO() {
        PurchaseOrder selectedPO = poTable.getSelectionModel().getSelectedItem();
        if (selectedPO != null && canApprove(selectedPO)) {
            // Determine next approval status based on current status or user role
            String nextStatus = "";
            String currentStatus = selectedPO.getStatus();
            // This logic needs to be adapted to your specific approval workflow
            if ("Generated - Pending Approval".equalsIgnoreCase(currentStatus) || "Pending PM Approval".equalsIgnoreCase(currentStatus)) {
                nextStatus = "Approved by PM"; // Or directly to "Pending Finance Approval"
            } else if ("Pending Finance Approval".equalsIgnoreCase(currentStatus)) {
                nextStatus = "Approved by Finance"; // Final approval for payment processing
                selectedPO.setApprovalDate(LocalDate.now()); // Set approval date
                selectedPO.setFinanceManagerId("CurrentFinanceUser"); // TODO: Get actual logged-in finance user
            } else {
                 showAlert(Alert.AlertType.INFORMATION, "Approval Info", "PO is not in a state that can be approved from here, or already approved.");
                return;
            }

            selectedPO.setStatus(nextStatus);
            
            // Update in cache and save
            this.poMapCache.put(selectedPO.getPoId(), selectedPO);
            try {
                PurchaseOrder.savePOs(this.poMapCache);
                showAlert(Alert.AlertType.INFORMATION, "PO Approved", "PO: " + selectedPO.getPoId() + " status updated to " + nextStatus);
                loadPOData(); // Refresh the table to reflect changes (e.g., it might disappear if no longer pending)
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save PO approval: " + e.getMessage());
                selectedPO.setStatus(currentStatus); // Rollback status in memory
                this.poMapCache.put(selectedPO.getPoId(), selectedPO); // Rollback cache
                loadPOData(); // Refresh
            }
        } else if (selectedPO != null) {
            showAlert(Alert.AlertType.INFORMATION, "Approval Info", "This PO cannot be approved from its current status (" + selectedPO.getStatus() + ").");
        }
    }

    private void handleRejectPO() {
        PurchaseOrder selectedPO = poTable.getSelectionModel().getSelectedItem();
        if (selectedPO != null && canReject(selectedPO)) {
            // Ask for rejection reason
            TextInputDialog reasonDialog = new TextInputDialog();
            reasonDialog.setTitle("Reject Purchase Order");
            reasonDialog.setHeaderText("Rejecting PO: " + selectedPO.getPoId());
            reasonDialog.setContentText("Please enter reason for rejection:");
            Optional<String> reasonResult = reasonDialog.showAndWait();

            if (reasonResult.isPresent() && !reasonResult.get().trim().isEmpty()) {
                String currentStatus = selectedPO.getStatus();
                selectedPO.setStatus("Rejected");
                selectedPO.setNotes((selectedPO.getNotes() == null ? "" : selectedPO.getNotes() + "\n") + "Rejected: " + reasonResult.get());

                this.poMapCache.put(selectedPO.getPoId(), selectedPO);
                try {
                    PurchaseOrder.savePOs(this.poMapCache);
                    showAlert(Alert.AlertType.INFORMATION, "PO Rejected", "PO: " + selectedPO.getPoId() + " has been rejected.");
                    loadPOData(); // Refresh table
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save PO rejection: " + e.getMessage());
                    selectedPO.setStatus(currentStatus); // Rollback
                    // Basic notes rollback - more complex if notes had prior content
                    if(selectedPO.getNotes() != null && selectedPO.getNotes().endsWith("Rejected: " + reasonResult.get())){
                        selectedPO.setNotes(selectedPO.getNotes().replace("\nRejected: " + reasonResult.get(), ""));
                    }
                    this.poMapCache.put(selectedPO.getPoId(), selectedPO);
                    loadPOData();
                }
            } else if (reasonResult.isPresent() && reasonResult.get().trim().isEmpty()){
                 showAlert(Alert.AlertType.WARNING, "Input Required", "Rejection reason cannot be empty.");
            }
        } else if (selectedPO != null){
            showAlert(Alert.AlertType.INFORMATION, "Rejection Info", "This PO cannot be rejected from its current status (" + selectedPO.getStatus() + ").");
        }
    }

    // handleUpdateQuantity is likely not relevant for a PO Approval screen unless you allow quantity changes
    // during approval, which is usually not standard. If it is, it needs careful implementation.
    // For now, I'll comment it out from being active.
    /*
    private void handleUpdateQuantity() {
        // This would require selecting a PO, then an item within that PO,
        // and then updating the quantity of that POItem.
        // This is more complex than the current FXML suggests.
        System.out.println("Update Quantity button clicked - functionality not fully implemented for PO Approval screen.");
    }
    */

}