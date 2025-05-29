package com.talon.testing.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.talon.testing.models.User; // Assuming this is your User model
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
// It's good practice to use a proper logging framework, but for simplicity:
// import java.util.logging.Level;
// import java.util.logging.Logger;

public class UserController {

    private static final String USERS_FILE_PATH = "/data/user.txt"; // Path relative to resources folder
    private static final Type USER_MAP_TYPE = new TypeToken<Map<String, User>>() {}.getType();
    private static final Gson gson = new Gson(); // Using default Gson for simplicity
    // private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());


    // Load all users
    public static Map<String, User> loadUsers() throws IOException {
        Map<String, User> userMap = new HashMap<>();

        // Try-with-resources ensures the stream is closed automatically
        try (var inputStream = UserController.class.getResourceAsStream(USERS_FILE_PATH)) {
            if (inputStream == null) {
                // LOGGER.warning("User data file not found: " + USERS_FILE_PATH);
                System.err.println("Warning: User data file not found at classpath:" + USERS_FILE_PATH + ". Returning empty user map.");
                // Consider creating an empty file here if it's the first run and you want to save to it later.
                // For now, just returning an empty map is fine for loading.
                return userMap;
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                userMap = gson.fromJson(reader, USER_MAP_TYPE);
                if (userMap == null) { // Handles case where file is empty or not valid JSON map
                    userMap = new HashMap<>();
                }
            }
        } catch (Exception e) { // Catch broader exceptions during file reading/parsing
            // LOGGER.log(Level.SEVERE, "Error loading users from file: " + USERS_FILE_PATH, e);
            System.err.println("Error loading users from file: " + USERS_FILE_PATH + " - " + e.getMessage());
            // Depending on the severity, you might re-throw or return an empty map.
            // For login, if data can't be loaded, no one can log in.
            throw new IOException("Failed to load user data.", e);
        }
        return userMap;
    }

    // Save users - Be cautious with getResource().getFile() if running from a JAR
    public static void saveUsers(Map<String, User> userMap) throws IOException {
        // This approach to get the file path might not work if running from a JAR.
        // For writing, it's often better to use a predefined absolute path
        // or a path relative to the application's working directory or user home.
        java.net.URL resourceUrl = UserController.class.getResource(USERS_FILE_PATH);
        if (resourceUrl == null) {
            // This means the file wasn't found in the classpath. If you intend to create it:
            // You'll need to figure out the correct absolute path in your build/output directory.
            // For simplicity, this example assumes it exists or can be written to via an external path.
            throw new IOException("Cannot find resource path to save users: " + USERS_FILE_PATH + ". Saving might not work correctly if packaged in a JAR.");
        }

        File file;
        try {
            file = new File(resourceUrl.toURI()); // Better way to convert URL to File
             // Ensure parent directories exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new IOException("Could not create parent directories for " + file.getAbsolutePath());
                }
            }
        } catch (java.net.URISyntaxException e) {
            throw new IOException("Invalid URI syntax for user file path.", e);
        }
        
        // Using GsonBuilder for pretty printing, easier to debug the JSON file
        Gson gsonWriter = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(file)) {
            gsonWriter.toJson(userMap, writer);
        }
    }

    // --- Login Method ---
    /**
     * Attempts to log in a user with the given username and password.
     *
     * @param username The username to check.
     * @param password The password to verify.
     * @return The User object if login is successful, null otherwise.
     * @throws IOException If there's an error loading user data.
     */
    public static User loginUser(String username, String password) throws IOException {
        Map<String, User> userMap = loadUsers();

        if (userMap == null || userMap.isEmpty()) {
            // LOGGER.info("No users found in the system during login attempt.");
            System.out.println("No users found in the system.");
            return null;
        }

        for (User user : userMap.values()) {
            // Compare username (often case-insensitive for login)
            if (user.getUsername().equalsIgnoreCase(username)) {
                // IMPORTANT: Password comparison
                // This is a PLAIN TEXT password comparison.
                // In a real application, you MUST hash passwords before storing them
                // and then hash the input password here and compare the hashes.
                // Example: if (BCrypt.checkpw(password, user.getHashedPassword()))
                if (user.getPassword().equals(password)) {
                    // LOGGER.info("User logged in successfully: " + username);
                    System.out.println("Login successful for: " + username);
                    return user; // Credentials match
                } else {
                    // LOGGER.warning("Password mismatch for user: " + username);
                    System.out.println("Password mismatch for user: " + username);
                    return null; // Username found, but password incorrect
                }
            }
        }
        // LOGGER.info("Username not found: " + username);
        System.out.println("Username not found: " + username);
        return null; // Username not found
    }


    // Add new user
    public static void addUser(User user) throws IOException {
        Map<String, User> userMap = loadUsers();
        // IMPORTANT: If adding users, ensure the password in the 'user' object
        // is hashed before calling this method or hash it here.
        // e.g., user.setPassword(hashFunction(user.getPassword()));
        if (userMap.containsKey(user.getUserID())) { // Assuming userID is the key
            throw new IllegalArgumentException("User with ID " + user.getUserID() + " already exists.");
        }
        // Also check for unique username if that's a constraint
        for (User existingUser : userMap.values()) {
            if (existingUser.getUsername().equalsIgnoreCase(user.getUsername())) {
                throw new IllegalArgumentException("User with username " + user.getUsername() + " already exists.");
            }
        }
        userMap.put(user.getUserID(), user);
        saveUsers(userMap);
        // LOGGER.info("User added: " + user.getUsername());
    }

    // Update existing user
    public static void updateUser(User updatedUser) throws IOException {
        Map<String, User> userMap = loadUsers();
        if (userMap.containsKey(updatedUser.getUserID())) {
            // IMPORTANT: If password can be updated, ensure it's hashed.
            userMap.put(updatedUser.getUserID(), updatedUser);
            saveUsers(userMap);
            // LOGGER.info("User updated: " + updatedUser.getUsername());
        } else {
            // LOGGER.warning("Attempted to update non-existent user: " + updatedUser.getUserID());
            throw new IllegalArgumentException("User not found: " + updatedUser.getUserID());
        }
    }

    // Delete a user by ID (which is the key in your map)
    public static void deleteUser(String userID) throws IOException {
        Map<String, User> userMap = loadUsers();
        if (userMap.containsKey(userID)) {
            User removedUser = userMap.remove(userID);
            saveUsers(userMap);
            // LOGGER.info("User deleted: " + (removedUser != null ? removedUser.getUsername() : userID));
        } else {
            // LOGGER.warning("Attempted to delete non-existent user: " + userID);
            throw new IllegalArgumentException("User not found: " + userID);
        }
    }

    // Get a single user by ID (key in your map)
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