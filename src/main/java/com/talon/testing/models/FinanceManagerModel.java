///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.talon.testing.models;
//
//
//
//
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.Map;
//
//public class FinanceManagerModel {
//    // Sample data structures
//    private final ObservableList<PurchaseOrder> pendingPOs = FXCollections.observableArrayList();
//    private final ObservableList<PurchaseOrder> approvedPOs = FXCollections.observableArrayList();
//    private final ObservableList<PurchaseRequisition> purchaseRequisitions = FXCollections.observableArrayList();
//    private final ObservableList<Supplier> suppliers = FXCollections.observableArrayList();
//    
//    // Map of items to their suppliers
//    private final Map<String, ObservableList<Supplier>> itemSuppliersMap = new HashMap<>();
//    
//    public FinanceManagerModel() {
//        // Initialize with sample data
//        initializeSampleData();
//    }
//    
//    private void initializeSampleData() {
//        // Sample suppliers
//        suppliers.addAll(
//            new Supplier("S001", "ABC Supplies", "John Doe", "john@abc.com", "123456789", "123 Main St", LocalDate.now()),
//            new Supplier("S002", "XYZ Distributors", "Jane Smith", "jane@xyz.com", "987654321", "456 Oak Ave", LocalDate.now())
//        );
//        
//        // Sample purchase requisitions
//        purchaseRequisitions.addAll(
//            new PurchaseRequisition("PR001", LocalDate.now().minusDays(3), "SM001", LocalDate.now().plusDays(7), "Approved"),
//            new PurchaseRequisition("PR002", LocalDate.now().minusDays(1), "SM002", LocalDate.now().plusDays(5), "Pending")
//        );
//        
//        // Sample purchase orders
//        pendingPOs.addAll(
//            new PurchaseOrder("PO001", "PR001", LocalDate.now().minusDays(2), "PM001", null, null, "Pending"),
//            new PurchaseOrder("PO002", "PR002", LocalDate.now(), "PM002", null, null, "Pending")
//        );
//        
//        // Sample item-supplier mapping
//        ObservableList<Supplier> item1Suppliers = FXCollections.observableArrayList(suppliers.get(0), suppliers.get(1));
//        itemSuppliersMap.put("ITEM001", item1Suppliers);
//    }
//    
//    // Methods for PO approval
//    public boolean approvePO(PurchaseOrder po, String financeManagerId, int newQuantity, Supplier selectedSupplier) {
//        if (pendingPOs.contains(po)) {
//            po.setFinanceManagerId(financeManagerId);
//            po.setApprovalDate(LocalDate.now());
//            po.setStatus("Approved");
//            // Update quantity and supplier (in a real app, these would be fields in PO)
//            
//            pendingPOs.remove(po);
//            approvedPOs.add(po);
//            return true;
//        }
//        return false;
//    }
//    
//    // Methods to get data
//    public ObservableList<PurchaseOrder> getPendingPOs() {
//        return pendingPOs;
//    }
//    
//    public ObservableList<PurchaseOrder> getApprovedPOs() {
//        return approvedPOs;
//    }
//    
//    public ObservableList<PurchaseRequisition> getPurchaseRequisitions() {
//        return purchaseRequisitions;
//    }
//    
//    public ObservableList<Supplier> getSuppliersForItem(String itemCode) {
//        return itemSuppliersMap.getOrDefault(itemCode, FXCollections.emptyObservableList());
//    }
//    
//    // Method to generate financial reports
//    public String generateFinancialReport(LocalDate startDate, LocalDate endDate) {
//        // In a real app, this would query a database
//        return "Financial Report from " + startDate + " to " + endDate + "\n" +
//               "Total Approved POs: " + approvedPOs.size() + "\n" +
//               "Total Pending POs: " + pendingPOs.size();
//    }
//    
//    // Inner classes for data models
//    public static class PurchaseRequisition {
//        private final String prId;
//        private final LocalDate requestDate;
//        private final String salesManagerId;
//        private final LocalDate requiredDeliveryDate;
//        private final String status;
//        
//        public PurchaseRequisition(String prId, LocalDate requestDate, String salesManagerId, 
//                                 LocalDate requiredDeliveryDate, String status) {
//            this.prId = prId;
//            this.requestDate = requestDate;
//            this.salesManagerId = salesManagerId;
//            this.requiredDeliveryDate = requiredDeliveryDate;
//            this.status = status;
//        }
//        
//        // Getters and setters
//        public String getPrId() { return prId; }
//        public LocalDate getRequestDate() { return requestDate; }
//        public String getSalesManagerId() { return salesManagerId; }
//        public LocalDate getRequiredDeliveryDate() { return requiredDeliveryDate; }
//        public String getStatus() { return status; }
//    }
//    
//    public static class PurchaseOrder {
//        private final String poId;
//        private final String prId;
//        private final LocalDate orderDate;
//        private final String purchaseManagerId;
//        private String financeManagerId;
//        private LocalDate approvalDate;
//        private String status;
//        
//        public PurchaseOrder(String poId, String prId, LocalDate orderDate, String purchaseManagerId, 
//                            String financeManagerId, LocalDate approvalDate, String status) {
//            this.poId = poId;
//            this.prId = prId;
//            this.orderDate = orderDate;
//            this.purchaseManagerId = purchaseManagerId;
//            this.financeManagerId = financeManagerId;
//            this.approvalDate = approvalDate;
//            this.status = status;
//        }
//        
//        // Getters and setters
//        public String getPoId() { return poId; }
//        public String getPrId() { return prId; }
//        public LocalDate getOrderDate() { return orderDate; }
//        public String getPurchaseManagerId() { return purchaseManagerId; }
//        public String getFinanceManagerId() { return financeManagerId; }
//        public void setFinanceManagerId(String financeManagerId) { this.financeManagerId = financeManagerId; }
//        public LocalDate getApprovalDate() { return approvalDate; }
//        public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }
//        public String getStatus() { return status; }
//        public void setStatus(String status) { this.status = status; }
//    }
//    
//    public static class Supplier {
//        private final String supplierId;
//        private final String supplierName;
//        private final String contactPerson;
//        private final String email;
//        private final String phoneNumber;
//        private final String address;
//        private final LocalDate registrationDate;
//        
//        public Supplier(String supplierId, String supplierName, String contactPerson, String email, 
//                       String phoneNumber, String address, LocalDate registrationDate) {
//            this.supplierId = supplierId;
//            this.supplierName = supplierName;
//            this.contactPerson = contactPerson;
//            this.email = email;
//            this.phoneNumber = phoneNumber;
//            this.address = address;
//            this.registrationDate = registrationDate;
//        }
//        
//        // Getters
//        public String getSupplierId() { return supplierId; }
//        public String getSupplierName() { return supplierName; }
//        public String getContactPerson() { return contactPerson; }
//        public String getEmail() { return email; }
//        public String getPhoneNumber() { return phoneNumber; }
//        public String getAddress() { return address; }
//        public LocalDate getRegistrationDate() { return registrationDate; }
//        
//        @Override
//        public String toString() {
//            return supplierName;
//        }
//    }
//    
//    public static class DailySales {
//        private final String salesId;
//        private final String itemCode;
//        private final int quantitySold;
//        private final LocalDate salesDate;
//        private final String salesManagerId;
//        
//        public DailySales(String salesId, String itemCode, int quantitySold, LocalDate salesDate, String salesManagerId) {
//            this.salesId = salesId;
//            this.itemCode = itemCode;
//            this.quantitySold = quantitySold;
//            this.salesDate = salesDate;
//            this.salesManagerId = salesManagerId;
//        }
//        
//        // Getters
//        public String getSalesId() { return salesId; }
//        public String getItemCode() { return itemCode; }
//        public int getQuantitySold() { return quantitySold; }
//        public LocalDate getSalesDate() { return salesDate; }
//        public String getSalesManagerId() { return salesManagerId; }
//    }
//}