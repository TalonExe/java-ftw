package com.talon.testing.controllers;

import com.talon.testing.models.BasicUser;
import com.talon.testing.models.User;
import com.talon.testing.models.UserType;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserManagementController implements Initializable {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> userIdCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> phoneCol;
    @FXML private TableColumn<User, UserType> typeCol;
    @FXML private TableColumn<User, String> createdCol;
    
    @FXML private TextField userIdField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<UserType> userTypeCombo;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userID"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("userType"));
        createdCol.setCellValueFactory(cellData -> 
            cellData.getValue().getCreateTime() != null 
                ? javafx.beans.binding.Bindings.createStringBinding(() -> 
                    cellData.getValue().getCreateTime().format(formatter))
                : javafx.beans.binding.Bindings.createStringBinding(() -> ""));
        
        // Initialize combo box
        userTypeCombo.setItems(FXCollections.observableArrayList(UserType.values()));
        
        // Load user data
        loadUserData();
        
        // Set up table selection listener
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
        
        // Set up button actions
        addButton.setOnAction(e -> addUser());
        updateButton.setOnAction(e -> updateUser());
        deleteButton.setOnAction(e -> deleteUser());
        clearButton.setOnAction(e -> clearForm());
    }
    
    private void loadUserData() {
        try {
            userList.setAll(UserController.getAllUsers());
            userTable.setItems(userList);
        } catch (IOException ex) {
            showAlert("Error", "Failed to load users: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void populateForm(User user) {
        userIdField.setText(user.getUserID());
        usernameField.setText(user.getUsername());
        passwordField.setText(user.getPassword());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhoneNumber());
        userTypeCombo.setValue(user.getUserType());
    }
    
    private void clearForm() {
        userIdField.clear();
        usernameField.clear();
        passwordField.clear();
        emailField.clear();
        phoneField.clear();
        userTypeCombo.getSelectionModel().clearSelection();
        userTable.getSelectionModel().clearSelection();
    }
    
    private void addUser() {
        try {
            String hashedPassword = hashPassword(passwordField.getText());

            BasicUser newUser = new BasicUser(
                userIdField.getText(),
                usernameField.getText(),
                hashedPassword,
                emailField.getText(),
                phoneField.getText(),
                userTypeCombo.getValue()
            );

            UserController.addUser(newUser);
            loadUserData();
            clearForm();
            showAlert("Success", "User added successfully", Alert.AlertType.INFORMATION);
        } catch (Exception ex) {
            showAlert("Error", "Failed to add user: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    
private void updateUser() {
    User selectedUser = userTable.getSelectionModel().getSelectedItem();
    if (selectedUser == null) {
        showAlert("Warning", "Please select a user to update", Alert.AlertType.WARNING);
        return;
    }

    try {
        String hashedPassword = hashPassword(passwordField.getText());

        BasicUser updatedUser = new BasicUser(
            userIdField.getText(),
            usernameField.getText(),
            hashedPassword,
            emailField.getText(),
            phoneField.getText(),
            userTypeCombo.getValue()
        );

        UserController.updateUser(updatedUser);
        loadUserData();
        clearForm();
        showAlert("Success", "User updated successfully", Alert.AlertType.INFORMATION);
    } catch (Exception ex) {
        showAlert("Error", "Failed to update user: " + ex.getMessage(), Alert.AlertType.ERROR);
    }
}

    
    private void deleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Warning", "Please select a user to delete", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            UserController.deleteUser(selectedUser.getUserID());
            loadUserData();
            clearForm();
            showAlert("Success", "User deleted successfully", Alert.AlertType.INFORMATION);
        } catch (Exception ex) {
            showAlert("Error", "Failed to delete user: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}