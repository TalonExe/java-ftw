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
public class Supplier {
    private String supplierId;
    private String supplierName;
    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String address;
    private String registrationDate;
    
    // File path for items storage
    private static final String ITEMS_FILE_PATH = "/data/suppliers.txt";
    // Type token for Map<String, Item>
    private static final Type ITEM_MAP_TYPE = new TypeToken<Map<String, Item>>() {}.getType();
    
    public Supplier(String supplierId, String supplierName, String contactPerson, String email, String phoneNumber, String address, String registrationDate) {
        setSupplierId(supplierId);
        setSupplierName(supplierName);
        setContactPerson(contactPerson);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setAddress(address);
        setRegistrationDate(registrationDate);
    }
    
    public String getSupplierId(){
        return this.supplierId;
    }
    
    public void setSupplierId(String supplierId){
        this.supplierId = supplierId;
    }
    
    public String getSupplierName(){
        return this.supplierName;
    }
    
    public void setSupplierName(String supplierName){
        this.supplierName = supplierName;
    }
    
    public String getContactPerson(){
        return this.contactPerson;
    }
    
    public void setContactPerson(String contactPerson){
        this.contactPerson = contactPerson;
    }
    
    public String getEmail(){
        return this.email;
    }
    
    public void setEmail(String email){
        this.email = email;
    }
    
    public String getPhoneNumber(){
        return this.phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
    
    public String getAddress(){
        return this.address;
    }
    
    public void setAddress(String address){
        this.address = address;
    }
    
    public String getRegistrationDate(){
        return this.registrationDate;
    }
    
    public void setRegistrationDate(String registrationDate){
        this.registrationDate = registrationDate;
    }
    
    public static Map<String, Supplier> loadSuppliers() throws IOException {
        Map<String, Supplier> supplierMap = new HashMap<>();
        Gson gson = new Gson();
        
        try (var inputStream = Supplier.class.getResourceAsStream(ITEMS_FILE_PATH)) {
            if (inputStream == null) {
                // If file doesn't exist, return empty map
                return supplierMap;
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                supplierMap = gson.fromJson(reader, ITEM_MAP_TYPE);
                // Handle null case if file exists but is empty or invalid
                if (supplierMap == null) {
                    supplierMap = new HashMap<>();
                }
            }
        }
        
        return supplierMap;
    }
}
