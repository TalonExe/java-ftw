/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PurchaseOrder {
    private String poId;
    private String prId;
    private String supplier;
    private String orderDate;
    private String purchaseManagerId;
    private String financeManagerId;
    private String approvalDate;
    private String status;
    private int quantity;
    private String itemCode;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PurchaseOrder(String poId, String prId, String supplier, LocalDate orderDate,
                       String purchaseManagerId, String financeManagerId, LocalDate approvalDate, 
                       String status, int quantity, String itemCode) {
        this.poId = poId;
        this.prId = prId;
        this.supplier = supplier;
        this.orderDate = orderDate.toString();
        this.purchaseManagerId = purchaseManagerId;
        this.financeManagerId = financeManagerId;
        this.approvalDate = approvalDate.toString();
        this.status = status;
        this.quantity = quantity;
        this.itemCode = itemCode;
    }
    
    public PurchaseOrder(String poId, String prId, String supplier, String orderDate,
                       String purchaseManagerId, String financeManagerId, String approvalDateStr, 
                       String status, int quantity, String itemCode) {
        this.poId = poId;
        this.prId = prId;
        this.supplier = supplier;
        this.orderDate = orderDate;
        this.purchaseManagerId = purchaseManagerId;
        this.financeManagerId = financeManagerId;
        this.approvalDate = approvalDateStr != null && !approvalDateStr.isEmpty() ? 
                            LocalDate.parse(approvalDateStr, DATE_FORMATTER).toString() : null;
        this.status = status;
        this.quantity = quantity;
        this.itemCode = itemCode;
    }

    // Constructor for creating a new PO
    public PurchaseOrder() {
        this.orderDate = LocalDate.now().toString();
        this.status = "Pending";
    }

    // Getters and setters
    public String getPoId() {
        return poId;
    }

    public void setPoId(String poId) {
        this.poId = poId;
    }

    public String getPrId() {
        return prId;
    }

    public void setPrId(String prId) {
        this.prId = prId;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate.toString();
    }

    public String getPurchaseManagerId() {
        return purchaseManagerId;
    }

    public void setPurchaseManagerId(String purchaseManagerId) {
        this.purchaseManagerId = purchaseManagerId;
    }

    public String getFinanceManagerId() {
        return financeManagerId;
    }

    public void setFinanceManagerId(String financeManagerId) {
        this.financeManagerId = financeManagerId;
    }


    public String getApprovalDate() {
        return approvalDate != null ? approvalDate : "";
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = approvalDate.toString();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public String getItemCode() {
        return itemCode;
    }
    
    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    @Override
    public String toString() {
        return poId + "," + 
               prId + "," + 
               supplier + "," + 
               orderDate + "," + 
               purchaseManagerId + "," + 
               financeManagerId + "," + 
               approvalDate + "," + 
               status + "," +
               quantity + "," +
               itemCode;
    }
}