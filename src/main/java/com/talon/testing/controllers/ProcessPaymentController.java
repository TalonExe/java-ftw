package com.talon.testing.controllers;

import com.talon.testing.models.POItem;
import com.talon.testing.models.PurchaseOrder;
import com.talon.testing.models.Supplier;
import com.talon.testing.models.StockTransaction; // Import StockTransaction

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
import java.util.Map;
import java.util.HashMap;
import java.util.List; // For loading transactions
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProcessPaymentController extends Switchable implements Initializable {

    // --- FXML Injections for Supplier Tab ---
    @FXML private TableView<Supplier> supplierTableView;
    @FXML private TableColumn<Supplier, String> supplierIdColumn;
    @FXML private TableColumn<Supplier, String> supplierNameColumn;
    @FXML private TableColumn<Supplier, String> contactPersonColumn;
    @FXML private TableColumn<Supplier, String> emailColumn;
    @FXML private TableColumn<Supplier, String> phoneColumn;
    // @FXML private TableColumn<Supplier, String> addressColumn; // Add if in FXML

    @FXML private TableView<PurchaseOrder> supplierPOTableView;
    @FXML private TableColumn<PurchaseOrder, String> poIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> prIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> poDateColumn;
    @FXML private TableColumn<PurchaseOrder, String> poStatusColumn;
    @FXML private TableColumn<PurchaseOrder, String> poApprovedColumn; // Changed generic to String for text display
    @FXML private TableColumn<PurchaseOrder, Void> poActionColumn;

    // --- FXML Injections for All Approved POs Tab ---
    @FXML private TableView<PurchaseOrder> approvedPOTableView;
    @FXML private TableColumn<PurchaseOrder, String> allPoIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> allPoSupplierColumn;
    @FXML private TableColumn<PurchaseOrder, String> allPrIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> allPoDateColumn;
    @FXML private TableColumn<PurchaseOrder, String> allPoStatusColumn;
    @FXML private TableColumn<PurchaseOrder, Void> allPoActionColumn;

    // --- FXML Injections for PO Details Pane ---
    @FXML private VBox poDetailsBox;
    @FXML private TableView<POItem> poItemsTableView;
    @FXML private TableColumn<POItem, String> poItemCodeColumn;
    @FXML private TableColumn<POItem, String> poItemNameDisplayColumn; // Added
    @FXML private TableColumn<POItem, Integer> poQuantityColumn;
    @FXML private TableColumn<POItem, String> poItemUnitPriceColumn;   // Added

    // --- FXML Injections for Stock Transactions Table ---
    @FXML private TableView<StockTransaction> stockTransactionsTableView;
    @FXML private TableColumn<StockTransaction, String> stItemCodeCol;
    @FXML private TableColumn<StockTransaction, String> stItemNameCol;
    @FXML private TableColumn<StockTransaction, String> stTypeCol;
    @FXML private TableColumn<StockTransaction, Integer> stQtyChangeCol;
    @FXML private TableColumn<StockTransaction, Integer> stStockAfterCol;
    @FXML private TableColumn<StockTransaction, String> stTimestampCol;
    @FXML private TableColumn<StockTransaction, String> stUserCol;
    @FXML private Button processPaymentButton;


    // --- Data Collections (Instance Variables) ---
    private ObservableList<Supplier> allSuppliersData = FXCollections.observableArrayList();
    private Map<String, Supplier> allSuppliersMapCache;

    private ObservableList<PurchaseOrder> allPOsMasterData = FXCollections.observableArrayList();
    private Map<String, PurchaseOrder> allPOsMapCache;

    private ObservableList<PurchaseOrder> supplierPOsData = FXCollections.observableArrayList();
    private ObservableList<PurchaseOrder> allApprovedPOsData = FXCollections.observableArrayList();
    private ObservableList<POItem> selectedPOItemsData = FXCollections.observableArrayList();
    private ObservableList<StockTransaction> poStockTransactionsData = FXCollections.observableArrayList();
    private List<StockTransaction> allStockTransactionsCache; // All transactions loaded once

    private PurchaseOrder currentlySelectedPO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMasterSupplierData();
        loadAllStockTransactions(); // Load these early

        configureSupplierTable();
        configureSupplierPOTable();
        configureApprovedPOTable();
        configurePOItemsTable();
        configureStockTransactionsTable(); // Configure the new table

        loadInitialPOData();

        setupTableSelectionListeners();

        if (poDetailsBox != null) {
            poDetailsBox.setVisible(false);
            poDetailsBox.setManaged(false);
        }
        // onAction for processPaymentButton is set in FXML
    }

    private void setupTableSelectionListeners() {
        if (supplierTableView != null) {
            supplierTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadPOsForSupplier(newSelection);
                        clearPODetails();
                    } else {
                        supplierPOsData.clear(); // Clear POs if no supplier selected
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
    }

    private void handlePOTableSelection(PurchaseOrder newSelection) {
        if (newSelection != null) {
            currentlySelectedPO = newSelection;
            showPODetails(currentlySelectedPO);
            // Highlight the selected PO in both tables if it exists in both
            if (supplierPOTableView.getItems().contains(newSelection)) supplierPOTableView.getSelectionModel().select(newSelection);
            if (approvedPOTableView.getItems().contains(newSelection)) approvedPOTableView.getSelectionModel().select(newSelection);

        } else {
            clearPODetails();
        }
    }

    private void loadMasterSupplierData() {
        try {
            allSuppliersMapCache = Supplier.loadSuppliers();
            if (allSuppliersMapCache != null) {
                allSuppliersData.setAll(allSuppliersMapCache.values());
            } else {
                allSuppliersMapCache = new HashMap<>();
                allSuppliersData.clear();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Supplier Load Error", "Could not load suppliers: " + e.getMessage());
            allSuppliersMapCache = new HashMap<>();
        }
    }

    private void loadAllStockTransactions() {
        try {
            allStockTransactionsCache = StockTransaction.loadTransactions();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Transaction Load Error", "Could not load stock transactions: " + e.getMessage());
            allStockTransactionsCache = FXCollections.observableArrayList();
        }
        if (allStockTransactionsCache == null) allStockTransactionsCache = FXCollections.observableArrayList();
    }

    private void configureSupplierTable() {
        if (supplierIdColumn != null) supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        if (supplierNameColumn != null) supplierNameColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        if (contactPersonColumn != null) contactPersonColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        if (emailColumn != null) emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (phoneColumn != null) phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        // if (addressColumn != null) addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        if (supplierTableView != null) supplierTableView.setItems(allSuppliersData);
    }

    private void configureSupplierPOTable() {
        if (poIdColumn != null) poIdColumn.setCellValueFactory(new PropertyValueFactory<>("poId"));
        if (prIdColumn != null) prIdColumn.setCellValueFactory(new PropertyValueFactory<>("prId"));
        if (poDateColumn != null) poDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        if (poStatusColumn != null) poStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (poApprovedColumn != null) {
            poApprovedColumn.setCellValueFactory(new PropertyValueFactory<>("status")); // Base on status
            poApprovedColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String itemStatus, boolean empty) { // item is status string
                    super.updateItem(itemStatus, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        PurchaseOrder po = (PurchaseOrder) getTableRow().getItem();
                        setText(po.isConsideredApprovedForPayment() ? "Yes" : "No");
                    }
                }
            });
        }
        if (poActionColumn != null) addSelectPOButtonToTable(poActionColumn, supplierPOTableView);
        if (supplierPOTableView != null) supplierPOTableView.setItems(supplierPOsData);
    }

    private void configureApprovedPOTable() {
        if (allPoIdColumn != null) allPoIdColumn.setCellValueFactory(new PropertyValueFactory<>("poId"));
        if (allPoSupplierColumn != null) allPoSupplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierNameDisplay"));
        if (allPrIdColumn != null) allPrIdColumn.setCellValueFactory(new PropertyValueFactory<>("prId"));
        if (allPoDateColumn != null) allPoDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        if (allPoStatusColumn != null) allPoStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (allPoActionColumn != null) addSelectPOButtonToTable(allPoActionColumn, approvedPOTableView);
        if (approvedPOTableView != null) approvedPOTableView.setItems(allApprovedPOsData);
    }

    private void configurePOItemsTable() {
        if (poItemCodeColumn != null) poItemCodeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        if (poItemNameDisplayColumn != null) poItemNameDisplayColumn.setCellValueFactory(new PropertyValueFactory<>("itemNameDisplay"));
        if (poQuantityColumn != null) poQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        if (poItemUnitPriceColumn != null) poItemUnitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        if (poItemsTableView != null) poItemsTableView.setItems(selectedPOItemsData);
    }

    private void configureStockTransactionsTable() {
        if (stockTransactionsTableView != null) {
            stItemCodeCol.setCellValueFactory(new PropertyValueFactory<>("itemId"));
            stItemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemNameDisplay"));
            stTypeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
            stQtyChangeCol.setCellValueFactory(new PropertyValueFactory<>("quantityChange"));
            stStockAfterCol.setCellValueFactory(new PropertyValueFactory<>("stockLevelAfterTransaction"));
            stTimestampCol.setCellValueFactory(new PropertyValueFactory<>("transactionTimestamp"));
            stUserCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
            stockTransactionsTableView.setItems(poStockTransactionsData);
        }
    }

    private void addSelectPOButtonToTable(TableColumn<PurchaseOrder, Void> column, TableView<PurchaseOrder> tableView) {
        column.setCellFactory(param -> new TableCell<>() {
            private final Button actionButton = new Button();
            {
                actionButton.setOnAction(event -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        PurchaseOrder po = (PurchaseOrder) getTableRow().getItem();
                        tableView.getSelectionModel().select(po);
                        handlePOTableSelection(po); // Use common handler
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); setText(null);
                } else {
                    PurchaseOrder po = (PurchaseOrder) getTableRow().getItem();
                    if (po.isConsideredApprovedForPayment() && !"Paid".equalsIgnoreCase(po.getStatus()) && !"Processed".equalsIgnoreCase(po.getStatus()) && !"Cancelled".equalsIgnoreCase(po.getStatus())) {
                        actionButton.setText("Pay/Details"); setGraphic(actionButton); setText(null);
                    } else if (!"Paid".equalsIgnoreCase(po.getStatus()) && !"Processed".equalsIgnoreCase(po.getStatus()) && !"Cancelled".equalsIgnoreCase(po.getStatus())) {
                        actionButton.setText("View Details"); setGraphic(actionButton); setText(null);
                    } else {
                        setText(po.getStatus()); setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadInitialPOData() {
        try {
            if (allSuppliersMapCache == null) loadMasterSupplierData();
            allPOsMapCache = PurchaseOrder.loadPOs(this.allSuppliersMapCache);
            if (allPOsMapCache == null) allPOsMapCache = new HashMap<>();
            allPOsMasterData.setAll(allPOsMapCache.values());

            allApprovedPOsData.setAll(
                allPOsMasterData.stream()
                    .filter(PurchaseOrder::isConsideredApprovedForPayment)
                    .filter(po -> !"Paid".equalsIgnoreCase(po.getStatus()) && !"Processed".equalsIgnoreCase(po.getStatus()) && !"Cancelled".equalsIgnoreCase(po.getStatus())) // Show only those needing payment
                    .collect(Collectors.toList())
            );
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "PO Load Error", "Failed to load PO data: " + e.getMessage());
        }
    }

    private void loadPOsForSupplier(Supplier supplier) {
        supplierPOsData.setAll(
            allPOsMasterData.stream()
                .filter(po -> po.getSupplierId() != null && po.getSupplierId().equals(supplier.getSupplierId()))
                .collect(Collectors.toList())
        );
    }

    private void showPODetails(PurchaseOrder po) {
        if (po != null && poDetailsBox != null && poItemsTableView != null) {
            selectedPOItemsData.setAll(po.getItems());
            loadStockTransactionsForPO(po.getPoId());
            poDetailsBox.setVisible(true);
            poDetailsBox.setManaged(true);
        } else {
            clearPODetails();
        }
    }

    private void loadStockTransactionsForPO(String poId) {
        poStockTransactionsData.clear();
        if (poId == null || allStockTransactionsCache == null) return;
        poStockTransactionsData.addAll(
            allStockTransactionsCache.stream()
                .filter(st -> poId.equals(st.getPoId()) && "PO_RECEIPT".equals(st.getTransactionType()))
                .collect(Collectors.toList())
        );
        if (stockTransactionsTableView != null) stockTransactionsTableView.refresh();
    }

    private void clearPODetails() {
        if (selectedPOItemsData != null) selectedPOItemsData.clear();
        if (poStockTransactionsData != null) poStockTransactionsData.clear();
        if (poDetailsBox != null) {
            poDetailsBox.setVisible(false);
            poDetailsBox.setManaged(false);
        }
        currentlySelectedPO = null;
        // Clear selection from tables to avoid re-triggering
        if(supplierPOTableView != null) supplierPOTableView.getSelectionModel().clearSelection();
        if(approvedPOTableView != null) approvedPOTableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleProcessPayment(ActionEvent event) {
        if (currentlySelectedPO == null) {
            showAlert(Alert.AlertType.WARNING, "No PO Selected", "Please select a PO to process payment.");
            return;
        }
        if (!currentlySelectedPO.isConsideredApprovedForPayment()) {
             showAlert(Alert.AlertType.WARNING, "Payment Process", "PO " + currentlySelectedPO.getPoId() + " is not approved for payment.");
             return;
        }
        if ("Paid".equalsIgnoreCase(currentlySelectedPO.getStatus()) || "Processed".equalsIgnoreCase(currentlySelectedPO.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Payment Process", "PO " + currentlySelectedPO.getPoId() + " already paid/processed.");
            return;
        }
        if ("Cancelled".equalsIgnoreCase(currentlySelectedPO.getStatus())) {
            showAlert(Alert.AlertType.ERROR, "Payment Process", "PO " + currentlySelectedPO.getPoId() + " is cancelled.");
            return;
        }

        // Stock Verification (Example)
        boolean stockReceiptsFound = !poStockTransactionsData.isEmpty();
        if (!stockReceiptsFound && !currentlySelectedPO.getItems().isEmpty()) {
            Alert confirmNoStock = new Alert(Alert.AlertType.CONFIRMATION,
                    "No stock receipt transactions recorded for PO " + currentlySelectedPO.getPoId() + ".\n" +
                    "Proceed with payment anyway?", ButtonType.YES, ButtonType.NO);
            confirmNoStock.setTitle("Stock Verification");
            Optional<ButtonType> result = confirmNoStock.showAndWait();
            if (result.isEmpty() || result.get() == ButtonType.NO) {
                showAlert(Alert.AlertType.INFORMATION, "Payment Cancelled", "Payment cancelled by user due to no stock receipt.");
                return;
            }
        }
        // More detailed item by item quantity check could be added here if needed.

        System.out.println("Processing payment for PO: " + currentlySelectedPO.getPoId() +
                           " for Supplier: " + currentlySelectedPO.getSupplierNameDisplay());
        String oldStatus = currentlySelectedPO.getStatus();
        currentlySelectedPO.setStatus("Paid");

        if (allPOsMapCache != null) allPOsMapCache.put(currentlySelectedPO.getPoId(), currentlySelectedPO);
        
        // Refresh the list of "approved POs pending payment"
        // Since it's now "Paid", it should be removed from that specific list.
        allApprovedPOsData.remove(currentlySelectedPO);

        // Refresh other tables if they show this PO
        if (supplierPOTableView != null) supplierPOTableView.refresh();


        try {
            PurchaseOrder.savePOs(allPOsMapCache);
            showAlert(Alert.AlertType.INFORMATION, "Payment Processed", "Payment for PO " + currentlySelectedPO.getPoId() + " processed.");
            clearPODetails();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save PO: " + e.getMessage());
            currentlySelectedPO.setStatus(oldStatus); // Rollback
            if (allPOsMapCache != null) allPOsMapCache.put(currentlySelectedPO.getPoId(), currentlySelectedPO);
            if (!allApprovedPOsData.contains(currentlySelectedPO) && currentlySelectedPO.isConsideredApprovedForPayment() &&
                !"Paid".equalsIgnoreCase(oldStatus) && !"Processed".equalsIgnoreCase(oldStatus) && !"Cancelled".equalsIgnoreCase(oldStatus)) {
                 allApprovedPOsData.add(currentlySelectedPO); // Add back if it was removed and should still be there
            }
            if (supplierPOTableView != null) supplierPOTableView.refresh();
            e.printStackTrace();
        }
    }
}