package com.talon.testing.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.talon.testing.models.PurchaseOrder;
import com.talon.testing.models.PurchaseRequisition;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class FinanceManagerController {
    private static final String PR_FILE_PATH = "/data/PR.txt";
    private static final Type PR_MAP_TYPE = new TypeToken<Map<String, PurchaseRequisition>>() {}.getType();
    
    private static final String PO_FILE_PATH = "/data/PO.txt";
    private static final Type PO_MAP_TYPE = new TypeToken<Map<String, PurchaseOrder>>() {}.getType();
    
    @FXML
    private TableView<PurchaseRequisition> requisitionTableView;
    
    @FXML
    private TableColumn<PurchaseRequisition, String> prIdColumn;
    
    @FXML
    private TableColumn<PurchaseRequisition, String> managerIdColumn;
    
    @FXML
    private TableColumn<PurchaseRequisition, String> itemIdColumn;
    
    @FXML
    private TableColumn<PurchaseRequisition, Integer> quantityColumn;
    
    @FXML
    private TableColumn<PurchaseRequisition, String> statusColumn;
    
    @FXML
    private TableColumn<PurchaseRequisition, String> createdAtColumn;
    
    private ObservableList<PurchaseRequisition> requisitionData = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Set up the table columns
        prIdColumn.setCellValueFactory(new PropertyValueFactory<>("prId"));
        managerIdColumn.setCellValueFactory(new PropertyValueFactory<>("salesManagerId"));
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemID"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAtString"));
        
        // Load data
        loadPRData();
    }
    
    private void loadPRData() {
        try {
            Map<String, PurchaseRequisition> prMap = loadPR();
            System.out.println(prMap);
            requisitionData.clear();
            requisitionData.addAll(prMap.values());
            requisitionTableView.setItems(requisitionData);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error (show alert, etc.)
        }
    }
    
    @FXML
    public void test() {
        System.out.println("com.talon.testing.controllers.FinanceManagerController.test()");
    }
    
    public static Map<String, PurchaseRequisition> loadPR() throws IOException {
        Map<String, PurchaseRequisition> PRMap = new HashMap<>();
        Gson gson = new Gson();
        
        try (var inputStream = PurchaseRequisition.class.getResourceAsStream(PR_FILE_PATH)) {
            if (inputStream == null) {
                return PRMap;
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                PRMap = gson.fromJson(reader, PR_MAP_TYPE);
                if (PRMap == null) {
                    PRMap = new HashMap<>();
                }
            }
        }
        return PRMap;
    }
    
    private static String determineFilePath() {
        String basePath = System.getProperty("user.dir");
        return basePath + "/src/main/resources" + PR_FILE_PATH;
    }
    
    private static void saveItems(Map<String, PurchaseRequisition> itemMap) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(itemMap, PR_MAP_TYPE);
        
        String filePath = determineFilePath();
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(json);
        }
    }
}