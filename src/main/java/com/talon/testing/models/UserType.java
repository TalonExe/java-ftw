package com.talon.testing.models;

public enum UserType {
    Purchase_Manager,
    Inventory_Manager,
    Administrator,
    Sales_Manager,
    Finance_Manager;

    // Optional: For nicer display in ComboBox if default (enum constant name) is not desired
    @Override
    public String toString() {
        return this.name().replace("_", " "); // e.g., "Purchase Manager"
    }

    // For robustly parsing from JSON string, handling potential case/underscore differences
    public static UserType fromString(String text) {
        if (text == null) return null;
        for (UserType ut : UserType.values()) {
            // Check against name() and a version with spaces if your JSON might have that
            if (ut.name().equalsIgnoreCase(text) || 
                ut.name().replace("_", " ").equalsIgnoreCase(text)) {
                return ut;
            }
        }
        System.err.println("Warning: Unknown UserType string encountered: " + text + ". Returning null.");
        return null; // Or throw new IllegalArgumentException("Unknown UserType: " + text);
    }
}