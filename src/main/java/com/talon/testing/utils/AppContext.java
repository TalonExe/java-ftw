package com.talon.testing.utils; // Or your preferred utility package

import com.talon.testing.models.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppContext {
    private static User currentUser;
    private static LocalDateTime loginTime;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
        if (user != null) {
            loginTime = LocalDateTime.now();
        } else {
            loginTime = null;
        }
    }

    public static String getFormattedLoginTime() {
        return loginTime != null ? loginTime.format(TIME_FORMATTER) : "N/A";
    }

    public static void clearCurrentUser() {
        currentUser = null;
        loginTime = null;
    }
}