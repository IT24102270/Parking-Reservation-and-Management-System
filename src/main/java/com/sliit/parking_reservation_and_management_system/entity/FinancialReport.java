package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "FinancialReport")
@PrimaryKeyJoinColumn(name = "ReportID")
public class FinancialReport extends Report {
    @Column(name = "TotalRevenue")
    private BigDecimal totalRevenue;

    @Column(name = "TotalReservations")
    private Integer totalReservations;

    @Override // 👈 Add this
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    @Override // 👈 Add this
    public Integer getTotalReservations() {
        return totalReservations;
    }

    public void setTotalReservations(Integer totalReservations) {
        this.totalReservations = totalReservations;
    }

}