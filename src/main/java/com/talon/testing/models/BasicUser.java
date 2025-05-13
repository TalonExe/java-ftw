/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.models;

/**
 *
 * @author tingwei
 */
public class BasicUser extends User {

    public BasicUser(String userID, String username, String password, String email, String phoneNumber, UserType userType) {
        super(userID, username, password, email, phoneNumber, userType);
    }

//    @Override
//    public boolean hasPermission(String action) {
//        //logic
//        return true;
//    }
}