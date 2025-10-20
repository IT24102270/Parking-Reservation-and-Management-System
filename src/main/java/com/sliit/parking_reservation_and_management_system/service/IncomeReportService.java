package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.IncomeReport;
import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.entity.Report;
import com.sliit.parking_reservation_and_management_system.repository.IncomeReportRepository;
import com.sliit.parking_reservation_and_management_system.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncomeReportService {
    
    @Autowired
    private IncomeReportRepository incomeReportRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private ReportService reportService;
    
    /**
     * Generate and save income report based on payment data for a date range
     */
    public IncomeReport generateIncomeReport(LocalDate startDate, LocalDate endDate) {
        // Get all payments within the date range
        List<Payment> payments = paymentService.getAllPayments();
        
        // Filter payments by date range and completed status
        BigDecimal totalIncome = payments.stream()
            .filter(payment -> payment.getDate() != null)
            .filter(payment -> {
                LocalDate paymentDate = payment.getDate().toLocalDate();
                return !paymentDate.isBefore(startDate) && !paymentDate.isAfter(endDate);
            })
            .filter(payment -> "COMPLETED".equals(payment.getStatus()))
            .map(Payment::getAmount)
            .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0) // Only positive amounts (exclude refunds)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Create base report first (this will generate a ReportID)
        Integer reportId = reportService.createBaseReport();
        
        // Create the Report entity for the relationship
        Report baseReport = new Report();
        baseReport.setReportID(reportId);
        baseReport.setReportType("INCOME");
        
        // Create income report with reference to base report
        IncomeReport incomeReport = new IncomeReport();
        incomeReport.setReport(baseReport);
        incomeReport.setTotalIncome(totalIncome);
        incomeReport.setStartDate(startDate);
        incomeReport.setEndDate(endDate);
        incomeReport.setCreatedAt(LocalDateTime.now());
        incomeReport.setUpdatedAt(LocalDateTime.now());
        
        // Save to database
        return incomeReportRepository.save(incomeReport);
    }
    
    /**
     * Generate income report for current month
     */
    public IncomeReport generateCurrentMonthReport() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        
        return generateIncomeReport(startOfMonth, endOfMonth);
    }
    
    /**
     * Generate income report for last month
     */
    public IncomeReport generateLastMonthReport() {
        LocalDate now = LocalDate.now();
        LocalDate startOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate endOfLastMonth = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());
        
        return generateIncomeReport(startOfLastMonth, endOfLastMonth);
    }
    
    /**
     * Generate income report for custom date range
     */
    public IncomeReport generateCustomRangeReport(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        return generateIncomeReport(startDate, endDate);
    }
    
    /**
     * Get all income reports
     */
    public List<IncomeReport> getAllIncomeReports() {
        return incomeReportRepository.findAll();
    }
    
    /**
     * Get income report by ID
     */
    public IncomeReport getIncomeReportById(Integer id) {
        return incomeReportRepository.findById(id).orElse(null);
    }
    
    /**
     * Calculate payment statistics for a date range
     */
    public PaymentStatistics calculatePaymentStatistics(LocalDate startDate, LocalDate endDate) {
        List<Payment> payments = paymentService.getAllPayments();
        
        // Filter payments by date range
        List<Payment> filteredPayments = payments.stream()
            .filter(payment -> payment.getDate() != null)
            .filter(payment -> {
                LocalDate paymentDate = payment.getDate().toLocalDate();
                return !paymentDate.isBefore(startDate) && !paymentDate.isAfter(endDate);
            })
            .toList();
        
        long totalTransactions = filteredPayments.size();
        long completedPayments = filteredPayments.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()))
            .count();
        long refundedPayments = filteredPayments.stream()
            .filter(p -> "REFUNDED".equals(p.getStatus()))
            .count();
        
        BigDecimal totalRevenue = filteredPayments.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()))
            .map(Payment::getAmount)
            .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalRefunds = filteredPayments.stream()
            .filter(p -> "REFUNDED".equals(p.getStatus()))
            .map(Payment::getAmount)
            .filter(amount -> amount != null)
            .map(BigDecimal::abs)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netIncome = totalRevenue.subtract(totalRefunds);
        
        return new PaymentStatistics(
            totalTransactions,
            completedPayments,
            refundedPayments,
            totalRevenue,
            totalRefunds,
            netIncome
        );
    }
    
    /**
     * Inner class for payment statistics
     */
    public static class PaymentStatistics {
        private final long totalTransactions;
        private final long completedPayments;
        private final long refundedPayments;
        private final BigDecimal totalRevenue;
        private final BigDecimal totalRefunds;
        private final BigDecimal netIncome;
        
        public PaymentStatistics(long totalTransactions, long completedPayments, long refundedPayments,
                               BigDecimal totalRevenue, BigDecimal totalRefunds, BigDecimal netIncome) {
            this.totalTransactions = totalTransactions;
            this.completedPayments = completedPayments;
            this.refundedPayments = refundedPayments;
            this.totalRevenue = totalRevenue;
            this.totalRefunds = totalRefunds;
            this.netIncome = netIncome;
        }
        
        // Getters
        public long getTotalTransactions() { return totalTransactions; }
        public long getCompletedPayments() { return completedPayments; }
        public long getRefundedPayments() { return refundedPayments; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public BigDecimal getTotalRefunds() { return totalRefunds; }
        public BigDecimal getNetIncome() { return netIncome; }
    }
}
