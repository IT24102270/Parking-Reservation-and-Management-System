package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Report")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ReportType", discriminatorType = DiscriminatorType.STRING)
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReportID")
    private Integer reportID;

    @Column(name = "ReportType", nullable = false, insertable = false, updatable = false)
    private String reportType;

    @Column(name = "GeneratedBy", nullable = false)
    private Integer generatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===========================================
    // All Getters and Setters
    // ===========================================

    public Integer getReportID() {
        return reportID;
    }

    public void setReportID(Integer reportID) {
        this.reportID = reportID;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public Integer getGeneratedBy() {
        return generatedBy;
    }

    // ✅ THIS IS THE MISSING METHOD THAT FIXES THE ERROR
    public void setGeneratedBy(Integer generatedBy) {
        this.generatedBy = generatedBy;
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

    // Placeholder getters for subclass compatibility - return null since fields don't exist in DB
    public BigDecimal getTotalRevenue() { 
        return null; 
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        // No-op since field doesn't exist in database
    }
    
    public Integer getTotalReservations() { 
        return null; 
    }
    
    public void setTotalReservations(Integer totalReservations) {
        // No-op since field doesn't exist in database
    }
    
    public BigDecimal getCustomerActivityRate() { 
        return null; 
    }
    
    public void setCustomerActivityRate(BigDecimal customerActivityRate) {
        // No-op since field doesn't exist in database
    }
    
    public BigDecimal getSlotUsageRate() { 
        return null; 
    }
    
    public void setSlotUsageRate(BigDecimal slotUsageRate) {
        // No-op since field doesn't exist in database
    }
    
    // Placeholder methods for compatibility with ReportService
    public LocalDate getStartDate() { return null; }
    public void setStartDate(LocalDate startDate) { /* No-op */ }
    public LocalDate getEndDate() { return null; }
    public void setEndDate(LocalDate endDate) { /* No-op */ }
    public User getUser() { return null; }
    public void setUser(User user) { /* No-op */ }
}