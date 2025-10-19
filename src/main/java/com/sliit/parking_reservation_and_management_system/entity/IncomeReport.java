package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "IncomeReport")
public class IncomeReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IncomeReportID")
    private Integer incomeReportID;

    @Column(name = "ReportID", nullable = false)
    private Integer reportID;

    @Column(name = "TotalIncome", nullable = false)
    private BigDecimal totalIncome;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Column(name = "StartDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDate endDate;

    // --- Getters and Setters ---
    // (Ensure all getters and setters are present here)
    public Integer getIncomeReportID() { return incomeReportID; }
    public void setIncomeReportID(Integer incomeReportID) { this.incomeReportID = incomeReportID; }
    public Integer getReportID() { return reportID; }
    public void setReportID(Integer reportID) { this.reportID = reportID; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}