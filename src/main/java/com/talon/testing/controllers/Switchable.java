/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.controllers;

import com.talon.testing.utils.AppContext;
import com.talon.testing.utils.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.talon.testing.models.User;
import java.util.Optional;
import javafx.scene.control.ButtonType;

/**
 *
 * @author TalonExe
 */
public class Switchable {
    Router router = Router.getInstance();
    
    @FXML protected Label profileUsernameLabel;
    @FXML protected Label profileUserTypeLabel;
    @FXML protected Label profileLoginTimeLabel;
    @FXML private Button logoutButton; // Assuming fx:id="logoutButton" for the logout button

    
    @FXML
    public void switchToFinanceReport() { // This is for the side navigation buttons
        router.switchScene("Finance Report");
    }
    
    @FXML
    public void switchToPOApproval() { // This is for the side navigation buttons
        router.switchScene("PO");
        // Implement navigation or specific actions
    }
    
    @FXML
    public void switchToPR() { // This is for the side navigation buttons
        router.switchScene("PR");
    }
    
    @FXML
    public void switchToPayment() { // This is for the side navigation buttons
        router.switchScene("Process Payment");
    }
    
    @FXML
    public void switchToItemList() { // This is for the side navigation buttons
        router.switchScene("Item List");
    }
    
    @FXML
    public void switchToSupplierList() { // This is for the side navigation buttons
        router.switchScene("Supplier List");
    }
    
    @FXML
    public void switchToCreatePR() { // This is for the side navigation buttons
        router.switchScene("Create PR");
    }
    
    @FXML
    public void switchToSupplierEntry() { // This is for the side navigation buttons
        router.switchScene("Supplier Entry");
    }
    
    @FXML
    public void switchToViewPR() { // This is for the side navigation buttons
        router.switchScene("View PR");
    }
    
    @FXML
    public void switchToViewPO() { // This is for the side navigation buttons
        router.switchScene("View PO");
    }
    
    @FXML
    public void switchToItemEntry() { // This is for the side navigation buttons
        router.switchScene("Item Entry");
    }
    
    @FXML
    public void switchToSalesEntry() { // This is for the side navigation buttons
        router.switchScene("Sales Entry");
    }
    
    @FXML
    public void switchToViewPRPM() { // This is for the side navigation buttons
        router.switchScene("View PR PM");
    }
    
    @FXML
    public void switchToCreatePO() { // This is for the side navigation buttons
        router.switchScene("Create PO");
    }
    
    @FXML
    public void switchToStockManagement() { // This is for the side navigation buttons
        router.switchScene("Stock Management");
    }
    
    @FXML
    public void switchToGenerateReport() { // This is for the side navigation buttons
        router.switchScene("Generate Report");
    }
    
    @FXML
    public void switchToViewPRIM() { // This is for the side navigation buttons
        router.switchScene("View PR IM");
    }
    
    @FXML
    public void switchToItemListIM() { // This is for the side navigation buttons
        router.switchScene("Item List IM");
    }
    
     @FXML
    public void switchToManageUsers(ActionEvent event) { router.switchScene("Manage Users"); }
    @FXML
    public void switchToCreatePOAdmin(ActionEvent event) { router.switchScene("Create PO Admin"); }
    @FXML
    public void switchToCreatePRAdmin(ActionEvent event) { router.switchScene("Create PR Admin"); }
    @FXML
    public void switchToGenerateInventoryReportAdmin(ActionEvent event) { router.switchScene("Generate Inventory Report Admin"); }
    @FXML
    public void switchToItemEntryAdmin(ActionEvent event) { router.switchScene("Item Entry Admin"); }
    @FXML
    public void switchToProcessPaymentAdmin(ActionEvent event) { router.switchScene("Process Payment Admin"); }
    @FXML
    public void switchToStockManagementAdmin(ActionEvent event) { router.switchScene("Stock Management Admin"); }
    @FXML
    public void switchToSupplierEntryAdmin(ActionEvent event) { router.switchScene("Supplier Entry Admin"); }
    @FXML
    public void switchToGenerateSalesReportAdmin(ActionEvent event) { router.switchScene("Generate Sales Report"); } // 
    
    @FXML
    public void switchToSalesEntryAdmin(ActionEvent event) { router.switchScene("Sales Entry Admin"); }
    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    protected void handleLogout(ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Logout");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            AppContext.clearCurrentUser(); // Clear the global user session

            try {
                // Use the router to switch to the "Login" scene.
                // Ensure "Login" is a scene key you've registered with your Router
                // during application startup (e.g., router.loadScene("Login", "LoginView.fxml");)
                router.switchScene("login");
            } catch (Exception e) { // Catch a more general Exception from router if it doesn't specify IOException
                // Show an alert because direct UI interaction from base class is okay here
                showAlert(Alert.AlertType.ERROR, "Logout Error",
                          "Could not return to the login screen: " + e.getMessage());
                e.printStackTrace(); // Log the full error for debugging
            }
        }
    }
    
     @FXML
    private void test(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        System.out.println("Action for: " + clickedButton.getText() + " (not yet implemented for navigation)");
        // Example: if(clickedButton.getText().equals("Item Entry")) { /* load Item Entry FXML */ }
    }

}
