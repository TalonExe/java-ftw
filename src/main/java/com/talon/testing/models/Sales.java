/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author talon
 */
public class Sales {
    private String salesId;
    private String itemCode;
    private String quantitySold;
    private String salesDate;
    private String salesManagerId;
    
    // File path for items storage
    private static final String SALES_FILE_PATH = "/data/sales.txt";
    // Type token for Map<String, Item>
    private static final Type SALES_MAP_TYPE = new TypeToken<Map<String, Sales>>() {}.getType();

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public void setQuantitySold(String quantitySold) {
        this.quantitySold = quantitySold;
    }

    public void setSalesDate(String salesDate) {
        this.salesDate = salesDate;
    }

    public void setSalesId(String salesId) {
        this.salesId = salesId;
    }

    public void setSalesManagerId(String salesManagerId) {
        this.salesManagerId = salesManagerId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getQuantitySold() {
        return quantitySold;
    }

    public String getSalesDate() {
        return salesDate;
    }

    public String getSalesId() {
        return salesId;
    }

    public String getSalesManagerId() {
        return salesManagerId;
    }
    
    public static Map<String, Sales> loadSales() throws IOException {
        Map<String, Sales> salesMap = new HashMap<>();
        Gson gson = new Gson();
        
        try (var inputStream = Sales.class.getResourceAsStream(SALES_FILE_PATH)) {
            if (inputStream == null) {
                // If file doesn't exist, return empty map
                return salesMap;
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                salesMap = gson.fromJson(reader, SALES_MAP_TYPE);
                // Handle null case if file exists but is empty or invalid
                if (salesMap == null) {
                    salesMap = new HashMap<>();
                }
            }
        }
        
        return salesMap;
    }
        
}
