package com.talon.testing.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PurchaseRequisition {
    private String prId;
    private String salesManagerId;
    private String ItemID; // The code of the item
    private String itemNameDisplay; // NEW: For displaying item name in table
    private String supplierNameDisplay; // NEW: For displaying supplier name in table (derived from item's supplier)
    private int quantity;
    private String status;
    private String createdAt;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Constructor used when creating a NEW PR from the form
    public PurchaseRequisition(String prId, String salesManagerId, String itemID,
                               String itemNameDisplay, String supplierNameDisplay, // Pass display names
                               int quantity, String status, LocalDate createdAtDate) {
        this.prId = prId;
        this.salesManagerId = salesManagerId;
        this.ItemID = itemID;
        this.itemNameDisplay = itemNameDisplay;
        this.supplierNameDisplay = supplierNameDisplay;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = (createdAtDate != null) ? createdAtDate.format(DATE_FORMATTER) : null;
    }

    // Constructor potentially used by Gson for deserialization (if JSON includes these display fields)
    // OR if you populate them after loading.
    // If JSON doesn't have them, Gson will leave them null, and you'd populate them in the controller.
    public PurchaseRequisition(String prId, String salesManagerId, String ItemID,
                             int quantity, String status, String createdAtString) {
        this.prId = prId;
        this.salesManagerId = salesManagerId;
        this.ItemID = ItemID;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = (createdAtString != null && !createdAtString.isEmpty()) ?
                         LocalDate.parse(createdAtString, DATE_FORMATTER).toString() : null;
        // itemNameDisplay and supplierNameDisplay would be null here if not in JSON
    }


    // Default constructor
    public PurchaseRequisition() {
        this.createdAt = LocalDate.now().format(DATE_FORMATTER);
        this.status = "Pending";
    }

    // Getters
    public String getPrId() { return prId; }
    public String getSalesManagerId() { return salesManagerId; }
    public String getItemID() { return ItemID; }
    public String getItemNameDisplay() { return itemNameDisplay; }         // NEW Getter
    public String getSupplierNameDisplay() { return supplierNameDisplay; } // NEW Getter
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setPrId(String prId) { this.prId = prId; }
    public void setSalesManagerId(String salesManagerId) { this.salesManagerId = salesManagerId; }
    public void setItemID(String itemID) { ItemID = itemID; }
    public void setItemNameDisplay(String itemNameDisplay) { this.itemNameDisplay = itemNameDisplay; } // NEW Setter
    public void setSupplierNameDisplay(String supplierNameDisplay) { this.supplierNameDisplay = supplierNameDisplay; } // NEW Setter
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDate createdAtDate) {
        this.createdAt = (createdAtDate != null) ? createdAtDate.format(DATE_FORMATTER) : null;
    }
    // Setter for string date for Gson or manual setting
    public void setCreatedAt(String createdAtString){
        this.createdAt = createdAtString;
    }


    @Override
    public String toString() { // Updated for new fields
        return prId + "," +
               salesManagerId + "," +
               ItemID + "," +
               (itemNameDisplay != null ? itemNameDisplay + "," : "N/A,") +
               (supplierNameDisplay != null ? supplierNameDisplay + "," : "N/A,") +
               quantity + "," +
               status + "," +
               createdAt;
    }
}