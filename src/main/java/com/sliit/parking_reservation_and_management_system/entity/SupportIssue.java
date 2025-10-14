package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SupportIssue")
public class SupportIssue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IssueID")
    private Long issueId;
    
    @Column(name = "CustomerID", nullable = false)
    private Long customerId;
    
    @Column(name = "Status", nullable = false, length = 20)
    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    
    @Column(name = "RaisedDate", nullable = false)
    private LocalDateTime raisedDate;
    
    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public SupportIssue() {
        this.raisedDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public SupportIssue(Long customerId, String description) {
        this();
        this.customerId = customerId;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getIssueId() {
        return issueId;
    }
    
    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getRaisedDate() {
        return raisedDate;
    }
    
    public void setRaisedDate(LocalDateTime raisedDate) {
        this.raisedDate = raisedDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper method to generate support ticket ID
    public String getTicketId() {
        if (issueId != null && raisedDate != null) {
            return String.format("SUP-%s-%d", 
                raisedDate.toLocalDate().toString().replace("-", ""), 
                issueId);
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "SupportIssue{" +
                "issueId=" + issueId +
                ", customerId=" + customerId +
                ", status='" + status + '\'' +
                ", raisedDate=" + raisedDate +
                ", description='" + description + '\'' +
                '}';
    }
}
