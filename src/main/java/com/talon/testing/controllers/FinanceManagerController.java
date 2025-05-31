package com.talon.testing.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.talon.testing.models.PurchaseRequisition;
import com.talon.testing.utils.FileUtils; // IMPORT FileUtils

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent; // For navigation methods if FXML calls them
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
// import javafx.scene.control.ButtonType; // Not used in this version of the controller
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.FileInputStream; // For reading from File
import java.io.FileWriter;    // For writing to File (though save is static)
import java.io.IOException;
import java.io.InputStream;     // For FileInputStream
import java.io.InputStreamReader;
import java.io.Writer;        // For FileWriter
import java.lang.reflect.Type;
import java.net.URL;
// import java.net.URISyntaxException; // No longer needed here if FileUtils handles it
// import java.net.URL; // No longer needed here for direct file path logic
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;   // For addPR example
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
// No need for java.net.URL or URISyntaxException if FileUtils handles all pathing

public class FinanceManagerController extends Switchable implements Initializable {
    private static final String PR_FILENAME = "PR.txt"; // Changed to FILENAME
    private static final Type PR_MAP_TYPE = new TypeToken<Map<String, PurchaseRequisition>>() {}.getType();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @FXML
    private TableView<PurchaseRequisition> requisitionTableView;
    @FXML
    private TableColumn<PurchaseRequisition, String> prIdColumn;
    @FXML
    private TableColumn<PurchaseRequisition, String> managerIdColumn;
    @FXML
    private TableColumn<PurchaseRequisition, String> itemIdColumn;     // FXML Text: "Date Requested"
    @FXML
    private TableColumn<PurchaseRequisition, String> quantityColumn;   // FXML Text: "Required By"
    @FXML
    private TableColumn<PurchaseRequisition, String> statusColumn;     // FXML Text: "Items"
    @FXML
    private TableColumn<PurchaseRequisition, Integer> createdAtColumn; // FXML Text: "Quantity"

