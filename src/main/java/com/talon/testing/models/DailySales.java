/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.models;



public class DailySales{
    private String salesId;
    private String itemCode;
    private int quantitySold;
    private String salesDate;
    private String salesManagerId;
    
    public DailySales(String salesId, String itemCode, int quantitySold, String salesDate, String salesManagerId) {
        this.salesId = salesId;
        this.itemCode = itemCode;
        this.quantitySold = quantitySold;
        this.salesDate = salesDate;
        this.salesManagerId = salesManagerId;
    }
    
    // Getters
    public String getSalesId() { return salesId; }
    public String getItemCode() { return itemCode; }
    public int getQuantitySold() { return quantitySold; }
    public String getSalesDate() { return salesDate; }
    public String getSalesManagerId() { return salesManagerId; }
    
    // Setters (if needed)
    public void setSalesId(String salesId) { this.salesId = salesId; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public void setQuantitySold(int quantitySold) { this.quantitySold = quantitySold; }
    public void setSalesDate(String salesDate) { this.salesDate = salesDate; }
    public void setSalesManagerId(String salesManagerId) { this.salesManagerId = salesManagerId; }
}

