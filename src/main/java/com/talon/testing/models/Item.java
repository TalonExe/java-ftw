// In Item.java
package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File; // Import File
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream; // Import InputStream
import java.io.InputStreamReader;
import java.io.Writer; // Import Writer
import java.lang.reflect.Type;
import java.net.URISyntaxException; // Import
import java.net.URL; // Import
import java.nio.charset.StandardCharsets; // Import
import java.util.HashMap;
import java.util.Map;

public class Item {
    private String itemCode;
    private String itemName;
    private String description;
    private String unitPrice;     // Kept as String as per your model
    private String currentStock;  // Kept as String
    private String minimumStock;  // Kept as String
    private String createDate;
    private String supplierId;    // <-- NEW FIELD

    private static final String ITEMS_FILE_PATH = "/data/items.txt";
    private static final Type ITEM_MAP_TYPE = new TypeToken<Map<String, Item>>() {}.getType();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Shared instance

    // Updated Constructor
    public Item(String itemCode, String itemName, String description, String unitPrice,
                String currentStock, String minimumStock, String createDate, String supplierId) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.description = description;
        this.unitPrice = unitPrice;
        this.currentStock = currentStock;
        this.minimumStock = minimumStock;
        this.createDate = createDate;
        this.supplierId = supplierId; // Initialize new field
    }

    // No-arg constructor for Gson
    public Item() {}

    // Getters
    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public String getDescription() { return description; }
    public String getUnitPrice() { return unitPrice; }
    public String getCurrentStock() { return currentStock; }
    public String getMinimumStock() { return minimumStock; }
    public String getCreateDate() { return createDate; }
    public String getSupplierId() { return supplierId; } // Getter for new field

    // Setters
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setDescription(String description) { this.description = description; }
    public void setUnitPrice(String unitPrice) { this.unitPrice = unitPrice; }
    public void setCurrentStock(String currentStock) { this.currentStock = currentStock; }
    public void setMinimumStock(String minimumStock) { this.minimumStock = minimumStock; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; } // Setter for new field

    @Override
    public String toString() { // Useful for ComboBox display if no cellFactory
        return (itemName != null ? itemName : "N/A") + " (" + (itemCode != null ? itemCode : "N/A") + ")";
    }

    // --- Robust File I/O Methods ---
    public static Map<String, Item> loadItems() throws IOException {
        Map<String, Item> itemMap = new HashMap<>();
        try (InputStream inputStream = Item.class.getResourceAsStream(ITEMS_FILE_PATH)) {
            if (inputStream == null) {
                System.err.println("Item data file not found: " + ITEMS_FILE_PATH + ". Attempting to create.");
                File file = getFileFromResource(ITEMS_FILE_PATH, true); // Create if not exists
                if (file != null && file.exists() && file.length() == 0) {
                    try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) { writer.write("{}"); }
                } else if (file == null || !file.exists()) {
                    System.err.println("Could not create or access Item file: " + ITEMS_FILE_PATH);
                }
                return itemMap; // Return empty, or potentially a map from a newly created empty file
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                itemMap = gson.fromJson(reader, ITEM_MAP_TYPE);
                if (itemMap == null) {
                    itemMap = new HashMap<>();
                }
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from Item data file: " + ITEMS_FILE_PATH);
            throw new IOException("Error parsing Item JSON data.", e);
        }
        return itemMap;
    }

    public static void saveItems(Map<String, Item> itemMap) throws IOException {
        File file = getFileFromResource(ITEMS_FILE_PATH, true); // Get file, attempt creation
        if (file == null) {
            throw new IOException("Could not get or create file for saving Items at path: " + ITEMS_FILE_PATH);
        }
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(itemMap, writer);
            System.out.println("Items saved to: " + file.getAbsolutePath());
        }
    }
    
    // Add item, then save all items. Consider if this is the desired pattern vs. instance method in controller
    public static boolean addItem(Item item) throws IOException {
        if (item == null || item.getItemCode() == null || item.getItemCode().trim().isEmpty()) {
            System.err.println("Cannot add item with null or empty item code.");
            return false;
        }
        Map<String, Item> itemMap = loadItems(); // Load current items
        if (itemMap.containsKey(item.getItemCode())) {
            System.err.println("Item with code " + item.getItemCode() + " already exists.");
            return false; 
        }
        itemMap.put(item.getItemCode(), item);
        saveItems(itemMap); // Save the entire map
        return true;
    }
    
    // The getFileFromResource helper (essential for robust file I/O)
    private static File getFileFromResource(String resourcePath, boolean createIfNotFound) throws IOException {
        URL resourceUrl = Item.class.getResource(resourcePath);
        File file;
        if (resourceUrl != null) {
            try {
                file = new File(resourceUrl.toURI());
            } catch (URISyntaxException e) {
                throw new IOException("Invalid resource URI: " + resourcePath, e);
            }
        } else {
            System.out.println("Resource " + resourcePath + " not found in classpath. Trying to create in expected build output location/user dir.");
            URL rootPath = Item.class.getProtectionDomain().getCodeSource().getLocation();
            File baseDir;
            try {
                baseDir = new File(rootPath.toURI());
                if (baseDir.isDirectory()) { // e.g., /target/classes
                    file = new File(baseDir.getAbsolutePath() + resourcePath);
                } else { // e.g., a JAR file
                    file = new File(baseDir.getParentFile().getAbsolutePath() + resourcePath);
                }
            } catch (Exception e) {
                System.err.println("Could not determine base directory for " + resourcePath + ". Error: " + e.getMessage() + ". Falling back to user home.");
                String fallbackPath = System.getProperty("user.home") + File.separator + "TalonAppData" + resourcePath.replace('/', File.separatorChar);
                file = new File(fallbackPath);
            }

            if (createIfNotFound && !file.exists()) {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    if (!parentDir.mkdirs()) {
                        System.err.println("Could not create directory: " + parentDir.getAbsolutePath());
                        return null; // Or throw
                    }
                }
                System.out.println("Attempting to create new file at: " + file.getAbsolutePath());
                if (!file.createNewFile()) {
                    System.err.println("Could not create new file: " + file.getAbsolutePath());
                    return null; // Or throw
                }
                System.out.println("Created new data file: " + file.getAbsolutePath());
                 // Initialize with empty JSON object if newly created
                if (file.length() == 0) {
                    try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                        writer.write("{}");
                    }
                }
            }
        }
        return file;
    }
}