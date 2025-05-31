package com.talon.testing.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Import for formatting

public class User {
    private String userID; // Matches your field name
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private UserType userType;
    private String createTime; // Stored as String

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    // No-arg constructor for Gson and manual creation
    public User() {
        // Set a default createTime only if it's truly a new object not being populated by Gson
        // Gson will call setters if they exist and match JSON fields.
        // If createTime is not in JSON, this will be its default.
        if (this.createTime == null) {
             this.createTime = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        }
    }

    // Constructor for creating new users programmatically
    public User(String userID, String username, String password, String email, String phoneNumber, UserType userType) {
        this.userID = userID;
        this.username = username;
        this.password = password; // In a real app, hash this
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
        this.createTime = LocalDateTime.now().format(TIMESTAMP_FORMATTER); // Set creation time for new instances
    }

    // Getters
    public String getUserID() { return userID; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public UserType getUserType() { return userType; }
    public String getCreateTime() { return createTime; }

    // For TableView display, formatted version
    public String getFormattedCreateTime() {
        try {
            if (createTime == null || createTime.isEmpty()) return "N/A";
            // Assuming createTime from JSON is already ISO_LOCAL_DATE_TIME
            LocalDateTime dt = LocalDateTime.parse(createTime, TIMESTAMP_FORMATTER);
            return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return createTime; // Return raw string if parsing/formatting fails
        }
    }

    // Setters
    public void setUserID(String userID) { this.userID = userID; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setUserType(UserType userType) { this.userType = userType; }

    // Setter for Gson to use if userType in JSON is a String
    public void setUserType(String userTypeString) {
        this.userType = UserType.fromString(userTypeString);
    }

    // Setter for createTime that accepts a String (e.g., from JSON)
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    // Method to explicitly update createTime to now (if needed for updates)
    public void updateCreateTimeToNow() {
        this.createTime = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }


    @Override
    public String toString() {
        return (username != null ? username : "N/A") +
               (userType != null ? " (" + userType.toString() + ")" : " (No Type)");
    }
}