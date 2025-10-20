package com.sliit.parking_reservation_and_management_system.dto;

import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.entity.Report;

import java.util.List;

public class ReportDetailsDTO {

    private Report report;
    private List<Payment> transactions;

    public ReportDetailsDTO(Report report, List<Payment> transactions) {
        this.report = report;
        this.transactions = transactions;
    }

    // Getters and Setters
    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public List<Payment> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Payment> transactions) {
        this.transactions = transactions;
    }
}