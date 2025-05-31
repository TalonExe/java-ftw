package com.talon.testing.controllers;

import com.talon.testing.models.User;
import com.talon.testing.utils.AppContext;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;

public class ProfileCardController implements Initializable {

    @FXML private Label profileUsernameLabel;
    @FXML private Label profileUserTypeLabel;
    @FXML private Label profileLoginTimeLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populate();
    }

    public void populate() { // Public method if needed to refresh
        User currentUser = AppContext.getCurrentUser();
        System.out.println("sldjkfhaskldjfaskldjfklasdjfkl;asdjfklasdfjkl");
        if (currentUser != null) {
            profileUsernameLabel.setText("User: " + currentUser.getUsername());
            profileUserTypeLabel.setText("Role: " + (currentUser.getUserType() != null ? currentUser.getUserType().toString() : "N/A"));
            profileLoginTimeLabel.setText("Logged in: " + AppContext.getFormattedLoginTime());
        } else {
            profileUsernameLabel.setText("User: Guest");
            profileUserTypeLabel.setText("Role: N/A");
            profileLoginTimeLabel.setText("Logged in: N/A");
        }
    }
}