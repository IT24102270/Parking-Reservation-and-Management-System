package com.sliit.parking_reservation_and_management_system.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminLogger {

    private static AdminLogger instance;
    private static final String LOG_FILE_PATH = "src/main/resources/logs/adminlog.txt";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AdminLogger() {
        ensureLogFileExists();
    }

    // Singleton global access point
    public static synchronized AdminLogger getInstance() {
        if (instance == null) {
            instance = new AdminLogger();
        }
        return instance;
    }

    // Ensure folder and file exist
    private void ensureLogFileExists() {
        try {
            File logFile = new File(LOG_FILE_PATH);
            File parentDir = logFile.getParentFile();
            if (!parentDir.exists()) parentDir.mkdirs();
            if (!logFile.exists()) logFile.createNewFile();
        } catch (IOException e) {
            System.err.println("⚠️ Failed to create admin log file: " + e.getMessage());
        }
    }

    // Thread-safe log method
    public synchronized void log(String message) {
        try (FileWriter writer = new FileWriter(LOG_FILE_PATH, true)) {
            String timestamp = LocalDateTime.now().format(formatter);
            String adminEmail = AdminSessionManager.getInstance().getAdminEmail();
            String line = String.format("[%s] %s - %s%n", timestamp,
                    (adminEmail != null ? "Admin: " + adminEmail : "System"), message);
            writer.write(line);
        } catch (IOException e) {
            System.err.println("⚠️ Failed to write to admin log: " + e.getMessage());
        }
    }
}
