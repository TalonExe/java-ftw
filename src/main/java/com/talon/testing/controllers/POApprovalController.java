package com.talon.testing.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import static com.talon.testing.controllers.FinanceManagerController.loadPR;
import com.talon.testing.models.PurchaseOrder; // Assuming you have this model
import com.talon.testing.models.PurchaseRequisition;
import com.talon.testing.utils.Router;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
// ... other imports

public class POApprovalController {
    Router router = Router.getInstance();

    private static final String PO_FILE_PATH = "/data/PO.txt";
    private static final Type PO_MAP_TYPE = new TypeToken<Map<String, PurchaseOrder>>() {}.getType();
    
    @FXML
    private TableView<PurchaseOrder> poTable; // For PurchaseOrder

    // Use unique fx:id names for columns in PO.fxml or unique field names here
    @FXML
    private TableColumn<PurchaseOrder, String> poIdColumnPO; // Example: Renamed for clarity

    @FXML
    private TableColumn<PurchaseOrder, String> prIdColumnPO;  // Example: Renamed

    @FXML
    private TableColumn<PurchaseOrder, String> statusColumnPO; // Example: Renamed

    @FXML
    private TableColumn<PurchaseOrder, String> approvedColumnPO; // Example: Renamed, text is "Approved"

    @FXML
    private Button approveButton;

    @FXML
    private Button rejectButton;

    @FXML
    private TextField itemCodeField;

    @FXML
    private TextField quantityField;

    @FXML
    private Button updateQuantityButton;

    private ObservableList<PurchaseOrder> poData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup poTable columns
        poIdColumnPO.setCellValueFactory(new PropertyValueFactory<>("poId")); // Assuming PurchaseOrder has getPoId()
        prIdColumnPO.setCellValueFactory(new PropertyValueFactory<>("prId"));   // Assuming PurchaseOrder has getPrId()
        statusColumnPO.setCellValueFactory(new PropertyValueFactory<>("status"));
        approvedColumnPO.setCellValueFactory(new PropertyValueFactory<>("approvalDate")); // Or similar

        loadPOData();

        // Add event handlers for buttons
        approveButton.setOnAction(event -> handleApprovePO());
        rejectButton.setOnAction(event -> handleRejectPO());
        updateQuantityButton.setOnAction(event -> handleUpdateQuantity());
    }
    
    private void loadPOData() {
        try {
            Map<String, PurchaseOrder> poMap = loadPO();
            System.out.println(poMap);
            poData.clear();
            poData.addAll(poMap.values());
            poTable.setItems(poData);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error (show alert, etc.)
        }
    }
    

    public static Map<String, PurchaseOrder> loadPO() throws IOException {
        Map<String, PurchaseOrder> POMap = new HashMap<>();
        Gson gson = new Gson();
        
        try (var inputStream = PurchaseOrder.class.getResourceAsStream(PO_FILE_PATH)) {
            if (inputStream == null) {
                return POMap;
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                POMap = gson.fromJson(reader, PO_MAP_TYPE);
                if (POMap == null) {
                    POMap = new HashMap<>();
                }
            }
        }
        return POMap;
    }
    
    private static String determineFilePath() {
        String basePath = System.getProperty("user.dir");
        return basePath + "/src/main/resources" + PO_FILE_PATH;
    }

    private void handleApprovePO() {
        PurchaseOrder selectedPO = poTable.getSelectionModel().getSelectedItem();
        if (selectedPO != null) {
            System.out.println("Approving PO: " + selectedPO.getPoId()); // Assuming getPoId
            // ... logic to approve PO and update data source
        }
    }

    private void handleRejectPO() {
        PurchaseOrder selectedPO = poTable.getSelectionModel().getSelectedItem();
        if (selectedPO != null) {
            System.out.println("Rejecting PO: " + selectedPO.getPoId());
            // ... logic to reject PO
        }
    }

    private void handleUpdateQuantity() {
        String itemCode = itemCodeField.getText();
        String quantityStr = quantityField.getText();
        System.out.println("Update quantity for item: " + itemCode + " to " + quantityStr);
        // ... logic to update quantity
    }

    @FXML
    public void test() { // This is for the side navigation buttons
        System.out.println("Button clicked in POApprovalController");
        // Implement navigation or specific actions
    }
    
    @FXML
    public void switchToFinanceReport() { // This is for the side navigation buttons
        System.out.println("Button clicked in POApprovalController");
        // Implement navigation or specific actions
    }
    
    @FXML
    public void switchToPOApproval() { // This is for the side navigation buttons
        System.out.println("Button clicked in POApprovalController");
        // Implement navigation or specific actions
    }
    
    @FXML
    public void switchToPR() { // This is for the side navigation buttons
        router.switchScene("PR");
    }
    
    @FXML
    public void switchToPayment() { // This is for the side navigation buttons
        System.out.println("Button clicked in POApprovalController");
        // Implement navigation or specific actions
    }
}