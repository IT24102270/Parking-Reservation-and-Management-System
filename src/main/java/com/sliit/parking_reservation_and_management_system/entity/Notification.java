package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "Notification")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Long notificationId;
    
    @Column(name = "CustomerID", nullable = false)
    private Long customerId;
    
    @Column(name = "Type", nullable = false, length = 50)
    private String type;
    
    @Column(name = "Message", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String message;
    
    @Column(name = "DateSent", nullable = false)
    private LocalDateTime dateSent;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.dateSent = LocalDateTime.now();
    }
    
    public Notification(Long customerId, String type, String message) {
        this();
        this.customerId = customerId;
        this.type = type;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getDateSent() {
        return dateSent;
    }
    
    public void setDateSent(LocalDateTime dateSent) {
        this.dateSent = dateSent;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility methods for compatibility with existing code
    public Long getUserId() {
        return this.customerId;
    }
    
    public void setUserId(Long userId) {
        this.customerId = userId;
    }
    
    public String getTitle() {
        // Extract title from message (first 50 characters)
        if (message != null && message.length() > 50) {
            return message.substring(0, 50) + "...";
        }
        return message;
    }
    
    public boolean isRead() {
        return false; // Since read status is not in the database schema
    }
    
    public boolean isUnread() {
        return true; // Since read status is not in the database schema
    }
    
    public boolean isHighPriority() {
        return "URGENT".equalsIgnoreCase(type) || "ERROR".equalsIgnoreCase(type);
    }
    
    public String getTimeAgo() {
        if (dateSent == null) return "Unknown";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateSent, now);
        long hours = ChronoUnit.HOURS.between(dateSent, now);
        long days = ChronoUnit.DAYS.between(dateSent, now);
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        }
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", customerId=" + customerId +
                ", type='" + type + '\'' +
                ", dateSent=" + dateSent +
                ", createdAt=" + createdAt +
                '}';
    }
}
