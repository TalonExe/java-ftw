package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.talon.testing.utils.FileUtils; // IMPORT FileUtils
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// Remove unused URL, URISyntaxException

public class PurchaseOrder {
    // ... (fields, constructors, getters, setters, toString, isConsideredApprovedForPayment remain the same) ...
    private String poId;
    private String prId;
    private String supplierId;
    private String supplierNameDisplay;
    private String orderDate;
    private String requiredDate;
    private String purchaseManagerId;
    private String financeManagerId;
    private String approvalDate;
    private String status;
    private ObservableList<POItem> items;
    private String notes;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String PO_FILENAME = "PO.txt"; // Renamed from PO_FILE_PATH
    private static final Type PO_MAP_TYPE = new TypeToken<Map<String, PurchaseOrder>>() {}.getType();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public PurchaseOrder() {
        this.items = FXCollections.observableArrayList();
        this.orderDate = LocalDate.now().format(DATE_FORMATTER);
        this.status = "Generated - Pending Approval";
    }

    public PurchaseOrder(String newPoId, PurchaseRequisition pr, Supplier supplier, Item itemDetails) {
        this();
        this.poId = newPoId;
        this.prId = pr.getPrId();
        if (supplier != null) {
            this.supplierId = supplier.getSupplierId();
            this.supplierNameDisplay = supplier.getSupplierName();
        } else if (itemDetails != null && itemDetails.getSupplierId() != null) {
            this.supplierId = itemDetails.getSupplierId();
            this.supplierNameDisplay = "Lookup Required for " + this.supplierId;
        }
        this.purchaseManagerId = pr.getSalesManagerId();
        if (itemDetails != null) {
            this.items.add(new POItem(itemDetails.getItemCode(), itemDetails.getItemName(), pr.getQuantity(), itemDetails.getUnitPrice()));
        } else {
            this.items.add(new POItem(pr.getItemID(), pr.getItemNameDisplay(), pr.getQuantity(), "0.00"));
            System.err.println("Warning: Full item details not found for ItemID: " + pr.getItemID() + " while creating PO: " + poId);
        }
    }
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
    public void setPoId(String poId) { this.poId = poId; }
    public void setPrId(String prId) { this.prId = prId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public void setSupplierNameDisplay(String supplierNameDisplay) { this.supplierNameDisplay = supplierNameDisplay; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = (orderDate != null) ? orderDate.format(DATE_FORMATTER) : null; }
    public void setOrderDate(String orderDateString) { this.orderDate = orderDateString; }
    public void setRequiredDate(LocalDate requiredDate) { this.requiredDate = (requiredDate != null) ? requiredDate.format(DATE_FORMATTER) : null; }
    public void setRequiredDate(String requiredDateString) { this.requiredDate = requiredDateString; }
    public void setPurchaseManagerId(String purchaseManagerId) { this.purchaseManagerId = purchaseManagerId; }
    public void setFinanceManagerId(String financeManagerId) { this.financeManagerId = financeManagerId; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = (approvalDate != null) ? approvalDate.format(DATE_FORMATTER) : null; }
    public void setApprovalDate(String approvalDateString) { this.approvalDate = approvalDateString; }
    public void setStatus(String status) { this.status = status; }
    public void setItems(ObservableList<POItem> items) { this.items = items; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setItemsListFromDto(List<POItem> itemsList) {
        if (this.items == null) this.items = FXCollections.observableArrayList();
        this.items.clear();
        if (itemsList != null) this.items.addAll(itemsList);
    }
     public boolean isConsideredApprovedForPayment() {
        if (status == null) return false;
        return "Approved by Finance".equalsIgnoreCase(status) ||
               "Approved by PM".equalsIgnoreCase(status) || 
               "Paid".equalsIgnoreCase(status) || 
               "Shipped".equalsIgnoreCase(status) || 
               "Received".equalsIgnoreCase(status);
    }
    @Override
    public String toString() {
        return "PO ID: " + poId + ", Supplier: " + (supplierNameDisplay != null ? supplierNameDisplay : supplierId) + ", Status: " + status + ", Items: " + (items != null ? items.size() : 0);
    }


    private static class PurchaseOrderDTO { // Keep DTO as it is
        String poId, prId, supplierId, supplierNameDisplay, orderDate, requiredDate,
               purchaseManagerId, financeManagerId, approvalDate, status, notes;
        List<POItem> items;
    }

    public static Map<String, PurchaseOrder> loadPOs(Map<String, Supplier> suppliersMap) throws IOException {
        Map<String, PurchaseOrder> finalPoMap = new HashMap<>();
        // Use FileUtils, create if not found with default content "{}"
        File file = FileUtils.getDataFileFromProjectRoot(PO_FILENAME, true, "{}");

        if (file == null || !file.exists() || !file.canRead()) {
             System.err.println("Cannot read PO data file or file does not exist: " + (file != null ? file.getAbsolutePath() : PO_FILENAME + " path problem"));
            return finalPoMap;
        }
        if (file.length() == 0) {
            System.out.println(PO_FILENAME + " is empty. Returning empty map.");
            return finalPoMap;
        }

        try (InputStream inputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Type dtoMapType = new TypeToken<Map<String, PurchaseOrderDTO>>() {}.getType();
            Map<String, PurchaseOrderDTO> dtoMap = gson.fromJson(reader, dtoMapType);

            if (dtoMap != null) {
                for (Map.Entry<String, PurchaseOrderDTO> entry : dtoMap.entrySet()) {
                    PurchaseOrderDTO dto = entry.getValue();
                    PurchaseOrder po = new PurchaseOrder();
                    // ... (set fields from dto to po as before) ...
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
                    po.setItemsListFromDto(dto.items);

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
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from PO data file: " + file.getAbsolutePath() + ". Error: " + e.getMessage());
            throw new IOException("Error parsing PO JSON data from " + file.getAbsolutePath(), e);
        }
        return finalPoMap;
    }

    public static void savePOs(Map<String, PurchaseOrder> poMap) throws IOException {
        File file = FileUtils.getDataFileFromProjectRoot(PO_FILENAME, true, "{}");
        if (file == null) {
            throw new IOException("Could not obtain file path for saving POs using filename: " + PO_FILENAME);
        }
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(poMap, writer);
            System.out.println("Purchase Orders saved to: " + file.getAbsolutePath());
        }
    }
    
    public boolean isPendingFinanceApproval() {
        // This is an example, adjust to your workflow
        return "Approved by PM".equalsIgnoreCase(status) || "Pending Finance Approval".equalsIgnoreCase(status);
    }

    // REMOVE the old private static File getFileFromResource method from this class
}