// In UserController.java (and similarly in Item.java, Supplier.java, etc.)
package com.talon.testing.controllers; // Or your models package

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.talon.testing.models.User;
import com.talon.testing.models.UserType; // Assuming you have UserTypeAdapter if needed
import com.talon.testing.utils.FileUtils; // IMPORT THE NEW UTILITY CLASS

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserController {
    private static final String USERS_DATA_FILENAME = "users.txt"; // Just the filename
    private static final Type USER_MAP_TYPE = new TypeToken<Map<String, User>>() {}.getType();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(UserType.class, new UserTypeAdapter()) // Keep if using
            .setPrettyPrinting().create();

    // No static initializer block needed here if FileUtils.getDataFileFromProjectRoot handles creation robustly

    public static List<User> getAllUsers() throws IOException {
        Map<String, User> userMap = loadUsersMap();
        return new ArrayList<>(userMap.values());
    }

    private static Map<String, User> loadUsersMap() throws IOException {
        Map<String, User> userMap = new HashMap<>();
        // Use FileUtils, pass true to create if not found, and default content "{}" for a map
        File file = FileUtils.getDataFileFromProjectRoot(USERS_DATA_FILENAME, true, "{}");

        if (file == null || !file.exists() || !file.canRead()) {
             System.err.println("Cannot read user data file or file does not exist: " + (file != null ? file.getAbsolutePath() : "null path"));
            return userMap; // Return empty map if file fundamentally unusable
        }
        if (file.length() == 0) { // File exists but is empty (e.g. after creation but before writing "{}")
            System.out.println(USERS_DATA_FILENAME + " is empty. Returning empty map.");
            return userMap;
        }


        try (InputStream inputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            userMap = gson.fromJson(reader, USER_MAP_TYPE);
            if (userMap == null) { // If JSON content was "null" or invalid leading to null
                userMap = new HashMap<>();
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Error parsing JSON from Users data file: " + file.getAbsolutePath() + ". Content: " + e.getMessage());
            // Consider what to do: return empty map, throw, or try to recover/backup.
            // For now, returning an empty map if parsing fails after file creation.
            return new HashMap<>(); // Or throw new IOException("Corrupted user data file.", e);
        }
        return userMap;
    }

    private static void saveUsersMap(Map<String, User> userMap) throws IOException {
        // Use FileUtils, pass true to create if not found (though load should have done it), and default content "{}"
        File file = FileUtils.getDataFileFromProjectRoot(USERS_DATA_FILENAME, true, "{}");
        if (file == null) {
            throw new IOException("Could not obtain file path for saving users.");
        }

        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) { // Overwrites existing file
            gson.toJson(userMap, writer);
            System.out.println("Users saved to: " + file.getAbsolutePath());
        }
    }

    // addUser, updateUser, deleteUser methods remain the same as they call loadUsersMap and saveUsersMap
    public static void addUser(User user) throws IOException, IllegalArgumentException {
        if (user == null || user.getUserID() == null || user.getUserID().trim().isEmpty()) {
            throw new IllegalArgumentException("User or User ID cannot be null or empty.");
        }
        Map<String, User> userMap = loadUsersMap();
        if (userMap.containsKey(user.getUserID())) {
            throw new IllegalArgumentException("User with ID " + user.getUserID() + " already exists.");
        }
        if (user.getCreateTime() == null || user.getCreateTime().isEmpty()) {
            user.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        userMap.put(user.getUserID(), user);
        saveUsersMap(userMap);
    }

    public static void updateUser(User user) throws IOException, IllegalArgumentException {
        if (user == null || user.getUserID() == null || user.getUserID().trim().isEmpty()) {
            throw new IllegalArgumentException("User or User ID cannot be null or empty for update.");
        }
        Map<String, User> userMap = loadUsersMap();
        User existingUser = userMap.get(user.getUserID());
        if (existingUser == null) {
            throw new IllegalArgumentException("User with ID " + user.getUserID() + " not found for update.");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        }
        if (user.getCreateTime() == null || user.getCreateTime().isEmpty()){
            user.setCreateTime(existingUser.getCreateTime());
        }
        userMap.put(user.getUserID(), user);
        saveUsersMap(userMap);
    }

    public static void deleteUser(String userId) throws IOException, IllegalArgumentException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty for deletion.");
        }
        Map<String, User> userMap = loadUsersMap();
        if (!userMap.containsKey(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " not found for deletion.");
        }
        userMap.remove(userId);
        saveUsersMap(userMap);
    }
    
    /**
     * Authenticates a user based on username and password.
     * WARNING: This method uses plain text password comparison.
     * In a real application, use password hashing (e.g., bcrypt).
     *
     * @param username The username to check.
     * @param password The plain text password to check.
     * @return The User object if authentication is successful, null otherwise.
     * @throws IOException If there's an error loading user data.
     */
    public static User loginUser(String username, String password) throws IOException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.err.println("Username or password cannot be empty for login.");
            return null;
        }

        Map<String, User> userMap = loadUsersMap();
        if (userMap.isEmpty()) {
            System.err.println("No users found in the system during login attempt for: " + username);
            return null;
        }

        for (User user : userMap.values()) {
            // Case-insensitive username comparison can be more user-friendly
            if (user.getUsername().equalsIgnoreCase(username.trim())) {
                // --- PLAIN TEXT PASSWORD COMPARISON - NOT SECURE ---
                // In a real application:
                // 1. When user is created/password changed: passwordEncoder.encode(plainPassword) -> store hash
                // 2. Here: passwordEncoder.matches(enteredPassword, user.getStoredPasswordHash())
                if (user.getPassword().equals(password)) {
                    System.out.println("User " + username + " logged in successfully.");
                    return user; // Credentials match
                } else {
                    System.out.println("Password incorrect for user: " + username);
                    return null; // Username found, but password incorrect
                }
                // --- END OF INSECURE PASSWORD COMPARISON ---
            }
        }
        System.out.println("Username not found: " + username);
        return null; // Username not found
    }
}

// Remember to include the UserTypeAdapter if you use it.
class UserTypeAdapter extends com.google.gson.TypeAdapter<UserType> {
    @Override
    public void write(com.google.gson.stream.JsonWriter out, UserType value) throws IOException {
        if (value == null) out.nullValue();
        else out.value(value.name()); // Writes enum constant name, e.g., "Sales_Manager"
    }
    @Override
    public UserType read(com.google.gson.stream.JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) { in.nextNull(); return null; }
        String userTypeString = in.nextString();
        try { return UserType.valueOf(userTypeString); } // Default enum parsing
        catch (IllegalArgumentException e) { return UserType.fromString(userTypeString); } // Fallback
    }
}