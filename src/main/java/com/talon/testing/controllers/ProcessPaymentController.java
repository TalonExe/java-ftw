package com.talon.testing.controllers;

import com.talon.testing.models.POItem;
import com.talon.testing.models.PurchaseOrder;
import com.talon.testing.models.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProcessPaymentController extends Switchable {

    // --- Left Pane Navigation Buttons (fx:id not strictly needed if only onAction is used) ---

    // --- Center Pane: Tab "By Supplier" ---
    @FXML private TableView<Supplier> supplierTableView;
    @FXML private TableColumn<Supplier, String> supplierIdColumn;
    @FXML private TableColumn<Supplier, String> supplierNameColumn;
    @FXML private TableColumn<Supplier, String> contactPersonColumn;
    @FXML private TableColumn<Supplier, String> emailColumn;
    @FXML private TableColumn<Supplier, String> phoneColumn;

    @FXML private TableView<PurchaseOrder> supplierPOTableView;
    @FXML private TableColumn<PurchaseOrder, String> poIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> prIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> poDateColumn;
    @FXML private TableColumn<PurchaseOrder, String> poStatusColumn;
    @FXML private TableColumn<PurchaseOrder, Boolean> poApprovedColumn; // For boolean display
    @FXML private TableColumn<PurchaseOrder, Void> poActionColumn; // For a button

    // --- Center Pane: Tab "All Approved POs" ---
    @FXML private TableView<PurchaseOrder> approvedPOTableView;
    @FXML private TableColumn<PurchaseOrder, String> allPoIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> allPoSupplierColumn; // Will need PO to have supplier name
    @FXML private TableColumn<PurchaseOrder, String> allPrIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> allPoDateColumn;
    @FXML private TableColumn<PurchaseOrder, String> allPoStatusColumn;
    @FXML private TableColumn<PurchaseOrder, Void> allPoActionColumn; // For a button

    // --- Center Pane: PO Details ---
    @FXML private VBox poDetailsBox;
    @FXML private TableView<POItem> poItemsTableView;
    @FXML private TableColumn<POItem, String> poItemCodeColumn;
    @FXML private TableColumn<POItem, Integer> poQuantityColumn; // Assuming quantity is int

    // Data collections
    private ObservableList<Supplier> allSuppliersData = FXCollections.observableArrayList();
    private ObservableList<PurchaseOrder> allPOsData = FXCollections.observableArrayList(); // All POs loaded
    private ObservableList<PurchaseOrder> supplierPOsData = FXCollections.observableArrayList(); // POs for selected supplier
    private ObservableList<PurchaseOrder> allApprovedPOsData = FXCollections.observableArrayList(); // Filtered approved POs
    private ObservableList<POItem> selectedPOItemsData = FXCollections.observableArrayList();

    private PurchaseOrder currentlySelectedPO;


     public void test(){
         System.out.print("testing 123");
     }
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureSupplierTable();
        configureSupplierPOTable();
        configureApprovedPOTable();
        configurePOItemsTable();

        loadInitialData();

        // Add listeners for table selections
        supplierTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadPOsForSupplier(newSelection);
                        clearPODetails();
                    }
                });

        supplierPOTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showPODetails(newSelection);
                        currentlySelectedPO = newSelection;
                    } else {
                        clearPODetails();
                    }
                });

        approvedPOTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showPODetails(newSelection);
                        currentlySelectedPO = newSelection;
                    } else {
                        clearPODetails();
                    }
                });

        // Initially hide PO details
        poDetailsBox.setVisible(false);
        poDetailsBox.setManaged(false);
    }

    private void configureSupplierTable() {
        supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        supplierNameColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        contactPersonColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        supplierTableView.setItems(allSuppliersData);
    }

    private void configureSupplierPOTable() {
        poIdColumn.setCellValueFactory(new PropertyValueFactory<>("poId"));
        prIdColumn.setCellValueFactory(new PropertyValueFactory<>("requisitionId"));
        poDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        poStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        poApprovedColumn.setCellValueFactory(new PropertyValueFactory<>("approved"));
        // Custom cell for boolean to show "Yes" / "No" or a CheckBox
        poApprovedColumn.setCellFactory(col -> new TableCell<PurchaseOrder, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item ? "Yes" : "No");
            }
        });
        addSelectPOButtonToTable(poActionColumn, supplierPOTableView);
        supplierPOTableView.setItems(supplierPOsData);
    }

    private void configureApprovedPOTable() {
        allPoIdColumn.setCellValueFactory(new PropertyValueFactory<>("poId"));
        allPoSupplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName")); // PO model needs supplierName
        allPrIdColumn.setCellValueFactory(new PropertyValueFactory<>("requisitionId"));
        allPoDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        allPoStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        addSelectPOButtonToTable(allPoActionColumn, approvedPOTableView);
        approvedPOTableView.setItems(allApprovedPOsData);
    }

    private void configurePOItemsTable() {
        poItemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        poQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        poItemsTableView.setItems(selectedPOItemsData);
    }

    private void addSelectPOButtonToTable(TableColumn<PurchaseOrder, Void> column, TableView<PurchaseOrder> tableView) {
        column.setCellFactory(param -> new TableCell<>() {
            private final Button selectButton = new Button("View Details");
            {
                selectButton.setOnAction(event -> {
                    PurchaseOrder po = getTableView().getItems().get(getIndex());
                    tableView.getSelectionModel().select(po); // Select the row
                    showPODetails(po);
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
                    // Only show button for POs that are not yet "Paid" or "Processed"
                    if (!"Paid".equalsIgnoreCase(po.getStatus()) && !"Processed".equalsIgnoreCase(po.getStatus()) && po.isApproved()) {
                         selectButton.setText("Select for Payment");
                         setGraphic(selectButton);
                    } else if (!"Paid".equalsIgnoreCase(po.getStatus()) && !"Processed".equalsIgnoreCase(po.getStatus()) && !po.isApproved()){
                         selectButton.setText("View Details"); // Or "Not Approved"
                         setGraphic(selectButton);
                    }
                    else {
                        setText(po.getStatus()); // Show status if already paid
                        setGraphic(null);
                    }
                }
            }
        });
    }


    private void loadInitialData() {
        try {
            Map<String, Supplier> suppliers = Supplier.loadSuppliers();
            allSuppliersData.setAll(suppliers.values());

            Map<String, PurchaseOrder> pos = PurchaseOrder.loadAllPOs(suppliers);
            allPOsData.setAll(pos.values());

            // Populate "All Approved POs" tab
            allApprovedPOsData.setAll(
                    allPOsData.stream()
                            .filter(PurchaseOrder::isApproved) // or .filter(po -> "Approved".equals(po.getStatus()))
                            .collect(Collectors.toList())
            );

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Data Load Error", "Failed to load initial data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPOsForSupplier(Supplier supplier) {
        supplierPOsData.setAll(
                allPOsData.stream()
                        .filter(po -> po.getSupplierId().equals(supplier.getSupplierId()))
                        .collect(Collectors.toList())
        );
    }

    private void showPODetails(PurchaseOrder po) {
        if (po != null) {
            selectedPOItemsData.setAll(po.getItems());
            poDetailsBox.setVisible(true);
            poDetailsBox.setManaged(true);
        } else {
            clearPODetails();
        }
    }

    private void clearPODetails() {
        selectedPOItemsData.clear();
        poDetailsBox.setVisible(false);
        poDetailsBox.setManaged(false);
        currentlySelectedPO = null;
    }

    @FXML
    private void handleProcessPayment(ActionEvent event) { // Renamed from "test"
        if (currentlySelectedPO != null) {
            if (!currentlySelectedPO.isApproved()) {
                 showAlert(Alert.AlertType.WARNING, "Payment Process", "Cannot process payment for PO " + currentlySelectedPO.getPoId() + ". It is not approved.");
                 return;
            }
            if ("Paid".equalsIgnoreCase(currentlySelectedPO.getStatus()) || "Processed".equalsIgnoreCase(currentlySelectedPO.getStatus())) {
                showAlert(Alert.AlertType.INFORMATION, "Payment Process", "PO " + currentlySelectedPO.getPoId() + " has already been processed.");
                return;
            }

            // Simulate processing payment
            System.out.println("Processing payment for PO: " + currentlySelectedPO.getPoId() +
                               " for Supplier: " + currentlySelectedPO.getSupplierName());
            // TODO: Implement actual payment processing logic
            // This would involve:
            // 1. Calling a service to record the payment.
            // 2. Updating the PO status in your data store (e.g., file, database).
            // 3. Potentially generating a payment voucher/receipt.

            currentlySelectedPO.setStatus("Paid"); // Update status in memory

            // Refresh the tables to reflect the change
            supplierPOTableView.refresh();
            approvedPOTableView.refresh();
            // Update allPOsData as well if it's the source of truth for persistence
            allPOsData.stream()
                .filter(po -> po.getPoId().equals(currentlySelectedPO.getPoId()))
                .findFirst().ifPresent(po -> po.setStatus("Paid"));


            // TODO: Persist this change to your sales.txt or equivalent for POs

            showAlert(Alert.AlertType.INFORMATION, "Payment Processed",
                    "Payment for PO " + currentlySelectedPO.getPoId() + " has been processed.");

            clearPODetails(); // Clear details after processing
        } else {
            showAlert(Alert.AlertType.WARNING, "No PO Selected", "Please select a Purchase Order to process payment.");
        }
    }
}