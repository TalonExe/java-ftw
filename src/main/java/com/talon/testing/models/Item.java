package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.talon.testing.utils.FileUtils; // IMPORT FileUtils

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Item {
    private String itemCode;
    private String itemName;
    private String description;
    private String unitPrice;
    private String currentStock;
    private String minimumStock;
    private String createDate;
    private String supplierId;

    private static final String ITEMS_FILENAME = "items.txt"; // Just the filename
    private static final Type ITEM_MAP_TYPE = new TypeToken<Map<String, Item>>() {}.getType();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ... (Constructors, Getters, Setters, toString remain the same) ...
    public Item(String itemCode, String itemName, String description, String unitPrice,
                String currentStock, String minimumStock, String createDate, String supplierId) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.description = description;
        this.unitPrice = unitPrice;
        this.currentStock = currentStock;
        this.minimumStock = minimumStock;
        this.createDate = createDate;
        this.supplierId = supplierId;
    }
    public Item() {}
    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public String getDescription() { return description; }
    public String getUnitPrice() { return unitPrice; }
    public String getCurrentStock() { return currentStock; }
    public String getMinimumStock() { return minimumStock; }
    public String getCreateDate() { return createDate; }
    public String getSupplierId() { return supplierId; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setDescription(String description) { this.description = description; }
    public void setUnitPrice(String unitPrice) { this.unitPrice = unitPrice; }
    public void setCurrentStock(String currentStock) { this.currentStock = currentStock; }
    public void setMinimumStock(String minimumStock) { this.minimumStock = minimumStock; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    @Override
    public String toString() {
        return (itemName != null ? itemName : "N/A") + " (" + (itemCode != null ? itemCode : "N/A") + ")";
    }


    // --- Modified File I/O Methods ---
    public static Map<String, Item> loadItems() throws IOException {
        Map<String, Item> itemMap = new HashMap<>();
        // Use FileUtils to get the file, create if not found with default content "{}"
        File file = FileUtils.getDataFileFromProjectRoot(ITEMS_FILENAME, true, "{}");

        if (file == null || !file.exists() || !file.canRead()) {
            System.err.println("Cannot read item data file or file does not exist: " + (file != null ? file.getAbsolutePath() : ITEMS_FILENAME + " path problem"));
            return itemMap; // Return empty if file unusable
        }
        if (file.length() == 0 && !file.getName().endsWith(".tmp")) { // Check if it's empty (and not a temp file)
            System.out.println(ITEMS_FILENAME + " is empty. Returning empty map. It should have been initialized with '{}'.");
            return itemMap;
        }

        try (InputStream inputStream = new FileInputStream(file); // Read from the obtained File object
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            itemMap = gson.fromJson(reader, ITEM_MAP_TYPE);
            if (itemMap == null) { // If JSON content was "null" or invalid to produce null
                itemMap = new HashMap<>();
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from Item data file: " + file.getAbsolutePath() + ". Error: " + e.getMessage());
            throw new IOException("Error parsing Item JSON data from " + file.getAbsolutePath() + ". Please check file content.", e);
        }
        return itemMap;
    }

    public static void saveItems(Map<String, Item> itemMap) throws IOException {
        // Use FileUtils to get the file, create if not found with default content "{}"
        File file = FileUtils.getDataFileFromProjectRoot(ITEMS_FILENAME, true, "{}");
        if (file == null) {
            throw new IOException("Could not obtain file path for saving Items using filename: " + ITEMS_FILENAME);
        }

        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) { // Overwrites existing file
            gson.toJson(itemMap, writer);
            System.out.println("Items saved to: " + file.getAbsolutePath());
        }
    }

    public static boolean addItem(Item item) throws IOException {
        if (item == null || item.getItemCode() == null || item.getItemCode().trim().isEmpty()) {
            System.err.println("Cannot add item with null or empty item code.");
            return false;
        }
        Map<String, Item> itemMap = loadItems();
        if (itemMap.containsKey(item.getItemCode())) {
            System.err.println("Item with code " + item.getItemCode() + " already exists.");
            return false;
        }
        itemMap.put(item.getItemCode(), item);
        saveItems(itemMap);
        return true;
    }

    // The getFileFromResource method is now REMOVED from Item.java
    // (and from all other model classes)
}