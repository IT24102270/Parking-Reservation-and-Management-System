package com.parking.observer.booking;

import com.sliit.parking_reservation_and_management_system.entity.Reservation;

/**
 * Observer interface for the Observer Design Pattern
 * 
 * This interface defines the contract for all concrete observers that want to be
 * notified when booking events occur (reservation created, cancelled, etc.)
 * 
 * The Observer Pattern allows multiple notification channels (Email, SMS, In-App)
 * to automatically receive updates when booking state changes occur.
 */
public interface Observer {
    
    /**
     * Called by the Subject when a booking event occurs
     * 
     * @param reservation The reservation that triggered the event
     * @param event The type of event that occurred (e.g., "CREATED", "CANCELLED", "UPDATED")
     */
    void update(Reservation reservation, String event);
}
