package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Supplier {
    private String supplierId;
    private String supplierName;
    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String address;
    private String registrationDate;

    private static final String SUPPLIER_FILE_PATH = "/data/suppliers.txt";
    private static final Type SUPPLIER_MAP_TYPE = new TypeToken<Map<String, Supplier>>() {}.getType();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Supplier(String supplierId, String supplierName, String contactPerson, String email, String phoneNumber, String address, String registrationDate) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.registrationDate = registrationDate;
    }
    
    // No-arg constructor for Gson
    public Supplier() {}

    // Getters and Setters (as you have them)
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    // ... other getters/setters ...
    public String getContactPerson(){ return this.contactPerson; }
    public void setContactPerson(String contactPerson){ this.contactPerson = contactPerson; }
    public String getEmail(){ return this.email; }
    public void setEmail(String email){ this.email = email; }
    public String getPhoneNumber(){ return this.phoneNumber; }
    public void setPhoneNumber(String phoneNumber){ this.phoneNumber = phoneNumber; }
    public String getAddress(){ return this.address; }
    public void setAddress(String address){ this.address = address; }
    public String getRegistrationDate(){ return this.registrationDate; }
    public void setRegistrationDate(String registrationDate){ this.registrationDate = registrationDate; }


    @Override
    public String toString() { // Important for ComboBox display
        return (supplierName != null ? supplierName : "N/A") + " (" + (supplierId != null ? supplierId : "N/A") + ")";
    }

    public static Map<String, Supplier> loadSuppliers() throws IOException {
        Map<String, Supplier> supplierMap = new HashMap<>();
        try (InputStream inputStream = Supplier.class.getResourceAsStream(SUPPLIER_FILE_PATH)) {
            if (inputStream == null) {
                System.err.println("Supplier data file not found: " + SUPPLIER_FILE_PATH + ". Attempting to create.");
                File file = getFileFromResource(SUPPLIER_FILE_PATH, true);
                if (file != null && file.exists() && file.length() == 0) {
                    try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) { writer.write("{}"); }
                } else if (file == null || !file.exists()) {
                     System.err.println("Could not create or access Supplier file: " + SUPPLIER_FILE_PATH);
                }
                return supplierMap;
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                supplierMap = gson.fromJson(reader, SUPPLIER_MAP_TYPE);
                if (supplierMap == null) {
                    supplierMap = new HashMap<>();
                }
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from Supplier data file: " + SUPPLIER_FILE_PATH);
            throw new IOException("Error parsing Supplier JSON data.", e);
        }
        return supplierMap;
    }

    // Added saveSuppliers (similar to Item.saveItems)
    public static void saveSuppliers(Map<String, Supplier> supplierMap) throws IOException {
        File file = getFileFromResource(SUPPLIER_FILE_PATH, true);
        if (file == null) {
            throw new IOException("Could not get or create file for saving Suppliers at path: " + SUPPLIER_FILE_PATH);
        }
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(supplierMap, writer);
            System.out.println("Suppliers saved to: " + file.getAbsolutePath());
        }
    }
    
    // The getFileFromResource helper (essential for robust file I/O)
    // (Copied from Item.java - ideally, this would be in a shared FileUtils class)
    private static File getFileFromResource(String resourcePath, boolean createIfNotFound) throws IOException {
        URL resourceUrl = Supplier.class.getResource(resourcePath); // Use Supplier.class context
        File file;
        if (resourceUrl != null) {
            try {
                file = new File(resourceUrl.toURI());
            } catch (URISyntaxException e) {
                throw new IOException("Invalid resource URI: " + resourcePath, e);
            }
        } else {
             System.out.println("Resource " + resourcePath + " not found in classpath. Trying to create in expected build output location/user dir.");
            URL rootPath = Supplier.class.getProtectionDomain().getCodeSource().getLocation();
            File baseDir;
            try {
                baseDir = new File(rootPath.toURI());
                if (baseDir.isDirectory()) { 
                    file = new File(baseDir.getAbsolutePath() + resourcePath);
                } else { 
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
                        return null;
                    }
                }
                System.out.println("Attempting to create new file at: " + file.getAbsolutePath());
                if (!file.createNewFile()) {
                    System.err.println("Could not create new file: " + file.getAbsolutePath());
                    return null;
                }
                System.out.println("Created new data file: " + file.getAbsolutePath());
                if (file.length() == 0) { // Initialize with {}
                    try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                        writer.write("{}");
                    }
                }
            }
        }
        return file;
    }
}