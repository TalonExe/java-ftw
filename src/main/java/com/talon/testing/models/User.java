/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.models;

import java.time.LocalDateTime;

/**
 *
 * @author talon
 */
public abstract class User {
    private String userID;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private UserType userType;
    private LocalDateTime createTime;

    public User() {
    
    }

    public User(String userID, String username, String password, String email, String phoneNumber, UserType userType, LocalDateTime createTime) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
        this.createTime = createTime;
    }

    public String getUserID() { 
        return userID;
    }
    public String getUsername() {
        return username; 
    }
    public String getPassword() {
        return password; 
    }
    public String getEmail() {
        return email; 
    }
    public String getPhoneNumber() {
        return phoneNumber; 
    }
    public UserType getUserType() { 
        return userType; 
    }
    public LocalDateTime getCreateTime() {
        return createTime; 
    }

    public void setUserID(String userID) {
        this.userID = userID; 
    }
    public void setUsername(String username) {
        this.username = username; 
    }
    public void setPassword(String password) { 
        this.password = password;
    }
    public void setEmail(String email) { 
        this.email = email; 
    }
    public void setPhoneNumber(String phoneNumber) { 
        this.phoneNumber = phoneNumber; 
    }
    public void setUserType(UserType userType) { 
        this.userType = userType; 
    }
    public void setCreateTime(LocalDateTime createTime) { 
        this.createTime = createTime; 
    }

    public abstract boolean hasPermission(String action);
}