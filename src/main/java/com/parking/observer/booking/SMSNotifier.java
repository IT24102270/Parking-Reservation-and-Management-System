package com.parking.observer.booking;

import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import java.time.format.DateTimeFormatter;

/**
 * SMSNotifier - Concrete Observer for SMS notifications
 * 
 * This class implements the Observer interface to handle SMS notifications
 * when booking events occur. It demonstrates the flexibility of the Observer Pattern
 * where different notification channels can have their own implementation logic.
 * 
 * When notified by the Subject (NotificationManager), this observer:
 * 1. Formats a concise SMS message (due to character limits)
 * 2. Simulates sending an SMS notification
 * 3. Logs the SMS notification activity
 */
public class SMSNotifier implements Observer {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd HH:mm");
    private static final int SMS_MAX_LENGTH = 160; // Standard SMS character limit
    
    /**
     * Constructor
     */
    public SMSNotifier() {
        System.out.println("SMSNotifier initialized - Ready to send SMS notifications");
    }
    
    /**
     * Called by NotificationManager when a booking event occurs
     * This method handles the SMS notification logic
     * 
     * @param reservation The reservation that triggered the event
     * @param event The type of event (CREATED, CANCELLED, etc.)
     */
    @Override
    public void update(Reservation reservation, String event) {
        try {
            // Format SMS message (keep it concise due to character limits)
            String smsMessage = formatSMSMessage(event, reservation);
            
            // Simulate SMS sending process
            System.out.println("📱 SMS NOTIFICATION:");
            System.out.println("   To: +1-555-0123");
            System.out.println("   Message: " + smsMessage);
            System.out.println("   Length: " + smsMessage.length() + "/" + SMS_MAX_LENGTH + " chars");
            
            // Simulate SMS delivery delay
            Thread.sleep(30);
            
            // Log successful SMS notification
            System.out.println("✅ SMS sent for Reservation ID " + reservation.getId() + 
                             " – Booking " + event);
            
        } catch (Exception e) {
            System.err.println("❌ SMSNotifier error: Failed to send SMS for reservation " + 
                             reservation.getId() + " - " + e.getMessage());
        }
    }
    
    /**
     * Formats a concise SMS message based on the event type
     * SMS messages need to be brief due to character limitations
     * 
     * @param event The booking event type
     * @param reservation The reservation details
     * @return Formatted SMS message
     */
    private String formatSMSMessage(String event, Reservation reservation) {
        StringBuilder message = new StringBuilder();
        
        // Add event-specific message
        switch (event.toUpperCase()) {
            case "CREATED":
                message.append("✅ Parking BOOKED! ");
                break;
            case "CANCELLED":
                message.append("❌ Parking CANCELLED! ");
                break;
            case "UPDATED":
                message.append("🔄 Parking UPDATED! ");
                break;
            default:
                message.append("📋 Parking STATUS: ");
        }
        
        // Add essential reservation details
        message.append("ID: ").append(reservation.getBookingId());
        
        if (reservation.getSlotId() != null) {
            message.append(", Slot: ").append(reservation.getSlotId());
        }
        
        if (reservation.getStartTime() != null) {
            message.append(", Start: ").append(reservation.getStartTime().format(TIME_FORMATTER));
        }
        
        if (reservation.getVehicleNumber() != null) {
            message.append(", Vehicle: ").append(reservation.getVehicleNumber());
        }
        
        // Add status for context
        if (reservation.getStatus() != null) {
            message.append(", Status: ").append(reservation.getStatus());
        }
        
        // Ensure message doesn't exceed SMS character limit
        String finalMessage = message.toString();
        if (finalMessage.length() > SMS_MAX_LENGTH) {
            finalMessage = finalMessage.substring(0, SMS_MAX_LENGTH - 3) + "...";
        }
        
        return finalMessage;
    }
    
    /**
     * Validates phone number format (for demonstration)
     * 
     * @param phoneNumber The phone number to validate
     * @return true if valid format
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Simple validation for demonstration
        return phoneNumber != null && phoneNumber.matches("\\+?[1-9]\\d{1,14}");
    }
}
