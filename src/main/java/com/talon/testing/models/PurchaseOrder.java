package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List; // For DTO
import java.util.Map;

public class PurchaseOrder {
    private String poId;              // Unique Purchase Order ID
    private String prId;              // ID of the original Purchase Requisition
    private String supplierId;
    private String supplierNameDisplay; // Name of the supplier (for display, can be denormalized)
    private String orderDate;         // Date PO was created (String "yyyy-MM-dd")
    private String requiredDate;      // Optional: Date items are required by (String "yyyy-MM-dd")
    private String purchaseManagerId; // Manager who authorized/created the PO
    private String financeManagerId;  // Manager who approved payment (workflow)
    private String approvalDate;      // Date finance approved (String "yyyy-MM-dd")
    private String status;            // e.g., "Generated", "Pending PM Approval", "Approved by PM", "Sent to Supplier", "Partially Received", "Received", "Paid", "Cancelled"
    private ObservableList<POItem> items; // List of items on this PO
    private String notes;             // Any additional notes for the PO

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String PO_FILE_PATH = "/data/PO.txt";
    private static final Type PO_MAP_TYPE = new TypeToken<Map<String, PurchaseOrder>>() {}.getType();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    // Default constructor for Gson and manual instantiation
    public PurchaseOrder() {
        this.items = FXCollections.observableArrayList();
        this.orderDate = LocalDate.now().format(DATE_FORMATTER); // Default to today
        this.status = "Generated - Pending Approval"; // Initial default status
    }

    /**
     * Constructor to create a Purchase Order from a Purchase Requisition.
     * Assumes PR is for a single item type for simplicity in this example.
     * If PR can have multiple items, this logic would need to loop through pr.getItems().
     */
    public PurchaseOrder(String newPoId, PurchaseRequisition pr, Supplier supplier, Item itemDetails) {
        this(); // Call default constructor to initialize items list and default date/status

        this.poId = newPoId;
        this.prId = pr.getPrId();

        if (supplier != null) {
            this.supplierId = supplier.getSupplierId();
            this.supplierNameDisplay = supplier.getSupplierName();
        } else if (itemDetails != null && itemDetails.getSupplierId() != null) {
            // Fallback if direct supplier object isn't passed but item has supplierId
            this.supplierId = itemDetails.getSupplierId();
            // supplierNameDisplay would need to be looked up separately if only itemDetails is available
            this.supplierNameDisplay = "Lookup Required for " + this.supplierId;
        }


        this.purchaseManagerId = pr.getSalesManagerId(); // Assuming PR's SM becomes PO's PM
        // this.requiredDate = pr.getRequiredDate(); // If PR has a requiredDate

        // Create a POItem from the PR's item details and add it
        if (itemDetails != null) {
            this.items.add(new POItem(itemDetails.getItemCode(), itemDetails.getItemName(), pr.getQuantity(), itemDetails.getUnitPrice()));
        } else {
            // If item master details are somehow missing, create POItem with what's on PR
            this.items.add(new POItem(pr.getItemID(), pr.getItemNameDisplay(), pr.getQuantity(), "0.00")); // Default price
            System.err.println("Warning: Full item details not found for ItemID: " + pr.getItemID() + " while creating PO: " + poId + ". Using details from PR.");
        }
    }


    // --- Getters ---
    public String getPoId() { return poId; }
    public String getPrId() { return prId; }
    public String getSupplierId() { return supplierId; }
    public String getSupplierNameDisplay() { return supplierNameDisplay; }
    public String getOrderDate() { return orderDate; }
    public String getRequiredDate() { return requiredDate; }
    public String getPurchaseManagerId() { return purchaseManagerId; }
    public String getFinanceManagerId() { return financeManagerId; }
    public String getApprovalDate() { return approvalDate; }
    public String getStatus() { return status; }
    public ObservableList<POItem> getItems() { return items; }
    public String getNotes() { return notes; }

