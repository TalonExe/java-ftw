package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList; // For the list of items

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseOrder {
    private String poId;
    private String prId;
    private String supplierId; // Was 'supplier' in your original, now explicitly ID
    private transient String supplierName; // Not persisted in this PO's JSON, populated from Supplier map
    private String orderDate;          // Stored as String "yyyy-MM-dd"
    private String purchaseManagerId;
    private String financeManagerId;
    private String approvalDate;       // Stored as String "yyyy-MM-dd"
    private String status;
    private ObservableList<POItem> items; // Collection of items

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String PO_FILE_PATH = "/data/purchase_orders.txt";
    private static final Type PO_MAP_TYPE = new TypeToken<Map<String, PurchaseOrder>>() {}.getType();

    // Gson instance for (de)serialization
    // No LocalDateAdapter needed here if dates are always strings in this class's JSON representation
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Constructor for creating a new PO programmatically
    public PurchaseOrder(String poId, String prId, String supplierId, LocalDate orderDate,
                         String purchaseManagerId, String status) {
        this.poId = poId;
        this.prId = prId;
        this.supplierId = supplierId;
        this.orderDate = (orderDate != null) ? orderDate.format(DATE_FORMATTER) : null;
        this.purchaseManagerId = purchaseManagerId;
        this.financeManagerId = null; // Can be set later
        this.approvalDate = null;     // Can be set later
        this.status = status;
        this.items = FXCollections.observableArrayList(); // Initialize empty items list
    }

    // Default constructor - Gson can use this, or you can use it for manual creation
    public PurchaseOrder() {
        this.orderDate = LocalDate.now().format(DATE_FORMATTER); // Default order date
        this.status = "Pending"; // Default status
        this.items = FXCollections.observableArrayList(); // Initialize empty items list
    }

    // --- Getters ---
    public String getPoId() { return poId; }
    public String getPrId() { return prId; }
    public String getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; } // Used for display
    public String getOrderDate() { return orderDate; }
    public String getPurchaseManagerId() { return purchaseManagerId; }
    public String getFinanceManagerId() { return financeManagerId; }
    public String getApprovalDate() { return approvalDate != null ? approvalDate : ""; }
    public String getStatus() { return status; }
    public ObservableList<POItem> getItems() { return items; }


    // --- Setters ---
    public void setPoId(String poId) { this.poId = poId; }
    public void setPrId(String prId) { this.prId = prId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; } // Used for display
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = (orderDate != null) ? orderDate.format(DATE_FORMATTER) : null;
    }
    public void setOrderDate(String orderDateString) { // To set from string directly
        this.orderDate = orderDateString;
    }
    public void setPurchaseManagerId(String purchaseManagerId) { this.purchaseManagerId = purchaseManagerId; }
    public void setFinanceManagerId(String financeManagerId) { this.financeManagerId = financeManagerId; }
    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = (approvalDate != null) ? approvalDate.format(DATE_FORMATTER) : null;
    }
     public void setApprovalDate(String approvalDateString) { // To set from string directly
        this.approvalDate = approvalDateString;
    }
    public void setStatus(String status) { this.status = status; }
    public void setItems(ObservableList<POItem> items) { this.items = items; }
    // If Gson deserializes 'items' as a basic List, you might need this:
    public void setItemsList(List<POItem> itemsList) {
        if (itemsList != null) {
            this.items = FXCollections.observableArrayList(itemsList);
        } else {
            this.items = FXCollections.observableArrayList();
        }
    }


    // --- Convenience Methods ---
    public boolean isApproved() {
        String currentStatus = this.status;
        if (currentStatus == null) return false;
        return "Approved".equalsIgnoreCase(currentStatus) ||
               "Paid".equalsIgnoreCase(currentStatus) ||
               "Processed".equalsIgnoreCase(currentStatus) ||
               (this.approvalDate != null && !this.approvalDate.isEmpty() &&
                !"Pending".equalsIgnoreCase(currentStatus) &&
                !"Rejected".equalsIgnoreCase(currentStatus));
    }

    // --- Data Loading and Saving ---

    // Temporary DTO class for robust deserialization if direct mapping is problematic
    private static class PurchaseOrderDTO {
        String poId;
        String prId;
        String supplierId; // Expecting this from JSON
        String orderDate;
        String purchaseManagerId;
        String financeManagerId;
        String approvalDate;
        String status;
        List<POItem> items; // Gson will deserialize to ArrayList here
    }

    public static Map<String, PurchaseOrder> loadAllPOs(Map<String, Supplier> suppliersMap) throws IOException {
        Map<String, PurchaseOrder> finalPoMap = new HashMap<>();

        try (InputStream inputStream = PurchaseOrder.class.getResourceAsStream(PO_FILE_PATH)) {
            if (inputStream == null) {
                System.err.println("Purchase Order data file not found: " + PO_FILE_PATH);
                // Create an empty file if it doesn't exist to prevent errors on first run
                File file = getFileFromResource(PO_FILE_PATH, true);
                if (file != null && file.exists() && file.length() == 0) {
                     System.out.println("Empty purchase_orders.txt created at: " + file.getAbsolutePath());
                     // Write empty JSON object to make it valid
                     try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                         writer.write("{}");
                     }
                     return finalPoMap; // Return empty map
                } else if (file == null){
                    System.err.println("Could not create or access PO file path.");
                    return finalPoMap;
                }
                // If we reach here, means getFileFromResource didn't create it or something else is wrong.
                 return finalPoMap;
            }

            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                // Use the DTO for deserialization
                Type dtoMapType = new TypeToken<Map<String, PurchaseOrderDTO>>() {}.getType();
                Map<String, PurchaseOrderDTO> dtoMap = gson.fromJson(reader, dtoMapType);

                if (dtoMap != null) {
                    for (Map.Entry<String, PurchaseOrderDTO> entry : dtoMap.entrySet()) {
                        PurchaseOrderDTO dto = entry.getValue();
                        PurchaseOrder po = new PurchaseOrder(); // Use default constructor

                        po.setPoId(dto.poId);
                        po.setPrId(dto.prId);
                        po.setSupplierId(dto.supplierId); // Set supplierId from DTO
                        po.setOrderDate(dto.orderDate);
                        po.setPurchaseManagerId(dto.purchaseManagerId);
                        po.setFinanceManagerId(dto.financeManagerId);
                        po.setApprovalDate(dto.approvalDate);
                        po.setStatus(dto.status);
                        po.setItemsList(dto.items); // This converts List to ObservableList

                        // Populate transient supplierName
                        if (po.getSupplierId() != null && suppliersMap != null && suppliersMap.containsKey(po.getSupplierId())) {
                            po.setSupplierName(suppliersMap.get(po.getSupplierId()).getSupplierName());
                        } else if (po.getSupplierId() != null) {
                            System.err.println("Warning: Supplier not found for ID: " + po.getSupplierId() + " for PO: " + po.getPoId());
                            po.setSupplierName("Unknown Supplier");
                        }

                        finalPoMap.put(entry.getKey(), po);
                    }
                }
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from Purchase Order data file: " + PO_FILE_PATH + ". File might be empty or malformed.");
            e.printStackTrace();
            // If file is syntactically incorrect, return empty map or handle as error
        } catch (IOException e) {
            System.err.println("IOException during loading Purchase Orders: " + e.getMessage());
            e.printStackTrace();
        }
        return finalPoMap;
    }

    public static void saveAllPOs(Map<String, PurchaseOrder> poMap) throws IOException {
        File file = getFileFromResource(PO_FILE_PATH, true); // true to attempt creation
         if (file == null) {
            throw new IOException("Could not get or create file for saving POs at path: " + PO_FILE_PATH);
        }

        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(poMap, writer);
            System.out.println("Purchase Orders saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing Purchase Order data to file: " + file.getAbsolutePath());
            throw e;
        }
    }

    private static File getFileFromResource(String resourcePath, boolean createIfNotFound) throws IOException {
        URL resourceUrl = PurchaseOrder.class.getResource(resourcePath);
        File file;
        if (resourceUrl != null) {
            try {
                file = new File(resourceUrl.toURI());
            } catch (URISyntaxException e) {
                throw new IOException("Invalid resource URI: " + resourcePath, e);
            }
        } else {
            // Resource not found in classpath, try to create it in a path relative to execution
            // This part is tricky and might need adjustment based on deployment
            System.out.println("Resource not found in classpath: " + resourcePath + ". Attempting to use/create file in execution directory structure.");
            // Attempt to get a base path (e.g., where the JAR is or target/classes)
            URL codeSourceUrl = PurchaseOrder.class.getProtectionDomain().getCodeSource().getLocation();
            File baseDir;
            try {
                baseDir = new File(codeSourceUrl.toURI()).getParentFile(); // Parent of target/classes or JAR location
                 if (resourcePath.startsWith("/data/")) { // Adjust for our /data/ structure
                    file = new File(baseDir, resourcePath.substring(1)); // remove leading /
                } else {
                    file = new File(baseDir, resourcePath);
                }

            } catch (Exception e) { // URISyntaxException or NullPointerException
                 System.err.println("Could not determine base directory for saving. Falling back to current working directory for " + resourcePath);
                 file = new File(System.getProperty("user.dir") + resourcePath);
            }


            if (createIfNotFound && !file.exists()) {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    if (!parentDir.mkdirs()) {
                        System.err.println("Could not create directory: " + parentDir.getAbsolutePath());
                        return null; // Or throw
                    }
                }
                 if (!file.createNewFile()) {
                     System.err.println("Could not create new file: " + file.getAbsolutePath());
                     return null; // Or throw
                 }
                 System.out.println("Created new file at: "+ file.getAbsolutePath());
            }
        }
        return file;
    }


    @Override
    public String toString() {
        int itemCount = (items != null) ? items.size() : 0;
        return "PurchaseOrder{" +
               "poId='" + poId + '\'' +
               ", prId='" + prId + '\'' +
               ", supplierId='" + supplierId + '\'' +
               (supplierName != null ? ", supplierName='" + supplierName + '\'' : "") +
               ", orderDate='" + orderDate + '\'' +
               ", status='" + status + '\'' +
               ", itemsCount=" + itemCount +
               '}';
    }
}