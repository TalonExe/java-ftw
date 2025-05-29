/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.controllers;

import com.talon.testing.utils.Router;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

/**
 *
 * @author TalonExe
 */
public class Switchable {
    Router router = Router.getInstance();
    
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
    
    
    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
