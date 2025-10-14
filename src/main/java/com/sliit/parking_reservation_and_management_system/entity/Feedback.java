package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Feedback")
public class Feedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FeedbackID")
    private Long feedbackID;
    
    @Column(name = "CustomerID", nullable = false)
    private Long customerID;
    
    @Column(name = "Rating", nullable = false)
    private Integer rating;
    
    @Column(name = "Comment", columnDefinition = "nvarchar(max)")
    private String comment;
    
    @Column(name = "Date", nullable = false)
    private LocalDateTime date;
    
    // Constructors
    public Feedback() {
        this.date = LocalDateTime.now();
    }
    
    public Feedback(Long customerID, Integer rating, String comment) {
        this();
        this.customerID = customerID;
        this.rating = rating;
        this.comment = comment;
    }
    
    // Getters and Setters
    public Long getFeedbackID() {
        return feedbackID;
    }
    
    public void setFeedbackID(Long feedbackID) {
        this.feedbackID = feedbackID;
    }
    
    public Long getCustomerID() {
        return customerID;
    }
    
    public void setCustomerID(Long customerID) {
        this.customerID = customerID;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    @PrePersist
    public void prePersist() {
        if (this.date == null) {
            this.date = LocalDateTime.now();
        }
    }
    
    @Override
    public String toString() {
        return "Feedback{" +
                "feedbackID=" + feedbackID +
                ", customerID=" + customerID +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", date=" + date +
                '}';
    }
}
