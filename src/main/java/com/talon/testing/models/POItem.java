package com.talon.testing.models;

public class POItem {
    private String itemCode;
    private int quantity;
    private String unitPrice; // Price of the item at the time of PO creation (as String)
    private String itemNameDisplay; // Optional: For display within PO details if needed

    public POItem(String itemCode, String itemNameDisplay, int quantity, String unitPrice) {
        this.itemCode = itemCode;
        this.itemNameDisplay = itemNameDisplay;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // No-arg constructor for Gson
    public POItem() {
    }

    // Getters
    public String getItemCode() { return itemCode; }
    public String getItemNameDisplay() { return itemNameDisplay; }
    public int getQuantity() { return quantity; }
    public String getUnitPrice() { return unitPrice; }

    // Setters
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public void setItemNameDisplay(String itemNameDisplay) { this.itemNameDisplay = itemNameDisplay; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(String unitPrice) { this.unitPrice = unitPrice; }

    @Override
    public String toString() {
        return "POItem{" +
               "itemCode='" + itemCode + '\'' +
               ", itemName='" + itemNameDisplay + '\'' +
               ", quantity=" + quantity +
               ", unitPrice='" + unitPrice + '\'' +
               '}';
    }
}