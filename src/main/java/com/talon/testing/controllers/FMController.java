/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.talon.testing.models.Item;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author PC
 */
public class FMController {
    private final String PO_PATH = "/data/PO.txt";
    private final String PR_PATH = "/data/PR.txt";
    
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
