/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.io.*;


/**
 *
 * @author talon
 */
public class Item {
    private String itemCode;
    private String itemName;
    private String description;
    private String unitPrice;
    private String currentStock;
    private String minimumStock;
    private String createDate;
    
    // File path for items storage
    private static final String ITEMS_FILE_PATH = "/data/items.txt";
    // Type token for Map<String, Item>
    private static final Type ITEM_MAP_TYPE = new TypeToken<Map<String, Item>>() {}.getType();
    
    public Item(String itemCode, String itemName, String description, String unitPrice, String currentStock, String minimumStock, String createDate) {
        setItemCode(itemCode);
        setItemName(itemName);
        setDescription(description);
        setUnitPrice(unitPrice);
        setCurrentStock(currentStock);
        setMinimumStock(minimumStock);
        setCreateDate(createDate);
    }
    
    public String getItemCode(){
        return this.itemCode;
    }
    
    public void setItemCode(String itemCode){
        this.itemCode = itemCode;
    }
    
    public String getItemName(){
        return this.itemName;
    }
    
    public void setItemName(String itemName){
        this.itemName = itemName;
    }
    
    public String getDescription(){
        return this.description;
    }
    
    public void setDescription(String description){
        this.description = description;
    }
    
    public String getUnitPrice(){
        return this.unitPrice;
    }
    
    public void setUnitPrice(String unitPrice){
        this.unitPrice = unitPrice;
    }
    
    public String getCurrentStock(){
        return this.currentStock;
    }
    
    public void setCurrentStock(String currentStock){
        this.currentStock = currentStock;
    }
    
    public String getMinimumStock(){
        return this.minimumStock;
    }
    
    public void setMinimumStock(String minimumStock){
        this.minimumStock = minimumStock;
    }
    
    public String getCreateDate(){
        return this.createDate;
    }
    
    public void setCreateDate(String createDate){
        this.createDate = createDate;
    }
    
    public static Map<String, Item> loadItems() throws IOException {
        Map<String, Item> itemMap = new HashMap<>();
        Gson gson = new Gson();
        
        try (var inputStream = Item.class.getResourceAsStream(ITEMS_FILE_PATH)) {
            if (inputStream == null) {
                // If file doesn't exist, return empty map
                return itemMap;
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                itemMap = gson.fromJson(reader, ITEM_MAP_TYPE);
                // Handle null case if file exists but is empty or invalid
                if (itemMap == null) {
                    itemMap = new HashMap<>();
                }
            }
        }
        
        return itemMap;
    }
    
    private static String determineFilePath() {
        // This is a simplification - in a real app you'd determine 
        // where you can write the file based on your application's structure
        String basePath = System.getProperty("user.dir");
        return basePath + "/src/main/resources" + ITEMS_FILE_PATH;
    }
    
    private static void saveItems(Map<String, Item> itemMap) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(itemMap, ITEM_MAP_TYPE);
        
        // Since we can't write directly to resources in a running app, 
        // we determine the actual path to write to
        String filePath = determineFilePath();
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(json);
        }
    }
    
    public static boolean addItem(Item item) throws IOException {
        if (item == null || item.getItemCode() == null) {
            return false;
        }
        
        Map<String, Item> itemMap = loadItems();
        
        // Check if item already exists
        if (itemMap.containsKey(item.getItemCode())) {
            return false; // Item already exists
        }
        
        // Add the new item
        itemMap.put(item.getItemCode(), item);
        
        // Save updated map
        saveItems(itemMap);
        return true;
    }
}
