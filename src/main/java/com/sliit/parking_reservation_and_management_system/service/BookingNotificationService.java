package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.parking.observer.booking.NotificationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * BookingNotificationService - Integration service for Observer pattern
 * 
 * This service acts as a bridge between your booking operations and the Observer pattern.
 * It provides methods to trigger notifications when booking events occur throughout the system.
 * 
 * Usage Examples:
 * - When a customer creates a booking: bookingNotificationService.notifyBookingCreated(reservation)
 * - When an admin updates a booking: bookingNotificationService.notifyBookingUpdated(reservation)
 * - When a booking is cancelled: bookingNotificationService.notifyBookingCancelled(reservation)
 */
@Service
public class BookingNotificationService {
    
    @Autowired
    private NotificationManager notificationManager;
    
    /**
     * Notify all observers when a new booking is created
     * 
     * @param reservation The newly created reservation
     */
    public void notifyBookingCreated(Reservation reservation) {
        System.out.println("🎯 BookingNotificationService: Triggering CREATED notifications for reservation " + reservation.getId());
        notificationManager.notifyObservers(reservation, "CREATED");
    }
    
    /**
     * Notify all observers when a booking is updated
     * 
     * @param reservation The updated reservation
     */
    public void notifyBookingUpdated(Reservation reservation) {
        System.out.println("🎯 BookingNotificationService: Triggering UPDATED notifications for reservation " + reservation.getId());
        notificationManager.notifyObservers(reservation, "UPDATED");
    }
    
    /**
     * Notify all observers when a booking is cancelled
     * 
     * @param reservation The cancelled reservation
     */
    public void notifyBookingCancelled(Reservation reservation) {
        System.out.println("🎯 BookingNotificationService: Triggering CANCELLED notifications for reservation " + reservation.getId());
        notificationManager.notifyObservers(reservation, "CANCELLED");
    }
    
    /**
     * Notify all observers when a booking is confirmed
     * 
     * @param reservation The confirmed reservation
     */
    public void notifyBookingConfirmed(Reservation reservation) {
        System.out.println("🎯 BookingNotificationService: Triggering CONFIRMED notifications for reservation " + reservation.getId());
        notificationManager.notifyObservers(reservation, "CONFIRMED");
    }
    
    /**
     * Notify all observers when a booking is completed
     * 
     * @param reservation The completed reservation
     */
    public void notifyBookingCompleted(Reservation reservation) {
        System.out.println("🎯 BookingNotificationService: Triggering COMPLETED notifications for reservation " + reservation.getId());
        notificationManager.notifyObservers(reservation, "COMPLETED");
    }
    
    /**
     * Generic method to notify observers of any booking event
     * 
     * @param reservation The reservation
     * @param eventType The type of event (CREATED, UPDATED, CANCELLED, etc.)
     */
    public void notifyBookingEvent(Reservation reservation, String eventType) {
        System.out.println("🎯 BookingNotificationService: Triggering " + eventType + " notifications for reservation " + reservation.getId());
        notificationManager.notifyObservers(reservation, eventType);
    }
    
    /**
     * Get notification statistics
     * 
     * @return String with observer statistics
     */
    public String getNotificationStats() {
        return "Active Observers: " + notificationManager.getObserverCount() + 
               " | Observer Types: " + notificationManager.getObserverNames();
    }
    
    /**
     * Check if notification system is properly initialized
     * 
     * @return true if notification manager has observers
     */
    public boolean isNotificationSystemReady() {
        return notificationManager != null && notificationManager.getObserverCount() > 0;
    }
}
