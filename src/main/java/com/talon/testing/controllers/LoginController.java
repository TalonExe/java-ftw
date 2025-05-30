/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import com.talon.testing.models.User;
import static com.talon.testing.models.UserType.Administrator;
import com.talon.testing.utils.Router;

/**
 *
 * @author talon
 */
public class LoginController  extends Switchable{
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    
    @FXML
    private void handleLogin(ActionEvent event) {
    
    String username = usernameField.getText();
    String password = passwordField.getText();

    if (username.isEmpty() || password.isEmpty()) {
        messageLabel.setText("Username and password cannot be empty.");
        messageLabel.setTextFill(Color.RED); // Make sure messageLabel is styled for errors
        return;
    }

    try {
        User loggedInUser = UserController.loginUser(username, password);
        

        if (loggedInUser != null) {
            messageLabel.setText("Login successful! Welcome, " + loggedInUser.getUsername() + " (" + loggedInUser.getUserType() + ")");
            messageLabel.setTextFill(Color.GREEN); // Style for success

            switch (loggedInUser.getUserType()) {
                case Administrator:
                    router.switchScene("PO");
                    break;
                case Sales_Manager:
                    router.switchScene("Create PR");
                    break;
                case Finance_Manager:
                    router.switchScene("PO");
                    break;
                case Inventory_Manager:
                    router.switchScene("Stock Management");
                    break;
                case Purchase_Manager:
                    router.switchScene("Supplier List");
                    break;
                default:
                    throw new AssertionError();
            }

        } else {
            messageLabel.setText("Invalid username or password.");
            messageLabel.setTextFill(Color.RED);
        }
    } catch (Exception e) {
        messageLabel.setText("Error accessing user data. Please try again later.");
        messageLabel.setTextFill(Color.RED);
        e.printStackTrace(); // Log the full error for debugging
    }
}
}
