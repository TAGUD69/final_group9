package com.busreservation.models;

public class User {
    private int userId;
    private String username;
    private String fullName;
    private String role;
    private String createdAt;
    
    public User(int userId, String username, String fullName, String role) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }
    
    public User(int userId, String username, String fullName, String role, String createdAt) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
    }
    
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getCreatedAt() { return createdAt; }
}