package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryManagerModel {

    private static final String FILE_PATH = "items.json"; // Path to store item data
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // =====================
    // Inner Item Class
    // =====================
    public static class Item {
        private StringProperty itemCode;
        private StringProperty itemName;
        private StringProperty description;
        private StringProperty unitPrice;
        private StringProperty currentStock;
        private StringProperty minimumStock;
        private StringProperty createDate;

        public Item(String itemCode, String itemName, String description, String unitPrice,
                    String currentStock, String minimumStock, String createDate) {
            this.itemCode = new SimpleStringProperty(itemCode);
            this.itemName = new SimpleStringProperty(itemName);
            this.description = new SimpleStringProperty(description);
            this.unitPrice = new SimpleStringProperty(unitPrice);
            this.currentStock = new SimpleStringProperty(currentStock);
            this.minimumStock = new SimpleStringProperty(minimumStock);
            this.createDate = new SimpleStringProperty(createDate);
        }

        // Property getters for JavaFX binding
        public StringProperty itemCodeProperty() { return itemCode; }
        public StringProperty itemNameProperty() { return itemName; }
        public StringProperty descriptionProperty() { return description; }
        public StringProperty unitPriceProperty() { return unitPrice; }
        public StringProperty currentStockProperty() { return currentStock; }
        public StringProperty minimumStockProperty() { return minimumStock; }
        public StringProperty createDateProperty() { return createDate; }

        // Basic getters
        public String getItemCode() { return itemCode.get(); }
        public String getItemName() { return itemName.get(); }
        public String getDescription() { return description.get(); }
        public String getUnitPrice() { return unitPrice.get(); }
        public String getCurrentStock() { return currentStock.get(); }
        public String getMinimumStock() { return minimumStock.get(); }
        public String getCreateDate() { return createDate.get(); }

        // Setters
        public void setItemCode(String itemCode) { this.itemCode.set(itemCode); }
        public void setItemName(String itemName) { this.itemName.set(itemName); }
        public void setDescription(String description) { this.description.set(description); }
        public void setUnitPrice(String unitPrice) { this.unitPrice.set(unitPrice); }
        public void setCurrentStock(String currentStock) { this.currentStock.set(currentStock); }
        public void setMinimumStock(String minimumStock) { this.minimumStock.set(minimumStock); }
        public void setCreateDate(String createDate) { this.createDate.set(createDate); }

        // Logic to check if item is low in stock
        public boolean isLowStock() {
            try {
                int current = Integer.parseInt(currentStock.get());
                int min = Integer.parseInt(minimumStock.get());
                return current < min;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    // =====================
    // Model Methods
    // =====================

    // Load items from JSON file
    public List<Item> loadItems() {
        List<Item> items = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return items;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Item[] loadedItems = gson.fromJson(reader, Item[].class);
            if (loadedItems != null) {
                items = Arrays.asList(loadedItems);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return items;
    }

    // Save items to JSON file
    public void saveItems(List<Item> items) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(items, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update stock of a specific item
    public boolean updateStock(String itemCode, String newStock) {
        List<Item> items = loadItems();
        boolean updated = false;

        for (Item item : items) {
            if (item.getItemCode().equalsIgnoreCase(itemCode)) {
                item.setCurrentStock(newStock);
                updated = true;
                break;
            }
        }

        if (updated) {
            saveItems(items);
        }

        return updated;
    }

    // Get all items that are below minimum stock
    public List<Item> getLowStockItems() {
        List<Item> lowStock = new ArrayList<>();
        for (Item item : loadItems()) {
            if (item.isLowStock()) {
                lowStock.add(item);
            }
        }
        return lowStock;
    }
}