    // --- Setters ---
    public void setPoId(String poId) { this.poId = poId; }
    public void setPrId(String prId) { this.prId = prId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public void setSupplierNameDisplay(String supplierNameDisplay) { this.supplierNameDisplay = supplierNameDisplay; }
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = (orderDate != null) ? orderDate.format(DATE_FORMATTER) : null;
    }
    public void setOrderDate(String orderDateString) { this.orderDate = orderDateString; }
    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDate = (requiredDate != null) ? requiredDate.format(DATE_FORMATTER) : null;
    }
    public void setRequiredDate(String requiredDateString) { this.requiredDate = requiredDateString; }
    public void setPurchaseManagerId(String purchaseManagerId) { this.purchaseManagerId = purchaseManagerId; }
    public void setFinanceManagerId(String financeManagerId) { this.financeManagerId = financeManagerId; }
    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = (approvalDate != null) ? approvalDate.format(DATE_FORMATTER) : null;
    }
    public void setApprovalDate(String approvalDateString) { this.approvalDate = approvalDateString; }
    public void setStatus(String status) { this.status = status; }
    public void setItems(ObservableList<POItem> items) { this.items = items; }
    public void setNotes(String notes) { this.notes = notes; }

    // Helper to convert List<POItem> from DTO to ObservableList<POItem>
    public void setItemsListFromDto(List<POItem> itemsList) {
        if (itemsList != null) {
            this.items = FXCollections.observableArrayList(itemsList);
        } else {
            this.items = FXCollections.observableArrayList();
        }
    }


    // --- Data Loading and Saving (Static Methods) ---

    // DTO for robust deserialization
    private static class PurchaseOrderDTO {
        String poId, prId, supplierId, supplierNameDisplay, orderDate, requiredDate,
               purchaseManagerId, financeManagerId, approvalDate, status, notes;
        List<POItem> items; // Gson deserializes to ArrayList
    }

    public static Map<String, PurchaseOrder> loadPOs(Map<String, Supplier> suppliersMap) throws IOException {
        Map<String, PurchaseOrder> finalPoMap = new HashMap<>();
        try (InputStream inputStream = PurchaseOrder.class.getResourceAsStream(PO_FILE_PATH)) {
            if (inputStream == null) {
                System.err.println("PO data file not found: " + PO_FILE_PATH + ". Attempting to create.");
                File file = getFileFromResource(PO_FILE_PATH, true);
                if (file != null && file.exists() && file.length() == 0) {
                    try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) { writer.write("{}"); }
                } else if (file == null || !file.exists()) {
                    System.err.println("Could not create or access PO file: " + PO_FILE_PATH);
                }
                return finalPoMap;
            }

            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                Type dtoMapType = new TypeToken<Map<String, PurchaseOrderDTO>>() {}.getType();
                Map<String, PurchaseOrderDTO> dtoMap = gson.fromJson(reader, dtoMapType);

                if (dtoMap != null) {
                    for (Map.Entry<String, PurchaseOrderDTO> entry : dtoMap.entrySet()) {
                        PurchaseOrderDTO dto = entry.getValue();
                        PurchaseOrder po = new PurchaseOrder(); // Use default constructor

                        po.setPoId(dto.poId);
                        po.setPrId(dto.prId);
                        po.setSupplierId(dto.supplierId);
                        po.setOrderDate(dto.orderDate);
                        po.setRequiredDate(dto.requiredDate);
                        po.setPurchaseManagerId(dto.purchaseManagerId);
                        po.setFinanceManagerId(dto.financeManagerId);
                        po.setApprovalDate(dto.approvalDate);
                        po.setStatus(dto.status);
                        po.setNotes(dto.notes);
                        po.setItemsListFromDto(dto.items); // Convert List to ObservableList

                        // Populate/Verify transient supplierNameDisplay
                        if (dto.supplierNameDisplay != null && !dto.supplierNameDisplay.isEmpty()) {
                             po.setSupplierNameDisplay(dto.supplierNameDisplay);
                        } else if (po.getSupplierId() != null && suppliersMap != null && suppliersMap.containsKey(po.getSupplierId())) {
                            po.setSupplierNameDisplay(suppliersMap.get(po.getSupplierId()).getSupplierName());
                        } else if (po.getSupplierId() != null) {
                            po.setSupplierNameDisplay("Unknown (" + po.getSupplierId() + ")");
                        } else {
                            po.setSupplierNameDisplay("N/A");
                        }
                        finalPoMap.put(entry.getKey(), po);
                    }
                }
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from PO data file: " + PO_FILE_PATH);
            throw new IOException("Error parsing PO JSON data.", e);
        }
        return finalPoMap;
    }

    public static void savePOs(Map<String, PurchaseOrder> poMap) throws IOException {
        File file = getFileFromResource(PO_FILE_PATH, true);
        if (file == null) {
            throw new IOException("Could not get or create file for saving POs: " + PO_FILE_PATH);
        }
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(poMap, writer);
            System.out.println("Purchase Orders saved to: " + file.getAbsolutePath());
        }
    }

    // getFileFromResource helper (essential for robust file I/O)
    private static File getFileFromResource(String resourcePath, boolean createIfNotFound) throws IOException {
        URL resourceUrl = PurchaseOrder.class.getResource(resourcePath); // Use PurchaseOrder.class context
        File file;
        if (resourceUrl != null) {
            try { file = new File(resourceUrl.toURI()); }
            catch (URISyntaxException e) { throw new IOException("Invalid resource URI: " + resourcePath, e); }
        } else {
            System.out.println("Resource " + resourcePath + " not found. Trying to create in build/user dir.");
            URL rootPath = PurchaseOrder.class.getProtectionDomain().getCodeSource().getLocation();
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
                if (file.length() == 0) { try (Writer w = new FileWriter(file)) { w.write("{}"); } }
            }
        }
        return file;
    }
    
    /**
     * Determines if the PO is in a state where it can be considered approved
     * for further actions like payment processing.
     * This logic might be specific to your workflow.
     * @return true if the PO is considered approved for payment.
     */
    public boolean isConsideredApprovedForPayment() {
        if (status == null) return false;
        // Add all statuses that mean it's sufficiently approved to proceed to payment
        return "Approved by Finance".equalsIgnoreCase(status) ||
               "Approved by PM".equalsIgnoreCase(status) || // If PM approval is enough for some views
               "Paid".equalsIgnoreCase(status) || // Already paid is also "approved" in a sense
               "Shipped".equalsIgnoreCase(status) || // If payment happens after shipping
               "Received".equalsIgnoreCase(status); // If payment happens after receipt
        // You might also check if approvalDate is not null and not empty,
        // depending on your exact business rules for "approved".
        // e.g., return (... existing status checks ...) && (approvalDate != null && !approvalDate.isEmpty());
    }

    @Override
    public String toString() {
        return "PO ID: " + poId + ", Supplier: " + (supplierNameDisplay != null ? supplierNameDisplay : supplierId) + ", Status: " + status + ", Items: " + items.size();
    }
}