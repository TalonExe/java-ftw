/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.models;

import com.talon.testing.models.User;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 *
 * @author talon
 */
public class SalesManager extends User {
    
    private static final Set<String> ALLOWED_PERMISSIONS = new HashSet<>(Arrays.asList(
        "ITEM_VIEW", "ITEM_ADD", "ITEM_EDIT", "ITEM_DELETE",
        "SUPPLIER_VIEW", "SUPPLIER_ADD", "SUPPLIER_EDIT", "SUPPLIER_DELETE",
        "SALES_VIEW", "SALES_ADD", "SALES_EDIT", "SALES_DELETE",
        "PR_VIEW", "PR_CREATE", "PR_EDIT", "PR_DELETE",
        "PO_VIEW" // Can only view Purchase Orders
    ));
    
   public SalesManager() {
       super();
   }
   
   public SalesManager(String userID, String username, String password, String email, String phoneNumber, UserType userType){
       super(userID, username, password, email, phoneNumber, UserType.Sales_Manager);
   }
   
   public boolean hasPermission(String action) {
       return ALLOWED_PERMISSIONS.contains(action);
   }
   
   public Set<String> getAllowedPermissions() {
       return new HashSet<>(ALLOWED_PERMISSIONS);
   }
}
