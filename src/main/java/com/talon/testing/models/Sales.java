package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.talon.testing.utils.FileUtils; // IMPORT FileUtils

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
// No need for URL, URISyntaxException if FileUtils handles paths
import java.util.HashMap;
import java.util.Map;

public class Sales {
    private String salesId;
    private String itemCode;
    private int quantitySold;    // Good, it's int
    private String salesDate;    // Stored as String "yyyy-MM-dd"
    private String salesManagerId;

    // Filename constant
    private static final String SALES_FILENAME = "sales.txt"; // Changed from _FILE_PATH
    private static final Type SALES_MAP_TYPE = new TypeToken<Map<String, Sales>>() {}.getType();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Constructor
    public Sales(String salesId, String itemCode, int quantitySold, String salesDate, String salesManagerId) {
        this.salesId = salesId;
        this.itemCode = itemCode;
        this.quantitySold = quantitySold;
        this.salesDate = salesDate; // Expects "yyyy-MM-dd" string
        this.salesManagerId = salesManagerId;
    }

    // No-arg constructor for Gson
    public Sales() {}

    // Getters
    public String getSalesId() { return salesId; }
    public String getItemCode() { return itemCode; }
    public int getQuantitySold() { return quantitySold; }
    public String getSalesDate() { return salesDate; }
    public String getSalesManagerId() { return salesManagerId; }

    // Setters
    public void setSalesId(String salesId) { this.salesId = salesId; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public void setQuantitySold(int quantitySold) { this.quantitySold = quantitySold; }
    public void setSalesDate(String salesDate) { this.salesDate = salesDate; } // Expects "yyyy-MM-dd" string
    public void setSalesManagerId(String salesManagerId) { this.salesManagerId = salesManagerId; }


    // --- Modified File I/O Methods using FileUtils ---
    public static Map<String, Sales> loadSales() throws IOException {
        Map<String, Sales> salesMap = new HashMap<>();
        File file = FileUtils.getDataFileFromProjectRoot(SALES_FILENAME, true, "{}");

        if (file == null || !file.exists() || !file.canRead()) {
            System.err.println("Cannot read sales data file or file does not exist: " + (file != null ? file.getAbsolutePath() : SALES_FILENAME + " path problem"));
            return salesMap;
        }
        if (file.length() == 0) {
            System.out.println(SALES_FILENAME + " is empty. Returning empty map.");
            return salesMap;
        }

        try (InputStream inputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            salesMap = gson.fromJson(reader, SALES_MAP_TYPE);
            if (salesMap == null) {
                salesMap = new HashMap<>();
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from Sales data file: " + file.getAbsolutePath() + ". Error: " + e.getMessage());
            throw new IOException("Error parsing Sales JSON data from " + file.getAbsolutePath(), e);
        }
        return salesMap;
    }

    public static void saveSales(Map<String, Sales> salesMap) throws IOException {
        File file = FileUtils.getDataFileFromProjectRoot(SALES_FILENAME, true, "{}");
        if (file == null) {
            throw new IOException("Could not obtain file path for saving Sales using filename: " + SALES_FILENAME);
        }
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(salesMap, writer);
            System.out.println("Sales data saved to: " + file.getAbsolutePath());
        }
    }
    // REMOVE internal getFileFromResource if it existed here
}