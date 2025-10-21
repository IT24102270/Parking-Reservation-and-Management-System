package com.sliit.parking_reservation_and_management_system.logging;

import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton pattern implementation for logging admin actions
 * Thread-safe singleton with lazy initialization
 */
@Component
public class AdminActionLogger {
    
    // Singleton instance
    private static volatile AdminActionLogger instance;
    
    // Thread safety lock
    private static final ReentrantLock lock = new ReentrantLock();
    
    // Log file path
    private static final String LOG_FILE_PATH = "src/main/resources/admin-actions.log";
    
    // Date formatter for timestamps
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Private constructor to prevent instantiation
    private AdminActionLogger() {
        // Initialize log file if it doesn't exist
        initializeLogFile();
    }
    
    /**
     * Get singleton instance with double-checked locking
     * @return AdminActionLogger instance
     */
    public static AdminActionLogger getInstance() {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new AdminActionLogger();
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }
    
    /**
     * Initialize log file with header
     */
    public void initializeLogFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_PATH, true))) {
            writer.println("=".repeat(80));
            writer.println("ADMIN ACTION LOG - " + LocalDateTime.now().format(DATE_FORMATTER));
            writer.println("=".repeat(80));
            writer.println();
        } catch (IOException e) {
            System.err.println("Failed to initialize admin action log file: " + e.getMessage());
        }
    }
    
    /**
     * Clear log file and initialize with new header
     */
    public void clearLogFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_PATH, false))) {
            writer.println("=".repeat(80));
            writer.println("ADMIN ACTION LOG - " + LocalDateTime.now().format(DATE_FORMATTER));
            writer.println("=".repeat(80));
            writer.println();
        } catch (IOException e) {
            System.err.println("Failed to clear admin action log file: " + e.getMessage());
        }
    }
    
    /**
     * Log admin action with timestamp
     * @param adminEmail Admin's email
     * @param action Action performed
     * @param details Additional details
     * @param targetEntity Target entity (e.g., "User", "Reservation")
     * @param targetId Target entity ID
     */
    public void logAction(String adminEmail, String action, String details, String targetEntity, String targetId) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format(
            "[%s] ADMIN: %s | ACTION: %s | TARGET: %s (ID: %s) | DETAILS: %s",
            timestamp, adminEmail, action, targetEntity, targetId, details
        );
        
        writeToFile(logEntry);
    }
    
    /**
     * Log admin action without target entity
     * @param adminEmail Admin's email
     * @param action Action performed
     * @param details Additional details
     */
    public void logAction(String adminEmail, String action, String details) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format(
            "[%s] ADMIN: %s | ACTION: %s | DETAILS: %s",
            timestamp, adminEmail, action, details
        );
        
        writeToFile(logEntry);
    }
    
    /**
     * Log admin login
     * @param adminEmail Admin's email
     * @param ipAddress IP address (if available)
     */
    public void logLogin(String adminEmail, String ipAddress) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format(
            "[%s] ADMIN: %s | ACTION: LOGIN | IP: %s | DETAILS: Admin logged in successfully",
            timestamp, adminEmail, ipAddress != null ? ipAddress : "Unknown"
        );
        
        writeToFile(logEntry);
    }
    
    /**
     * Log admin logout
     * @param adminEmail Admin's email
     */
    public void logLogout(String adminEmail) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format(
            "[%s] ADMIN: %s | ACTION: LOGOUT | DETAILS: Admin logged out",
            timestamp, adminEmail
        );
        
        writeToFile(logEntry);
    }
    
    /**
     * Log dashboard access
     * @param adminEmail Admin's email
     * @param dashboardName Name of the dashboard accessed
     */
    public void logDashboardAccess(String adminEmail, String dashboardName) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format(
            "[%s] ADMIN: %s | ACTION: DASHBOARD_ACCESS | TARGET: %s | DETAILS: Accessed dashboard",
            timestamp, adminEmail, dashboardName
        );
        
        writeToFile(logEntry);
    }
    
    /**
     * Write log entry to file (thread-safe)
     * @param logEntry Log entry to write
     */
    private void writeToFile(String logEntry) {
        lock.lock();
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_PATH, true))) {
            writer.println(logEntry);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to write to admin action log: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Get current log file path
     * @return Log file path
     */
    public String getLogFilePath() {
        return LOG_FILE_PATH;
    }
    
    /**
     * Create a new log session (adds separator)
     */
    public void startNewSession() {
        String separator = String.format(
            "\n%s - NEW ADMIN SESSION STARTED %s\n",
            "=".repeat(30), "=".repeat(30)
        );
        
        writeToFile(separator);
    }
}
