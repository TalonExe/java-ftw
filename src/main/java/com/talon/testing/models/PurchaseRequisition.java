/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PurchaseRequisition {
    private String prId;
    private LocalDate requestDate;
    private String salesManagerId;
    private LocalDate requiredDeliveryDate;
    private String status;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PurchaseRequisition(String prId, LocalDate requestDate, String salesManagerId, 
                              LocalDate requiredDeliveryDate, String status) {
        this.prId = prId;
        this.requestDate = requestDate;
        this.salesManagerId = salesManagerId;
        this.requiredDeliveryDate = requiredDeliveryDate;
        this.status = status;
    }
    
    public PurchaseRequisition(String prId, String requestDateStr, String salesManagerId, 
                              String requiredDeliveryDateStr, String status) {
        this.prId = prId;
        this.requestDate = LocalDate.parse(requestDateStr, DATE_FORMATTER);
        this.salesManagerId = salesManagerId;
        this.requiredDeliveryDate = LocalDate.parse(requiredDeliveryDateStr, DATE_FORMATTER);
        this.status = status;
    }

    // Default constructor for creating new PR
    public PurchaseRequisition() {
        this.requestDate = LocalDate.now();
        this.status = "Pending";
    }

    // Getters and setters
    public String getPrId() {
        return prId;
    }

    public void setPrId(String prId) {
        this.prId = prId;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public String getRequestDateString() {
        return requestDate.format(DATE_FORMATTER);
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public String getSalesManagerId() {
        return salesManagerId;
    }

    public void setSalesManagerId(String salesManagerId) {
        this.salesManagerId = salesManagerId;
    }

    public LocalDate getRequiredDeliveryDate() {
        return requiredDeliveryDate;
    }

    public String getRequiredDeliveryDateString() {
        return requiredDeliveryDate.format(DATE_FORMATTER);
    }

    public void setRequiredDeliveryDate(LocalDate requiredDeliveryDate) {
        this.requiredDeliveryDate = requiredDeliveryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return prId + "," + 
               requestDate.format(DATE_FORMATTER) + "," + 
               salesManagerId + "," + 
               requiredDeliveryDate.format(DATE_FORMATTER) + "," + 
               status;
    }
}
