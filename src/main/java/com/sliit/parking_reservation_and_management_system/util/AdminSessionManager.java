package com.sliit.parking_reservation_and_management_system.util;

public class AdminSessionManager {

    private static AdminSessionManager instance;

    private String adminEmail;
    private String role;

    // Private constructor (no direct instantiation)
    private AdminSessionManager() {}

    // Global access point (Singleton)
    public static synchronized AdminSessionManager getInstance() {
        if (instance == null) {
            instance = new AdminSessionManager();
        }
        return instance;
    }

    // Store current admin info
    public void login(String email, String role) {
        this.adminEmail = email;
        this.role = role;
    }

    // Clear session when admin logs out
    public void logout() {
        this.adminEmail = null;
        this.role = null;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public String getRole() {
        return role;
    }

    public boolean isLoggedIn() {
        return adminEmail != null;
    }
}
