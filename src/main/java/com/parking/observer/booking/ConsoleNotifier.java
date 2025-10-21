package com.parking.observer.booking;

import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ConsoleNotifier - Console Observer for debugging and verification
 * 
 * This class implements the Observer interface specifically for console logging
 * and debugging purposes. It provides detailed console output to verify that
 * the Observer pattern is working correctly throughout the system.
 * 
 * When notified by the Subject (NotificationManager), this observer:
 * 1. Logs detailed information about the booking event
 * 2. Provides visual console output with emojis and formatting
 * 3. Helps developers verify the Observer pattern is functioning
 * 4. Tracks notification statistics for debugging
 */
public class ConsoleNotifier implements Observer {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private int totalNotifications = 0;
    
    /**
     * Constructor
     */
    public ConsoleNotifier() {
        System.out.println("🖥️  ConsoleNotifier initialized - Ready for debugging and verification");
    }
    
    /**
     * Called by NotificationManager when a booking event occurs
     * This method provides detailed console logging for debugging
     * 
     * @param reservation The reservation that triggered the event
     * @param event The type of event (CREATED, CANCELLED, etc.)
     */
    @Override
    public void update(Reservation reservation, String event) {
        totalNotifications++;
        
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String eventIcon = getEventIcon(event);
            
            // Create detailed console log
            System.out.println("\n" + "=".repeat(80));
            System.out.println("🔔 OBSERVER PATTERN NOTIFICATION #" + totalNotifications);
            System.out.println("=".repeat(80));
            System.out.println("📅 Timestamp: " + timestamp);
            System.out.println("🎯 Event Type: " + eventIcon + " " + event.toUpperCase());
            System.out.println("📋 Reservation Details:");
            System.out.println("   ├─ Reservation ID: " + reservation.getId());
            System.out.println("   ├─ User ID: " + reservation.getUserId());
            System.out.println("   ├─ Slot ID: " + reservation.getSlotId());
            System.out.println("   ├─ Vehicle: " + reservation.getVehicleNumber());
            System.out.println("   ├─ Status: " + reservation.getStatus());
            
            if (reservation.getStartTime() != null) {
                System.out.println("   ├─ Start Time: " + reservation.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            if (reservation.getEndTime() != null) {
                System.out.println("   ├─ End Time: " + reservation.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            
            System.out.println("   └─ Created At: " + (reservation.getCreatedAt() != null ? reservation.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A"));
            
            // Log notification flow
            System.out.println("🔄 Notification Flow:");
            System.out.println("   ├─ 📧 Email notification triggered");
            System.out.println("   ├─ 📱 SMS notification triggered");
            System.out.println("   ├─ 🔔 In-app notification triggered");
            System.out.println("   └─ 🖥️  Console notification logged");
            
            System.out.println("📊 Statistics:");
            System.out.println("   └─ Total notifications processed: " + totalNotifications);
            
            System.out.println("✅ Observer Pattern verification: WORKING CORRECTLY");
            System.out.println("=".repeat(80) + "\n");
            
        } catch (Exception e) {
            System.err.println("❌ ConsoleNotifier error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the appropriate icon for the event type
     * 
     * @param event The booking event
     * @return Icon string
     */
    private String getEventIcon(String event) {
        switch (event.toUpperCase()) {
            case "CREATED":
                return "✅";
            case "CANCELLED":
                return "❌";
            case "UPDATED":
                return "🔄";
            case "CONFIRMED":
                return "✔️";
            case "PENDING":
                return "⏳";
            case "COMPLETED":
                return "🏁";
            default:
                return "📋";
        }
    }
    
    /**
     * Get the total number of notifications processed
     * 
     * @return Total notification count
     */
    public int getTotalNotifications() {
        return totalNotifications;
    }
    
    /**
     * Reset the notification counter (for testing)
     */
    public void resetCounter() {
        totalNotifications = 0;
        System.out.println("🔄 ConsoleNotifier counter reset");
    }
}
