/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talon.testing.controllers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.talon.testing.models.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
/**
 *
 * @author tingwei
 */
public class UserController {

    private static final String USERS_FILE_PATH = "/data/user.txt";
    private static final Type USER_MAP_TYPE = new TypeToken<Map<String, User>>(){}.getType();
    private static final Gson gson = new Gson();

    // Load all users
    public static Map<String, User> loadUsers() throws IOException {
        Map<String, User> userMap = new HashMap<>();

        try (var inputStream = UserController.class.getResourceAsStream(USERS_FILE_PATH)) {
            if (inputStream == null) {
                System.out.println("salam");
                return userMap;
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                userMap = gson.fromJson(reader, USER_MAP_TYPE);
                if (userMap == null) {
                    userMap = new HashMap<>();
                }
            }
        }
        return userMap;
    }

    
    public static void saveUsers(Map<String, User> userMap) throws IOException {
        File file = new File(UserController.class.getResource(USERS_FILE_PATH).getFile());
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(userMap, writer);
        }
    }

    // Add new user
    public static void addUser(User user) throws IOException {
        Map<String, User> userMap = loadUsers();
        userMap.put(user.getUserID(), user);
        saveUsers(userMap);
    }

    // Update existing user
    public static void updateUser(User updatedUser) throws IOException {
        Map<String, User> userMap = loadUsers();
        if (userMap.containsKey(updatedUser.getUserID())) {
            userMap.put(updatedUser.getUserID(), updatedUser);
            saveUsers(userMap);
        } else {
            throw new IllegalArgumentException("User not found: " + updatedUser.getUserID());
        }
    }

    // Delete a user by ID
    public static void deleteUser(String userID) throws IOException {
        Map<String, User> userMap = loadUsers();
        if (userMap.containsKey(userID)) {
            userMap.remove(userID);
            saveUsers(userMap);
        } else {
            throw new IllegalArgumentException("User not found: " + userID);
        }
    }

    // Get a single user by ID
    public static User getUser(String userID) throws IOException {
        Map<String, User> userMap = loadUsers();
        return userMap.get(userID);
    }

    // List all users
    public static Collection<User> getAllUsers() throws IOException {
        Map<String, User> userMap = loadUsers();
        return userMap.values();
    }
}