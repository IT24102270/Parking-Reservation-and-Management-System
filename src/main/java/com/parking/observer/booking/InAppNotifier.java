package com.parking.observer.booking;

import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * InAppNotifier - Concrete Observer for in-app notifications
 * 
 * This class implements the Observer interface to handle in-app notifications
 * when booking events occur. It demonstrates how the Observer Pattern enables
 * multiple notification channels to coexist and operate independently.
 * 
 * When notified by the Subject (NotificationManager), this observer:
 * 1. Creates rich in-app notification content
 * 2. Simulates displaying notifications in the application UI
 * 3. Logs the in-app notification activity with visual indicators
 */
public class InAppNotifier implements Observer {
    
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
    private int notificationCounter = 0;
    
    /**
     * Constructor
     */
    public InAppNotifier() {
        System.out.println("InAppNotifier initialized - Ready to display in-app notifications");
    }
    
    /**
     * Called by NotificationManager when a booking event occurs
     * This method handles the in-app notification logic
     * 
     * @param reservation The reservation that triggered the event
     * @param event The type of event (CREATED, CANCELLED, etc.)
     */
    @Override
    public void update(Reservation reservation, String event) {
        try {
            notificationCounter++;
            
            // Create in-app notification content
            InAppNotification notification = createNotification(event, reservation);
            
            // Simulate displaying in-app notification
            displayNotification(notification);
            
            // Simulate brief display time
            Thread.sleep(20);
            
            // Log successful in-app notification
            System.out.println("✅ In-app alert displayed for Reservation ID " + reservation.getId() + 
                             " – Booking " + event);
            
        } catch (Exception e) {
            System.err.println("❌ InAppNotifier error: Failed to display notification for reservation " + 
                             reservation.getId() + " - " + e.getMessage());
        }
    }
    
    /**
     * Creates an in-app notification object with rich content
     * 
     * @param event The booking event type
     * @param reservation The reservation details
     * @return InAppNotification object
     */
    private InAppNotification createNotification(String event, Reservation reservation) {
        InAppNotification notification = new InAppNotification();
        notification.id = "NOTIF_" + notificationCounter;
        notification.timestamp = LocalDateTime.now();
        notification.priority = getPriorityLevel(event);
        notification.icon = getEventIcon(event);
        notification.title = getNotificationTitle(event, reservation);
        notification.message = getNotificationMessage(event, reservation);
        notification.actionUrl = "/customer/booking/" + reservation.getId() + "/view";
        
        return notification;
    }
    
    /**
     * Simulates displaying the notification in the application UI
     * 
     * @param notification The notification to display
     */
    private void displayNotification(InAppNotification notification) {
        System.out.println("🔔 IN-APP NOTIFICATION:");
        System.out.println("   ┌─────────────────────────────────────────────┐");
        System.out.println("   │ " + notification.icon + " " + notification.title + " [" + notification.id + "]");
        System.out.println("   │ " + notification.message);
        System.out.println("   │ Priority: " + notification.priority + " | Time: " + 
                         notification.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        System.out.println("   │ Action: View Details → " + notification.actionUrl);
        System.out.println("   └─────────────────────────────────────────────┘");
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
            default:
                return "📋";
        }
    }
    
    /**
     * Gets the priority level for the event
     * 
     * @param event The booking event
     * @return Priority level
     */
    private String getPriorityLevel(String event) {
        switch (event.toUpperCase()) {
            case "CREATED":
                return "HIGH";
            case "CANCELLED":
                return "MEDIUM";
            case "UPDATED":
                return "MEDIUM";
            default:
                return "LOW";
        }
    }
    
    /**
     * Gets the notification title based on event type
     * 
     * @param event The booking event
     * @param reservation The reservation details
     * @return Notification title
     */
    private String getNotificationTitle(String event, Reservation reservation) {
        switch (event.toUpperCase()) {
            case "CREATED":
                return "Booking Confirmed!";
            case "CANCELLED":
                return "Booking Cancelled";
            case "UPDATED":
                return "Booking Updated";
            default:
                return "Booking Notification";
        }
    }
    
    /**
     * Gets the notification message with reservation details
     * 
     * @param event The booking event
     * @param reservation The reservation details
     * @return Notification message
     */
    private String getNotificationMessage(String event, Reservation reservation) {
        StringBuilder message = new StringBuilder();
        
        switch (event.toUpperCase()) {
            case "CREATED":
                message.append("Your parking reservation ").append(reservation.getBookingId())
                       .append(" has been confirmed");
                break;
            case "CANCELLED":
                message.append("Reservation ").append(reservation.getBookingId())
                       .append(" has been cancelled");
                break;
            case "UPDATED":
                message.append("Reservation ").append(reservation.getBookingId())
                       .append(" has been updated");
                break;
            default:
                message.append("Status change for reservation ").append(reservation.getBookingId());
        }
        
        if (reservation.getStartTime() != null) {
            message.append(" for ").append(reservation.getStartTime().format(DISPLAY_FORMATTER));
        }
        
        return message.toString();
    }
    
    /**
     * Inner class representing an in-app notification
     */
    private static class InAppNotification {
        String id;
        LocalDateTime timestamp;
        String priority;
        String icon;
        String title;
        String message;
        String actionUrl;
    }
    
    /**
     * Get the total number of notifications displayed
     * 
     * @return Notification count
     */
    public int getNotificationCount() {
        return notificationCounter;
    }
}