    // INSTANCE MEMBERS
    private ObservableList<PurchaseRequisition> requisitionData = FXCollections.observableArrayList();
    private Map<String, PurchaseRequisition> prMapCache;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (requisitionTableView != null) {
            // Configure table columns based on FXML text and desired PR properties
            if (prIdColumn != null) prIdColumn.setCellValueFactory(new PropertyValueFactory<>("prId"));
            if (managerIdColumn != null) managerIdColumn.setCellValueFactory(new PropertyValueFactory<>("salesManagerId"));
            if (itemIdColumn != null) itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
            if (quantityColumn != null) quantityColumn.setCellValueFactory(new PropertyValueFactory<>("requiredDate")); // Assumes PR model has getRequiredDate()
            if (statusColumn != null) statusColumn.setCellValueFactory(new PropertyValueFactory<>("ItemID")); // Or itemNameDisplay
            if (createdAtColumn != null) createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

            requisitionTableView.setItems(this.requisitionData);
        }
        loadPRData();
    }

    private void loadPRData() {
        try {
            this.prMapCache = loadPRsFromFileStatic(); // Call the static method that now uses FileUtils
            System.out.println("Loaded PRs (FinanceManagerController): " + this.prMapCache);
            this.requisitionData.clear();
            if (this.prMapCache != null) {
                this.requisitionData.addAll(this.prMapCache.values());
            }
            if (requisitionTableView != null) {
                 requisitionTableView.refresh();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load Purchase Requisitions: " + e.getMessage());
        }
    }

    // --- ADD PR Function (Instance Method) ---
    public boolean addPR(String prId, String salesManagerId, String itemId, int quantity, String status,
                         LocalDate createdAt, LocalDate requiredByDate) throws NoSuchMethodException { // Optional requiredByDate
        if (prId == null || prId.trim().isEmpty() || salesManagerId == null || salesManagerId.trim().isEmpty() ||
            itemId == null || itemId.trim().isEmpty() || quantity <= 0) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "PR ID, Manager ID, Item ID are required, Quantity must be positive.");
            return false;
        }
        if (status == null || status.trim().isEmpty()) status = "Pending";
        if (createdAt == null) createdAt = LocalDate.now();

        if (this.prMapCache == null) { // Should be initialized by loadPRData
            this.prMapCache = new HashMap<>();
        }
        if (this.prMapCache.containsKey(prId)) {
            showAlert(Alert.AlertType.ERROR, "Duplicate ID", "PR with ID '" + prId + "' already exists.");
            return false;
        }

        PurchaseRequisition newPR = new PurchaseRequisition(); // Use default constructor then setters
        newPR.setPrId(prId);
        newPR.setSalesManagerId(salesManagerId);
        newPR.setItemID(itemId);
        newPR.setQuantity(quantity);
        newPR.setStatus(status);
        newPR.setCreatedAt(createdAt);
        if (requiredByDate != null && newPR.getClass().getMethod("setRequiredDate", String.class) != null) { // Check if setRequiredDate exists
             try {
                newPR.getClass().getMethod("setRequiredDate", String.class).invoke(newPR, requiredByDate.toString());
            } catch (Exception e) {System.err.println("Could not set requiredDate via reflection: " + e.getMessage());}
        } else if (requiredByDate != null) {
            System.err.println("PurchaseRequisition model does not have a setRequiredDate(String) method.");
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
            this.prMapCache.remove(newPR.getPrId()); // Rollback
            this.requisitionData.remove(newPR);      // Rollback
            return false;
        }
    }

    // --- DELETE PR Function (Instance Method) ---
    public boolean deletePR(String prId) {
        if (prId == null || prId.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "PR ID to delete cannot be empty.");
            return false;
        }
        if (this.prMapCache == null || !this.prMapCache.containsKey(prId)) {
            showAlert(Alert.AlertType.WARNING, "Not Found", "PR with ID '" + prId + "' not found.");
            return false;
        }

        PurchaseRequisition prToRemove = this.prMapCache.get(prId);
        this.prMapCache.remove(prId);
        this.requisitionData.remove(prToRemove); // Updates TableView

        try {
            savePRsToFileStatic(this.prMapCache);
            System.out.println("PR '" + prId + "' deleted and saved.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save deletion: " + e.getMessage());
            if (prToRemove != null) { // Rollback
                this.prMapCache.put(prId, prToRemove);
                this.requisitionData.add(prToRemove);
            }
            return false;
        }
    }


    // ------------- STATIC File I/O Methods using FileUtils -------------
    public static Map<String, PurchaseRequisition> loadPRsFromFileStatic() throws IOException {
        Map<String, PurchaseRequisition> loadedPRMap = new HashMap<>();
        // Use FileUtils, create if not found with default content "{}" for a map
        File file = FileUtils.getDataFileFromProjectRoot(PR_FILENAME, true, "{}");

        if (file == null || !file.exists() || !file.canRead()) {
            System.err.println("Cannot read PR data file or file does not exist: " + (file != null ? file.getAbsolutePath() : PR_FILENAME + " path problem"));
            return loadedPRMap;
        }
        if (file.length() == 0) {
            System.out.println(PR_FILENAME + " is empty. Returning empty map.");
            return loadedPRMap;
        }

        try (InputStream inputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            loadedPRMap = gson.fromJson(reader, PR_MAP_TYPE);
            if (loadedPRMap == null) {
                loadedPRMap = new HashMap<>();
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from PR data file: " + file.getAbsolutePath() + ". Error: " + e.getMessage());
            throw new IOException("Error parsing PR JSON data from " + file.getAbsolutePath(), e);
        }
        return loadedPRMap;
    }

    public static void savePRsToFileStatic(Map<String, PurchaseRequisition> prMapToSave) throws IOException {
        // Use FileUtils, create if not found with default content "{}"
        File file = FileUtils.getDataFileFromProjectRoot(PR_FILENAME, true, "{}");
        if (file == null) {
            throw new IOException("Could not obtain file path for saving PRs using filename: " + PR_FILENAME);
        }

        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) { // Overwrites existing file
            gson.toJson(prMapToSave, writer);
            System.out.println("Purchase Requisitions saved to: " + file.getAbsolutePath());
        }
    }

    // REMOVED old getFileFromResource from this controller
}