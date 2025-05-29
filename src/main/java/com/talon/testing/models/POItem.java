package com.talon.testing.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class POItem {
    private String itemCode;
    private int quantity;

    // Constructor for new POItem
    public POItem(String itemCode, int quantity) {
        this.itemCode = itemCode;
        this.quantity = quantity;
    }

    // No-arg constructor for Gson/deserialization if needed (ensure fields are set later)
    public POItem() {
        this.itemCode = "";
        this.quantity = 0;
    }


    // --- itemCode ---
    public String getItemCode() {
        return this.itemCode;
    }
    public void setItemCode(String value) {
        this.itemCode = value;
    }
    public String itemCodeProperty() {
        return this.itemCode;
    }

    // --- quantity ---
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int value) {
        this.quantity = value;
    }
    public int quantityProperty() {
        return quantity;
    }

    @Override
    public String toString() {
        return "POItem{" +
               "itemCode=" + itemCode +
               ", quantity=" + quantity +
               '}';
    }
}