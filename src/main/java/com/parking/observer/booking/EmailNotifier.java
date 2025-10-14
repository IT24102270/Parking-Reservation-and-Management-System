package com.parking.observer.booking;

import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import java.time.format.DateTimeFormatter;

/**
 * EmailNotifier - Concrete Observer for email notifications
 * 
 * This class implements the Observer interface to handle email notifications
 * when booking events occur. It demonstrates how the Observer Pattern allows
 * different notification channels to operate independently.
 * 
 * When notified by the Subject (NotificationManager), this observer:
 * 1. Formats the reservation details for email
 * 2. Simulates sending an email notification
 * 3. Logs the email notification activity
 */
public class EmailNotifier implements Observer {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * Constructor
     */
    public EmailNotifier() {
        System.out.println("EmailNotifier initialized - Ready to send email notifications");
    }
    
    /**
     * Called by NotificationManager when a booking event occurs
     * This method handles the email notification logic
     * 
     * @param reservation The reservation that triggered the event
     * @param event The type of event (CREATED, CANCELLED, etc.)
     */
    @Override
    public void update(Reservation reservation, String event) {
        try {
            // Format email content based on event type
            String emailSubject = formatEmailSubject(event, reservation);
            String emailBody = formatEmailBody(event, reservation);
            
            // Simulate email sending process
            System.out.println("📧 EMAIL NOTIFICATION:");
            System.out.println("   To: customer@example.com");
            System.out.println("   Subject: " + emailSubject);
            System.out.println("   Body: " + emailBody);
            
            // Simulate email delivery delay
            Thread.sleep(50);
            
            // Log successful email notification
            System.out.println("✅ Email sent for Reservation ID " + reservation.getId() + 
                             " – Booking " + event);
            
        } catch (Exception e) {
            System.err.println("❌ EmailNotifier error: Failed to send email for reservation " + 
                             reservation.getId() + " - " + e.getMessage());
        }
    }
    
    /**
     * Formats the email subject line based on the event type
     * 
     * @param event The booking event type
     * @param reservation The reservation details
     * @return Formatted email subject
     */
    private String formatEmailSubject(String event, Reservation reservation) {
        switch (event.toUpperCase()) {
            case "CREATED":
                return "Booking Confirmation - Reservation " + reservation.getBookingId();
            case "CANCELLED":
                return "Booking Cancellation - Reservation " + reservation.getBookingId();
            case "UPDATED":
                return "Booking Updated - Reservation " + reservation.getBookingId();
            default:
                return "Booking Notification - Reservation " + reservation.getBookingId();
        }
    }
    
    /**
     * Formats the email body content based on the event type
     * 
     * @param event The booking event type
     * @param reservation The reservation details
     * @return Formatted email body
     */
    private String formatEmailBody(String event, Reservation reservation) {
        StringBuilder body = new StringBuilder();
        
        switch (event.toUpperCase()) {
            case "CREATED":
                body.append("Your parking reservation has been successfully created!");
                break;
            case "CANCELLED":
                body.append("Your parking reservation has been cancelled.");
                break;
            case "UPDATED":
                body.append("Your parking reservation has been updated.");
                break;
            default:
                body.append("Your parking reservation status has changed.");
        }
        
        body.append("\n\nReservation Details:");
        body.append("\n- Booking ID: ").append(reservation.getBookingId());
        body.append("\n- Slot ID: ").append(reservation.getSlotId());
        body.append("\n- Vehicle: ").append(reservation.getVehicleNumber());
        
        if (reservation.getStartTime() != null) {
            body.append("\n- Start Time: ").append(reservation.getStartTime().format(DATE_FORMATTER));
        }
        if (reservation.getEndTime() != null) {
            body.append("\n- End Time: ").append(reservation.getEndTime().format(DATE_FORMATTER));
        }
        body.append("\n- Status: ").append(reservation.getStatus());
        
        body.append("\n\nThank you for using our parking service!");
        
        return body.toString();
    }
}
