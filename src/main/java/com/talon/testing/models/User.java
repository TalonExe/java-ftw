/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.models;

/**
 *
 * @author talon
 */
public abstract class User {
    private String userID;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String userType;
    
    public User(){
        
    }
    
    public User(String userID, String username, String password, String fullName, String email, String phoneNumber, String userType){
        setUserID(userID);
        setUsername(username);
        setPassword(password);
        setFullName(fullName);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setUserType(userType);
    }
    
    public String getUserID(){
        return this.userID;
    }
    
    public void setUserID(String userID){
        this.userID = userID;
    }
    
    public String getUsername(){
        return this.username;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public String getPassword(){
        return this.password;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    public String getFullName(){
        return this.fullName;
    }
    
    public void setFullName(String fullName){
        this.password = fullName;
    }
    
    public String getEmail(){
        return this.email;
    }
    
    public void setEmail(String email){
        this.email = email;
    }
    
    public String getPhoneNumber(){
        return this.phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
    
    public String getUserType(){
        return this.userType;
    }
    
    public void setUserType(String userType){
        this.userType = userType;
    }
    
    public abstract boolean hasPermission(String action);
}
