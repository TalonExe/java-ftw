package com.talon.testing.controllers;

import com.talon.testing.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;

public class loginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        try {
            Map<String, User> userMap = UserController.loadUsers();

            boolean loginSuccess = false;
            for (User user : userMap.values()) {
                if (user.getUsername().equals(username)) {
                    String hashedInputPassword = hashPassword(password);
                    if (user.getPassword().equals(hashedInputPassword)) {
                        loginSuccess = true;
                        showAlert(AlertType.INFORMATION, "Login Successful!", "Welcome, " + username + "!");
                        ((Stage) usernameField.getScene().getWindow()).close();
                        break;
                    }
                }
            }

            if (!loginSuccess) {
                messageLabel.setText("Invalid username or password.");
            }

        } catch (IOException e) {
            messageLabel.setText("Error loading user data.");
            e.printStackTrace();
        }
    }

    // SHA-256 hashing
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println("Password hashing failed: " + e.getMessage());
            return "";
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
