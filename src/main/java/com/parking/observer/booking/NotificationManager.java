package com.parking.observer.booking;

import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationManager - Concrete Subject implementation for Observer Pattern
 * 
 * This class acts as the central notification hub that:
 * 1. Maintains a list of observers (notification channels)
 * 2. Provides methods to add/remove observers dynamically
 * 3. Notifies all registered observers when booking events occur
 * 
 * The Observer Pattern decouples the booking logic from notification logic,
 * allowing multiple notification channels to operate independently.
 */
public class NotificationManager implements Subject {
    
    /**
     * List of observers that will be notified when booking events occur
     * This list can be modified at runtime to add/remove notification channels
     */
    private final List<Observer> observers;
    
    /**
     * Constructor initializes the observer list
     */
    public NotificationManager() {
        this.observers = new ArrayList<>();
        System.out.println("NotificationManager initialized - Ready to manage booking notifications");
    }
    
    /**
     * Adds a new observer to receive booking notifications
     * 
     * @param observer The notification channel to add (e.g., EmailNotifier, SMSNotifier)
     */
    @Override
    public void addObserver(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            System.out.println("Observer added: " + observer.getClass().getSimpleName() + 
                             " (Total observers: " + observers.size() + ")");
        }
    }
    
    /**
     * Removes an observer from receiving booking notifications
     * 
     * @param observer The notification channel to remove
     */
    @Override
    public void removeObserver(Observer observer) {
        if (observers.remove(observer)) {
            System.out.println("Observer removed: " + observer.getClass().getSimpleName() + 
                             " (Total observers: " + observers.size() + ")");
        }
    }
    
    /**
     * Notifies all registered observers about a booking event
     * This is the core of the Observer Pattern - when called, it triggers
     * the update() method on all registered observers
     * 
     * @param reservation The reservation that triggered the event
     * @param event The type of event (CREATED, CANCELLED, UPDATED, etc.)
     */
    @Override
    public void notifyObservers(Reservation reservation, String event) {
        if (reservation == null || event == null) {
            System.err.println("NotificationManager: Cannot notify observers - reservation or event is null");
            return;
        }
        
        System.out.println("=== NotificationManager: Broadcasting booking event ===");
        System.out.println("Event: " + event);
        System.out.println("Reservation ID: " + reservation.getId());
        System.out.println("Booking ID: " + reservation.getBookingId());
        System.out.println("Notifying " + observers.size() + " observers...");
        
        // Notify each observer about the booking event
        for (Observer observer : observers) {
            try {
                observer.update(reservation, event);
            } catch (Exception e) {
                System.err.println("Error notifying observer " + observer.getClass().getSimpleName() + 
                                 ": " + e.getMessage());
            }
        }
        
        System.out.println("=== Notification broadcast complete ===\n");
    }
    
    /**
     * Get the current number of registered observers
     * 
     * @return Number of active observers
     */
    public int getObserverCount() {
        return observers.size();
    }
    
    /**
     * Get list of observer class names for debugging
     * 
     * @return List of observer class names
     */
    public List<String> getObserverNames() {
        List<String> names = new ArrayList<>();
        for (Observer observer : observers) {
            names.add(observer.getClass().getSimpleName());
        }
        return names;
    }
}
