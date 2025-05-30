package com.talon.testing.controllers;

import com.talon.testing.models.POItem;
import com.talon.testing.models.PurchaseOrder;
import com.talon.testing.models.Supplier; // Make sure Supplier model is available
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // IMPORT THIS
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
// Removed FXML Loader, Parent, Scene, Stage as they are for navigation which Switchable might handle
// import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap; // For initializing maps if null
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProcessPaymentController extends Switchable implements Initializable { // IMPLEMENT Initializable

    // --- FXML Injections ---
    @FXML private TableView<Supplier> supplierTableView;
    @FXML private TableColumn<Supplier, String> supplierIdColumn;
    @FXML private TableColumn<Supplier, String> supplierNameColumn;
    @FXML private TableColumn<Supplier, String> contactPersonColumn;
    @FXML private TableColumn<Supplier, String> emailColumn;
    @FXML private TableColumn<Supplier, String> addressColumn;
    @FXML private TableColumn<Supplier, String> phoneColumn; // In FXML, maps to Supplier.phoneNumber

    @FXML private TableView<PurchaseOrder> supplierPOTableView;
    @FXML private TableColumn<PurchaseOrder, String> poIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> prIdColumn; // In FXML, maps to PurchaseOrder.prId
    @FXML private TableColumn<PurchaseOrder, String> poDateColumn; // In FXML, maps to PurchaseOrder.orderDate
    @FXML private TableColumn<PurchaseOrder, String> poStatusColumn;
    @FXML private TableColumn<PurchaseOrder, Boolean> poApprovedColumn; // Custom cell factory will handle this
    @FXML private TableColumn<PurchaseOrder, Void> poActionColumn;

    @FXML private TableView<PurchaseOrder> approvedPOTableView;
    @FXML private TableColumn<PurchaseOrder, String> allPoIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> allPoSupplierColumn;
    @FXML private TableColumn<PurchaseOrder, String> allPrIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> allPoDateColumn;
    @FXML private TableColumn<PurchaseOrder, String> allPoStatusColumn;
    @FXML private TableColumn<PurchaseOrder, Void> allPoActionColumn;

    @FXML private VBox poDetailsBox;
    @FXML private TableView<POItem> poItemsTableView;
    @FXML private TableColumn<POItem, String> poItemCodeColumn;
    @FXML private TableColumn<POItem, Integer> poQuantityColumn;
    // You might want to add a column for unit price in POItem display:
    // @FXML private TableColumn<POItem, String> poItemPriceColumn;


    // --- Data Collections (Instance Variables) ---
    private ObservableList<Supplier> allSuppliersData = FXCollections.observableArrayList();
    private Map<String, Supplier> allSuppliersMapCache; // Cache for suppliers

    private ObservableList<PurchaseOrder> allPOsMasterData = FXCollections.observableArrayList(); // Holds ALL POs loaded
    private Map<String, PurchaseOrder> allPOsMapCache; // Cache for all POs

    private ObservableList<PurchaseOrder> supplierPOsData = FXCollections.observableArrayList();
    private ObservableList<PurchaseOrder> allApprovedPOsData = FXCollections.observableArrayList();
    private ObservableList<POItem> selectedPOItemsData = FXCollections.observableArrayList();

    private PurchaseOrder currentlySelectedPO;

    @Override // ADDED @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { // Correct signature
        loadMasterSupplierData(); // Load suppliers first for PO loading dependency

        configureSupplierTable();
        configureSupplierPOTable();
        configureApprovedPOTable();
        configurePOItemsTable();

        loadInitialPOData(); // Load POs after suppliers are available

        // Add listeners for table selections
        if (supplierTableView != null) {
            supplierTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadPOsForSupplier(newSelection);
                        clearPODetails();
                    }
                });
        }
        if (supplierPOTableView != null) {
            supplierPOTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> handlePOTableSelection(newSelection));
        }
        if (approvedPOTableView != null) {
            approvedPOTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> handlePOTableSelection(newSelection));
        }

        if (poDetailsBox != null) {
            poDetailsBox.setVisible(false);
            poDetailsBox.setManaged(false);
        }
    }

    private void handlePOTableSelection(PurchaseOrder newSelection) {
        if (newSelection != null) {
            showPODetails(newSelection);
            currentlySelectedPO = newSelection;
        } else {
            clearPODetails();
        }
    }

    private void loadMasterSupplierData() {
        try {
            allSuppliersMapCache = Supplier.loadSuppliers(); // From Supplier.java
            if (allSuppliersMapCache != null) {
                allSuppliersData.setAll(allSuppliersMapCache.values());
            } else {
                allSuppliersMapCache = new HashMap<>(); // Ensure not null
                allSuppliersData.clear();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Supplier Load Error", "Could not load suppliers: " + e.getMessage());
            allSuppliersMapCache = new HashMap<>(); // Ensure not null on error
        }
    }


    private void configureSupplierTable() {
        if (supplierIdColumn != null) supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        if (supplierNameColumn != null) supplierNameColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        if (contactPersonColumn != null) contactPersonColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        if (emailColumn != null) emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (phoneColumn != null) phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber")); // Supplier model uses phoneNumber
        if (addressColumn != null) addressColumn.setCellValueFactory(new PropertyValueFactory<>("address")); // Assuming Supplier has address
        // if (registrationDateColumn != null) registrationDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate")); // Assuming Supplier has registrationDate

        if (supplierTableView != null) supplierTableView.setItems(allSuppliersData);
    }

    private void configureSupplierPOTable() {
        if (poIdColumn != null) poIdColumn.setCellValueFactory(new PropertyValueFactory<>("poId"));
        if (prIdColumn != null) prIdColumn.setCellValueFactory(new PropertyValueFactory<>("prId")); // PO has prId
        if (poDateColumn != null) poDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate")); // PO has orderDate
        if (poStatusColumn != null) poStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (poApprovedColumn != null) {
            // Custom cell for boolean. PurchaseOrder needs an isApproved() method.
            poApprovedColumn.setCellValueFactory(new PropertyValueFactory<>("approved")); // This relies on PO having an isApproved() -> Boolean getter
            poApprovedColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                         // If PurchaseOrder's isApproved() returns boolean, this works.
                         // Or, directly get the PurchaseOrder object and call its isApproved() method.
                        PurchaseOrder po = getTableView().getItems().get(getIndex());
                        setText(po.isConsideredApprovedForPayment()? "Yes" : "No");
                    }
                }
            });
        }
        if (poActionColumn != null) addSelectPOButtonToTable(poActionColumn, supplierPOTableView);
        if (supplierPOTableView != null) supplierPOTableView.setItems(supplierPOsData);
    }

    private void configureApprovedPOTable() {
        if (allPoIdColumn != null) allPoIdColumn.setCellValueFactory(new PropertyValueFactory<>("poId"));
        if (allPoSupplierColumn != null) allPoSupplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierNameDisplay")); // PO has supplierNameDisplay
        if (allPrIdColumn != null) allPrIdColumn.setCellValueFactory(new PropertyValueFactory<>("prId"));
        if (allPoDateColumn != null) allPoDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        if (allPoStatusColumn != null) allPoStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (allPoActionColumn != null) addSelectPOButtonToTable(allPoActionColumn, approvedPOTableView);
        if (approvedPOTableView != null) approvedPOTableView.setItems(allApprovedPOsData);
    }

    private void configurePOItemsTable() {
        if (poItemCodeColumn != null) poItemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        if (poQuantityColumn != null) poQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        // If you add unit price to POItem display:
        // if (poItemPriceColumn != null) poItemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        if (poItemsTableView != null) poItemsTableView.setItems(selectedPOItemsData);
    }

    private void addSelectPOButtonToTable(TableColumn<PurchaseOrder, Void> column, TableView<PurchaseOrder> tableView) {
        column.setCellFactory(param -> new TableCell<>() {
            private final Button actionButton = new Button(); // Text set dynamically
            {
                actionButton.setOnAction(event -> {
                    PurchaseOrder po = getTableView().getItems().get(getIndex());
                    tableView.getSelectionModel().select(po); // Ensure row is selected
                    // Action depends on status (view details or select for payment)
                    showPODetails(po); // Always show details for now
                    currentlySelectedPO = po;
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PurchaseOrder po = getTableView().getItems().get(getIndex());
                    if (po.isConsideredApprovedForPayment()&& !"Paid".equalsIgnoreCase(po.getStatus()) && !"Processed".equalsIgnoreCase(po.getStatus()) && !"Cancelled".equalsIgnoreCase(po.getStatus())) {
                        actionButton.setText("Pay/Details");
                        setGraphic(actionButton);
                    } else if (!po.isConsideredApprovedForPayment()&& !"Paid".equalsIgnoreCase(po.getStatus()) && !"Processed".equalsIgnoreCase(po.getStatus()) && !"Cancelled".equalsIgnoreCase(po.getStatus())){
                        actionButton.setText("View Details");
                        setGraphic(actionButton);
                    } else {
                        setText(po.getStatus()); // Show status like "Paid", "Cancelled"
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadInitialPOData() { // Renamed from loadInitialData
        try {
            // allSuppliersMapCache should be loaded by now from loadMasterSupplierData()
            if (allSuppliersMapCache == null) {
                System.err.println("Supplier map cache is null in loadInitialPOData. Load suppliers first.");
                loadMasterSupplierData(); // Attempt to load if not already
            }
            allPOsMapCache = PurchaseOrder.loadPOs(this.allSuppliersMapCache); // Pass supplier map
            if (allPOsMapCache == null) allPOsMapCache = new HashMap<>();

            allPOsMasterData.setAll(allPOsMapCache.values());

            // Populate "All Approved POs" tab
            allApprovedPOsData.setAll(
                allPOsMasterData.stream()
                    // Filter by status OR by calling isApproved() method on PurchaseOrder
                    .filter(PurchaseOrder::isConsideredApprovedForPayment) // Assumes PO.isApproved() checks relevant statuses
                    .collect(Collectors.toList())
            );

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Data Load Error", "Failed to load PO data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPOsForSupplier(Supplier supplier) {
        supplierPOsData.setAll(
            allPOsMasterData.stream() // Filter from the master list of all POs
                .filter(po -> po.getSupplierId() != null && po.getSupplierId().equals(supplier.getSupplierId()))
                .collect(Collectors.toList())
        );
    }

    private void showPODetails(PurchaseOrder po) {
        if (po != null && poDetailsBox != null && poItemsTableView != null) {
            selectedPOItemsData.setAll(po.getItems()); // Assumes PO.getItems() returns ObservableList<POItem>
            poDetailsBox.setVisible(true);
            poDetailsBox.setManaged(true);
        } else {
            clearPODetails();
        }
    }

    private void clearPODetails() {
        if (selectedPOItemsData != null) selectedPOItemsData.clear();
        if (poDetailsBox != null) {
            poDetailsBox.setVisible(false);
            poDetailsBox.setManaged(false);
        }
        currentlySelectedPO = null;
    }

    @FXML
    private void handleProcessPayment(ActionEvent event) {
        if (currentlySelectedPO == null) {
            showAlert(Alert.AlertType.WARNING, "No PO Selected", "Please select a Purchase Order to process payment.");
            return;
        }
        if (!currentlySelectedPO.isConsideredApprovedForPayment()) {
             showAlert(Alert.AlertType.WARNING, "Payment Process", "PO " + currentlySelectedPO.getPoId() + " is not approved for payment.");
             return;
        }
        if ("Paid".equalsIgnoreCase(currentlySelectedPO.getStatus()) || "Processed".equalsIgnoreCase(currentlySelectedPO.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Payment Process", "PO " + currentlySelectedPO.getPoId() + " has already been paid/processed.");
            return;
        }
        if ("Cancelled".equalsIgnoreCase(currentlySelectedPO.getStatus())) {
            showAlert(Alert.AlertType.ERROR, "Payment Process", "PO " + currentlySelectedPO.getPoId() + " is cancelled and cannot be paid.");
            return;
        }


        // --- Simulate actual payment processing ---
        System.out.println("Processing payment for PO: " + currentlySelectedPO.getPoId() +
                           " for Supplier: " + currentlySelectedPO.getSupplierNameDisplay()); // Use supplierNameDisplay

        // Update PO status in memory
        String oldStatus = currentlySelectedPO.getStatus();
        currentlySelectedPO.setStatus("Paid");
        // Update finance manager and approval date if applicable for "Paid" status
        // currentlySelectedPO.setFinanceManagerId("USER_FINANCE_01"); // Example: Logged in finance user
        // currentlySelectedPO.setApprovalDate(LocalDate.now());


        // Update the PO in our main cache
        if (allPOsMapCache != null) {
            allPOsMapCache.put(currentlySelectedPO.getPoId(), currentlySelectedPO);
        }

        // Refresh tables
        if (supplierPOTableView != null) supplierPOTableView.refresh();
        if (approvedPOTableView != null) approvedPOTableView.refresh();
        // The item in allPOsMasterData is the same instance, so it's also updated.

        // Persist changes to file
        try {
            PurchaseOrder.savePOs(allPOsMapCache); // Save the entire updated PO map
            showAlert(Alert.AlertType.INFORMATION, "Payment Processed",
                    "Payment for PO " + currentlySelectedPO.getPoId() + " has been processed.");
            clearPODetails(); // Clear details and selection after processing
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save PO after payment: " + e.getMessage());
            // Rollback status in memory if save failed
            currentlySelectedPO.setStatus(oldStatus);
            if (allPOsMapCache != null) allPOsMapCache.put(currentlySelectedPO.getPoId(), currentlySelectedPO); // Put back old state
            if (supplierPOTableView != null) supplierPOTableView.refresh();
            if (approvedPOTableView != null) approvedPOTableView.refresh();
            e.printStackTrace();
        }
    }
    
    @FXML
    public void test(){
        System.out.println("hi");
    }

}