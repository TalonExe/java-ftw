package com.talon.testing.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
// import com.talon.testing.models.PurchaseOrder; // Not used here
import com.talon.testing.models.PurchaseRequisition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // IMPORT THIS
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType; // For confirmation dialogs
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle; // IMPORT THIS

public class FinanceManagerController extends Switchable implements Initializable { // IMPLEMENT Initializable
    private static final String PR_FILE_PATH = "/data/PR.txt";
    private static final Type PR_MAP_TYPE = new TypeToken<Map<String, PurchaseRequisition>>() {}.getType();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // --- FXML Injections as per your FXML ---
    @FXML
    private TableView<PurchaseRequisition> requisitionTableView;

    @FXML
    private TableColumn<PurchaseRequisition, String> prIdColumn;       // Text: "Requisition ID"
    @FXML
    private TableColumn<PurchaseRequisition, String> managerIdColumn;  // Text: "Manager ID"
    @FXML
    private TableColumn<PurchaseRequisition, String> itemIdColumn;     // Text: "Date Requested" (mapping to createdAt)
    @FXML
    private TableColumn<PurchaseRequisition, String> quantityColumn;   // Text: "Required By" (mapping to requiredDate)
    @FXML
    private TableColumn<PurchaseRequisition, String> statusColumn;     // Text: "Items" (mapping to ItemID or ItemNameDisplay)
    @FXML
    private TableColumn<PurchaseRequisition, Integer> createdAtColumn; // Text: "Quantity" (mapping to quantity)

    // INSTANCE MEMBERS - not static
    private ObservableList<PurchaseRequisition> requisitionData = FXCollections.observableArrayList();
    private Map<String, PurchaseRequisition> prMapCache;

    @Override // ADDED
    public void initialize(URL url, ResourceBundle resourceBundle) { // CORRECTED SIGNATURE
        // Ensure TableView and Columns are not null before configuring
        if (requisitionTableView != null) {
            // Set up the table columns based on FXML text headers
            if (prIdColumn != null)
                prIdColumn.setCellValueFactory(new PropertyValueFactory<>("prId")); // Correct
            if (managerIdColumn != null)
                managerIdColumn.setCellValueFactory(new PropertyValueFactory<>("salesManagerId")); // Correct

            // Mapping based on FXML text="Date Requested" for itemIdColumn fx:id
            if (itemIdColumn != null)
                itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt")); // PR.getCreatedAt()

            // Mapping based on FXML text="Required By" for quantityColumn fx:id
            // This assumes your PurchaseRequisition model has a 'getRequiredDate()' method.
            // If not, this column will be blank or you need to map to something else.
            if (quantityColumn != null)
                quantityColumn.setCellValueFactory(new PropertyValueFactory<>("requiredDate"));

            // Mapping based on FXML text="Items" for statusColumn fx:id
            // This will show the ItemID. If you added itemNameDisplay to PR, use that.
            if (statusColumn != null)
                statusColumn.setCellValueFactory(new PropertyValueFactory<>("ItemID")); // Or "itemNameDisplay"

            // Mapping based on FXML text="Quantity" for createdAtColumn fx:id
            // This column will show the quantity.
            if (createdAtColumn != null)
                createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

            requisitionTableView.setItems(this.requisitionData);
        }
        loadPRData();
    }

    private void loadPRData() {
        try {
            this.prMapCache = loadPRsFromFileStatic();
            System.out.println("Loaded PRs from FinanceManagerController: " + this.prMapCache);
            this.requisitionData.clear();
            if (this.prMapCache != null) {
                // If PR model doesn't store requiredDate or ItemNameDisplay directly,
                // you might need to populate them here after loading from other sources.
                // For now, assuming they are in the PR object or will be handled by getters.
                this.requisitionData.addAll(this.prMapCache.values());
            }
            if (requisitionTableView != null) {
                 requisitionTableView.refresh(); // Good to refresh after setting items
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load Purchase Requisitions: " + e.getMessage());
        }
    }

    @FXML
    public void test() {
        System.out.println("com.talon.testing.controllers.FinanceManagerController.test() called");
        // Example: Add a PR with requiredDate if your model supports it
        // addPR("FMC_PR001", "SM00TEST", "ITEM_TEST", 10, "Pending", LocalDate.now(), LocalDate.now().plusDays(7));
    }

    // --- ADD PR Function (Instance Method, adapted) ---
    // Assuming your PR model might have requiredDate now
    public boolean addPR(String prId, String salesManagerId, String itemId, int quantity, String status,
                         LocalDate createdAt, LocalDate requiredByDate) { // Added requiredByDate
        if (prId == null || prId.trim().isEmpty() || salesManagerId == null || salesManagerId.trim().isEmpty() ||
            itemId == null || itemId.trim().isEmpty() || quantity <= 0) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "PR ID, Manager ID, Item ID are required, Quantity must be positive.");
            return false;
        }
        if (status == null || status.trim().isEmpty()) status = "Pending";
        if (createdAt == null) createdAt = LocalDate.now();

        if (this.prMapCache == null) {
            loadPRData(); // Ensure cache is loaded
            if (this.prMapCache == null) {
                showAlert(Alert.AlertType.ERROR, "Data Error", "Could not load PR data.");
                return false;
            }
        }
        if (this.prMapCache.containsKey(prId)) {
            showAlert(Alert.AlertType.ERROR, "Duplicate ID", "PR with ID '" + prId + "' already exists.");
            return false;
        }

