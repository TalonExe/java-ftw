/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PurchaseRequisition {
    private String prId;
    private String salesManagerId;
    private String ItemID;
    private int quantity;
    private String requiredDate;
    private String status;
    private String createdAt;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PurchaseRequisition(String prId, String salesManagerId, String ItemID, 
                             int quantity, String status, LocalDate createdAt) {
        this.prId = prId;
        this.salesManagerId = salesManagerId;
        this.ItemID = ItemID;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt.toString();
    }
    
    public PurchaseRequisition(String prId, String salesManagerId, String ItemID, 
                             int quantity, String status, String createdAt) {
        this.prId = prId;
        this.salesManagerId = salesManagerId;
        this.ItemID = ItemID;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = LocalDate.parse(createdAt, DATE_FORMATTER).toString();
    }

    // Default constructor for creating new PR
    public PurchaseRequisition() {
        this.createdAt = LocalDate.now().toString();
        this.status = "Pending";
    }

    // Getters and setters
    public String getPrId() {
        return prId;
    }

    public void setPrId(String prId) {
        this.prId = prId;
    }

    public String getSalesManagerId() {
        return salesManagerId;
    }

    public void setSalesManagerId(String salesManagerId) {
        this.salesManagerId = salesManagerId;
    }

    public String getItemID() {
        return ItemID;
    }

    public void setItemID(String ItemID) {
        this.ItemID = ItemID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCreatedAtString() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt.toString();
    }

    @Override
    public String toString() {
        return prId + "," + 
               salesManagerId + "," + 
               ItemID + "," + 
               quantity + "," + 
               status + "," + 
               createdAt;
    }
}