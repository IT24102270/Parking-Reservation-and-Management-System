package com.sliit.parking_reservation_and_management_system.config;

import com.parking.observer.booking.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring Configuration for Observer Pattern Integration
 * 
 * This configuration sets up the Observer pattern components as Spring beans,
 * allowing them to be injected into controllers and services throughout the application.
 * 
 * The Observer pattern is used for:
 * - Booking notifications (email, SMS, in-app)
 * - Admin dashboard notifications
 * - Real-time updates across the system
 */
@Configuration
public class ObserverPatternConfig {
    
    /**
     * Creates the central NotificationManager bean
     * This is the Subject in the Observer pattern that manages all observers
     * 
     * @return NotificationManager instance
     */
    @Bean
    @Primary
    public NotificationManager notificationManager() {
        NotificationManager manager = new NotificationManager();
        
        // Register all notification observers
        manager.addObserver(emailNotifier());
        manager.addObserver(smsNotifier());
        manager.addObserver(inAppNotifier());
        manager.addObserver(consoleNotifier());
        
        System.out.println("🚀 NotificationManager configured with " + manager.getObserverCount() + " observers");
        System.out.println("📋 Active observers: " + manager.getObserverNames());
        
        return manager;
    }
    
    /**
     * Creates EmailNotifier bean for email notifications
     * 
     * @return EmailNotifier instance
     */
    @Bean
    public EmailNotifier emailNotifier() {
        return new EmailNotifier();
    }
    
    /**
     * Creates SMSNotifier bean for SMS notifications
     * 
     * @return SMSNotifier instance
     */
    @Bean
    public SMSNotifier smsNotifier() {
        return new SMSNotifier();
    }
    
    /**
     * Creates InAppNotifier bean for in-app notifications
     * 
     * @return InAppNotifier instance
     */
    @Bean
    public InAppNotifier inAppNotifier() {
        return new InAppNotifier();
    }
    
    /**
     * Creates ConsoleNotifier bean for console logging and debugging
     * This observer helps verify that the Observer pattern is working correctly
     * 
     * @return ConsoleNotifier instance
     */
    @Bean
    public ConsoleNotifier consoleNotifier() {
        return new ConsoleNotifier();
    }
}