        // Use the constructor from your PurchaseRequisition model
        // It needs to handle requiredDate if you use it.
        PurchaseRequisition newPR = new PurchaseRequisition(); // Use default constructor
        newPR.setPrId(prId);
        newPR.setSalesManagerId(salesManagerId);
        newPR.setItemID(itemId); // Correct setter from your model
        newPR.setQuantity(quantity);
        newPR.setStatus(status);
        newPR.setCreatedAt(createdAt); // Setter takes LocalDate
        if (requiredByDate != null) { // Check if your model has setRequiredDate
             // newPR.setRequiredDate(requiredByDate.toString()); // Example
        }


        this.prMapCache.put(newPR.getPrId(), newPR);
        this.requisitionData.add(newPR);

        try {
            savePRsToFileStatic(this.prMapCache);
            System.out.println("PR '" + prId + "' added and saved.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save PR: " + e.getMessage());
            this.prMapCache.remove(newPR.getPrId());
            this.requisitionData.remove(newPR);
            return false;
        }
    }

    // --- DELETE PR Function (Instance Method) ---
    public boolean deletePR(String prId) {
        if (prId == null || prId.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "PR ID to delete cannot be empty.");
            return false;
        }
        if (this.prMapCache == null) { loadPRData(); if (this.prMapCache == null) { showAlert(Alert.AlertType.ERROR, "Data Error", "Could not load PR data."); return false; } }
        if (!this.prMapCache.containsKey(prId)) { showAlert(Alert.AlertType.WARNING, "Not Found", "PR with ID '" + prId + "' not found."); return false; }

        PurchaseRequisition prToRemove = this.prMapCache.get(prId);
        this.prMapCache.remove(prId);
        this.requisitionData.remove(prToRemove);

        try {
            savePRsToFileStatic(this.prMapCache);
            System.out.println("PR '" + prId + "' deleted and saved.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save deletion: " + e.getMessage());
            if (prToRemove != null) { this.prMapCache.put(prId, prToRemove); this.requisitionData.add(prToRemove); } // Rollback
            return false;
        }
    }

    // --- FXML Action handler for a delete button (if you add one to this FXML) ---
    /*
    @FXML
    private void handleDeleteSelectedPRAction(ActionEvent event) {
        if (requisitionTableView == null) return;
        PurchaseRequisition selectedPR = requisitionTableView.getSelectionModel().getSelectedItem();
        if (selectedPR == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a PR to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete PR " + selectedPR.getPrId() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (deletePR(selectedPR.getPrId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "PR deleted.");
                }
            }
        });
    }
    */


    // ------------- STATIC File I/O Methods (Ensure these are robust) -------------
    public static Map<String, PurchaseRequisition> loadPRsFromFileStatic() throws IOException {
        Map<String, PurchaseRequisition> loadedPRMap = new HashMap<>();
        try (InputStream inputStream = FinanceManagerController.class.getResourceAsStream(PR_FILE_PATH)) {
            if (inputStream == null) {
                System.err.println("PR file not found: " + PR_FILE_PATH + ". Attempting to create.");
                File file = getFileFromResource(PR_FILE_PATH, true);
                if (file != null && file.exists() && file.length() == 0) {
                    System.out.println("Empty " + PR_FILE_PATH + " created at: " + file.getAbsolutePath());
                    try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) { writer.write("{}"); }
                } else if (file == null || !file.exists()){
                     System.err.println("Could not create or access PR file path on load: " + PR_FILE_PATH);
                }
                return loadedPRMap;
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                loadedPRMap = gson.fromJson(reader, PR_MAP_TYPE);
                if (loadedPRMap == null) loadedPRMap = new HashMap<>();
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from PR data file: " + PR_FILE_PATH + ". File might be empty or malformed.");
            throw new IOException("Error parsing JSON. Ensure PR.txt contains valid JSON (e.g., {} if empty).", e);
        }
        return loadedPRMap;
    }

    public static void savePRsToFileStatic(Map<String, PurchaseRequisition> prMapToSave) throws IOException {
        File file = getFileFromResource(PR_FILE_PATH, true);
        if (file == null) throw new IOException("Could not get file for saving PRs: " + PR_FILE_PATH);
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(prMapToSave, writer);
            System.out.println("Purchase Requisitions saved to: " + file.getAbsolutePath());
        }
    }
    private static File getFileFromResource(String resourcePath, boolean createIfNotFound) throws IOException {
        URL resourceUrl = FinanceManagerController.class.getResource(resourcePath);
        File file;
        if (resourceUrl != null) {
            try { file = new File(resourceUrl.toURI()); }
            catch (URISyntaxException e) { throw new IOException("Invalid resource URI: " + resourcePath, e); }
        } else {
            System.out.println("Resource " + resourcePath + " not found. Trying to create in build/user dir.");
            URL rootPath = FinanceManagerController.class.getProtectionDomain().getCodeSource().getLocation();
            File baseDir;
            try {
                baseDir = new File(rootPath.toURI());
                file = new File( (baseDir.isDirectory() ? baseDir.getAbsolutePath() : baseDir.getParentFile().getAbsolutePath()) + resourcePath);
            } catch (Exception e) {
                String fallbackPath = System.getProperty("user.home") + File.separator + "TalonAppData" + resourcePath.replace('/', File.separatorChar);
                System.err.println("Fallback to: " + fallbackPath + " due to: " + e.getMessage());
                file = new File(fallbackPath);
            }
            if (createIfNotFound && !file.exists()) {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                     System.err.println("Could not create dir: " + parentDir); return null;
                }
                System.out.println("Attempting to create: " + file.getAbsolutePath());
                if (!file.createNewFile()) { System.err.println("Could not create file: " + file); return null; }
                if (file.length() == 0) { try (Writer w = new FileWriter(file)) { w.write("{}"); } } // Initialize if new
            }
        }
        return file;
    }

}