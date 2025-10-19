package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SuspiciousReport")
public class SuspiciousReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SuspiciousID")
    private Long suspiciousID;

    @Column(name = "SecurityOfficerID", nullable = false)
    private Long securityOfficerID;
    
    // Foreign key relationship to User table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SecurityOfficerID", insertable = false, updatable = false)
    private User securityOfficer;

    @Column(name = "Description", nullable = false, length = 500)
    private String description;

    @Column(name = "Date", nullable = false)
    private LocalDateTime date;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime updated_at;

    @PrePersist
    public void prePersist() {
        if (this.status == null || this.status.isBlank()) {
            this.status = "PENDING";
        }
        if (this.date == null) {
            this.date = LocalDateTime.now();
        }
    }

    // Constructors
    public SuspiciousReport() {}

    public SuspiciousReport(Long securityOfficerID, String description) {
        this.securityOfficerID = securityOfficerID;
        this.description = description;
        this.date = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getSuspiciousID() {
        return suspiciousID;
    }

    public void setSuspiciousID(Long suspiciousID) {
        this.suspiciousID = suspiciousID;
    }

    public Long getSecurityOfficerID() {
        return securityOfficerID;
    }

    public void setSecurityOfficerID(Long securityOfficerID) {
        this.securityOfficerID = securityOfficerID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public User getSecurityOfficer() {
        return securityOfficer;
    }

    public void setSecurityOfficer(User securityOfficer) {
        this.securityOfficer = securityOfficer;
        if (securityOfficer != null) {
            this.securityOfficerID = securityOfficer.getUserID();
        }
    }
}
