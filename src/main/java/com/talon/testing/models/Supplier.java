package com.talon.testing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.talon.testing.utils.FileUtils; // IMPORT FileUtils

import java.io.*; // Keep general IO imports
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
// Remove unused URL, URISyntaxException if getFileFromResource is fully replaced

public class Supplier {
    private String supplierId;
    private String supplierName;
    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String address;
    private String registrationDate;

    private static final String SUPPLIER_FILENAME = "suppliers.txt"; // Renamed constant
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
    
    public Supplier() {}

    // Getters and Setters ... (keep as they are) ...
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
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
    public String toString() {
        return (supplierName != null ? supplierName : "N/A") + " (" + (supplierId != null ? supplierId : "N/A") + ")";
    }

    public static Map<String, Supplier> loadSuppliers() throws IOException {
        Map<String, Supplier> supplierMap = new HashMap<>();
        // Use FileUtils to get the file, create if not found with default content "{}"
        File file = FileUtils.getDataFileFromProjectRoot(SUPPLIER_FILENAME, true, "{}");

        if (file == null || !file.exists() || !file.canRead()) {
            System.err.println("Cannot read supplier data file or file does not exist: " + (file != null ? file.getAbsolutePath() : SUPPLIER_FILENAME + " path problem"));
            return supplierMap;
        }
        if (file.length() == 0) {
            System.out.println(SUPPLIER_FILENAME + " is empty. Returning empty map.");
            return supplierMap;
        }

        try (InputStream inputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            supplierMap = gson.fromJson(reader, SUPPLIER_MAP_TYPE);
            if (supplierMap == null) {
                supplierMap = new HashMap<>();
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from Supplier data file: " + file.getAbsolutePath() + ". Error: " + e.getMessage());
            throw new IOException("Error parsing Supplier JSON data from " + file.getAbsolutePath(), e);
        }
        return supplierMap;
    }

    public static void saveSuppliers(Map<String, Supplier> supplierMap) throws IOException {
        File file = FileUtils.getDataFileFromProjectRoot(SUPPLIER_FILENAME, true, "{}");
        if (file == null) {
            throw new IOException("Could not obtain file path for saving Suppliers using filename: " + SUPPLIER_FILENAME);
        }
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(supplierMap, writer);
            System.out.println("Suppliers saved to: " + file.getAbsolutePath());
        }
    }
    
    // REMOVE the old private static File getFileFromResource method from this class
}