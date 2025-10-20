package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "IncomeReport")
public class IncomeReport {

    @Id
    @Column(name = "ReportID")
    private Integer reportID;

    @OneToOne
    @JoinColumn(name = "ReportID", referencedColumnName = "ReportID")
    @MapsId
    private Report report;

    @Column(name = "TotalIncome", nullable = false)
    private BigDecimal totalIncome;

    @Column(name = "StartDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Getters and Setters ---
    public Integer getReportID() { return reportID; }
    public void setReportID(Integer reportID) { this.reportID = reportID; }
    
    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }
    
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Compatibility methods for templates
    public Integer getIncomeReportID() { return reportID; }
    public void setIncomeReportID(Integer incomeReportID) { this.reportID = incomeReportID; }
    
    public LocalDate getDate() { return createdAt != null ? createdAt.toLocalDate() : null; }
    public void setDate(LocalDate date) { 
        if (date != null) {
            this.createdAt = date.atStartOfDay();
        }
    }
}