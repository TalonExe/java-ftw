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
    private LocalDate orderDate;
    private String purchaseManagerId;
    private String financeManagerId;
    private LocalDate approvalDate;
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
        this.orderDate = orderDate;
        this.purchaseManagerId = purchaseManagerId;
        this.financeManagerId = financeManagerId;
        this.approvalDate = approvalDate;
        this.status = status;
        this.quantity = quantity;
        this.itemCode = itemCode;
    }

    public PurchaseOrder(String poId, String prId, String supplier, String orderDateStr,
            String purchaseManagerId, String financeManagerId, String approvalDateStr,
            String status, int quantity, String itemCode) {
        this(poId, prId, supplier, LocalDate.parse(orderDateStr, DATE_FORMATTER),
                purchaseManagerId, financeManagerId,
                approvalDateStr != null && !approvalDateStr.isEmpty() ? LocalDate.parse(approvalDateStr, DATE_FORMATTER) : null,
                status, quantity, itemCode);
    }

    public PurchaseOrder() {
        this.orderDate = LocalDate.now();
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

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public String getOrderDateString() {
        return orderDate.format(DATE_FORMATTER);
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
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

    public LocalDate getApprovalDate() {
        return approvalDate;
    }

    public String getApprovalDateString() {
        return approvalDate != null ? approvalDate.format(DATE_FORMATTER) : "";
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = approvalDate;
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
        String approvalDateStr = approvalDate != null ? approvalDate.format(DATE_FORMATTER) : "";
        return poId + "," + prId + "," + supplier + "," + orderDate.format(DATE_FORMATTER) + ","
                + purchaseManagerId + "," + financeManagerId + "," + approvalDateStr + ","
                + status + "," + quantity + "," + itemCode;
    }
}
