package com.parking.observer.booking;

import com.sliit.parking_reservation_and_management_system.entity.Reservation;

/**
 * Subject interface for the Observer Design Pattern
 * 
 * This interface defines the contract for subjects (observable objects) that
 * maintain a list of observers and notify them when state changes occur.
 * 
 * In the Observer Pattern:
 * - Subject maintains a list of observers
 * - When an event occurs, Subject notifies all registered observers
 * - Observers can be added or removed dynamically at runtime
 */
public interface Subject {
    
    /**
     * Adds an observer to the list of observers
     * The observer will receive notifications for all future booking events
     * 
     * @param observer The observer to add
     */
    void addObserver(Observer observer);
    
    /**
     * Removes an observer from the list of observers
     * The observer will no longer receive notifications
     * 
     * @param observer The observer to remove
     */
    void removeObserver(Observer observer);
    
    /**
     * Notifies all registered observers about a booking event
     * This method is called when a reservation state change occurs
     * 
     * @param reservation The reservation that triggered the event
     * @param event The type of event (e.g., "CREATED", "CANCELLED", "UPDATED")
     */
    void notifyObservers(Reservation reservation, String event);
}
