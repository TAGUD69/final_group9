package com.busreservation.utils;

import com.busreservation.models.User;

public class SessionManager {
    private static User currentUser;
    
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public static boolean isAdmin() {
        return currentUser != null && "admin".equals(currentUser.getRole());
    }
    
    public static void clearSession() {
        currentUser = null;
    }
}