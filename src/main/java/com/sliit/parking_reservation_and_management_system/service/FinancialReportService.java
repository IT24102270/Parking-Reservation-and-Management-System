package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.entity.Report;
import com.sliit.parking_reservation_and_management_system.repository.PaymentRepository;
import com.sliit.parking_reservation_and_management_system.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
public class FinancialReportService {

    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;

    // ✅ CRUD OPERATIONS FOR FINANCIAL REPORTS

    /**
     * Create a new financial report
     */
    public Report createFinancialReport(String reportType, LocalDate startDate, LocalDate endDate, String description) {
        Report report = new Report();
        report.setReportType(reportType);
        report.setGeneratedBy(1); // TODO: Get actual user ID from security context
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        
        // Calculate financial data based on date range
        FinancialData financialData = calculateFinancialData(startDate, endDate);
        
        // Set report description with financial summary
        String reportDescription = description != null ? description : 
            String.format("%s Financial Report (%s to %s) - Total Revenue: $%.2f, Net Income: $%.2f", 
                reportType, startDate, endDate, financialData.totalRevenue, financialData.netIncome);
        
        // Note: Report table doesn't have description field, so we'll store it in a way that works with the schema
        
        return reportRepository.save(report);
    }

    /**
     * Get all financial reports
     */
    public List<Report> getAllFinancialReports() {
        return reportRepository.findAll().stream()
            .filter(report -> report.getReportType().contains("FINANCIAL") || 
                            report.getReportType().equals("DAILY") ||
                            report.getReportType().equals("WEEKLY") ||
                            report.getReportType().equals("MONTHLY"))
            .toList();
    }

    /**
     * Get financial report by ID
     */
    public Optional<Report> getFinancialReportById(Integer reportId) {
        return reportRepository.findById(reportId);
    }

    /**
     * Update financial report
     */
    public Report updateFinancialReport(Integer reportId, String reportType, String description) {
        Optional<Report> reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();
            if (reportType != null) {
                report.setReportType(reportType);
            }
            report.setUpdatedAt(LocalDateTime.now());
            return reportRepository.save(report);
        }
        throw new RuntimeException("Financial report not found with ID: " + reportId);
    }

    /**
     * Delete financial report
     */
    public boolean deleteFinancialReport(Integer reportId) {
        if (reportRepository.existsById(reportId)) {
            reportRepository.deleteById(reportId);
            return true;
        }
        return false;
    }

    // ✅ FINANCIAL REPORT GENERATION METHODS

    /**
     * Generate Daily Financial Report
     */
    public Report generateDailyReport(LocalDate date) {
        LocalDate startDate = date;
        LocalDate endDate = date;
        
        Report report = createFinancialReport("DAILY", startDate, endDate, 
            "Daily Financial Report for " + date);
        
        return report;
    }

    /**
     * Generate Weekly Financial Report
     */
    public Report generateWeeklyReport(LocalDate weekStartDate) {
        LocalDate startDate = weekStartDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusDays(6);
        
        Report report = createFinancialReport("WEEKLY", startDate, endDate, 
            "Weekly Financial Report for week starting " + startDate);
        
        return report;
    }

    /**
     * Generate Monthly Financial Report
     */
    public Report generateMonthlyReport(LocalDate monthDate) {
        LocalDate startDate = monthDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endDate = monthDate.with(TemporalAdjusters.lastDayOfMonth());
        
        Report report = createFinancialReport("MONTHLY", startDate, endDate, 
            "Monthly Financial Report for " + monthDate.getMonth() + " " + monthDate.getYear());
        
        return report;
    }

    /**
     * Generate Custom Range Financial Report
     */
    public Report generateCustomRangeReport(LocalDate startDate, LocalDate endDate, String description) {
        String reportType = "FINANCIAL_CUSTOM";
        String reportDescription = description != null ? description : 
            "Custom Financial Report from " + startDate + " to " + endDate;
        
        return createFinancialReport(reportType, startDate, endDate, reportDescription);
    }

    // ✅ FINANCIAL DATA CALCULATION METHODS

    /**
     * Calculate comprehensive financial data for a date range
     */
    public FinancialData calculateFinancialData(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Payment> allPayments = paymentRepository.findAll().stream()
            .filter(payment -> payment.getDate() != null)
            .filter(payment -> !payment.getDate().isBefore(startDateTime) && 
                             !payment.getDate().isAfter(endDateTime))
            .toList();
        
        FinancialData data = new FinancialData();
        
        // Calculate revenue (completed positive payments)
        data.totalRevenue = allPayments.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()))
            .filter(p -> p.getAmount().compareTo(BigDecimal.ZERO) > 0)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate refunds (completed negative payments or refunded status)
        data.totalRefunds = allPayments.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()) && p.getAmount().compareTo(BigDecimal.ZERO) < 0 ||
                        "REFUNDED".equals(p.getStatus()))
            .map(payment -> payment.getAmount().abs())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate net income
        data.netIncome = data.totalRevenue.subtract(data.totalRefunds);
        
        // Calculate transaction counts
        data.totalTransactions = allPayments.size();
        data.completedTransactions = (int) allPayments.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()))
            .count();
        data.refundedTransactions = (int) allPayments.stream()
            .filter(p -> "REFUNDED".equals(p.getStatus()) || 
                        ("COMPLETED".equals(p.getStatus()) && p.getAmount().compareTo(BigDecimal.ZERO) < 0))
            .count();
        data.pendingTransactions = (int) allPayments.stream()
            .filter(p -> "PENDING".equals(p.getStatus()))
            .count();
        
        // Calculate rates
        if (data.totalTransactions > 0) {
            data.completionRate = (data.completedTransactions * 100.0) / data.totalTransactions;
            data.refundRate = (data.refundedTransactions * 100.0) / data.totalTransactions;
        }
        
        // Set date range
        data.startDate = startDate;
        data.endDate = endDate;
        
        return data;
    }

    /**
     * Get financial data for an existing report
     */
    public FinancialData getFinancialDataForReport(Report report, LocalDate startDate, LocalDate endDate) {
        return calculateFinancialData(startDate, endDate);
    }

    // ✅ QUICK REPORT GENERATION METHODS

    /**
     * Generate Today's Financial Report
     */
    public Report generateTodayReport() {
        return generateDailyReport(LocalDate.now());
    }

    /**
     * Generate This Week's Financial Report
     */
    public Report generateThisWeekReport() {
        return generateWeeklyReport(LocalDate.now());
    }

    /**
     * Generate This Month's Financial Report
     */
    public Report generateThisMonthReport() {
        return generateMonthlyReport(LocalDate.now());
    }

    /**
     * Generate Last Month's Financial Report
     */
    public Report generateLastMonthReport() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        return generateMonthlyReport(lastMonth);
    }

    // ✅ REPORT SEARCH AND FILTER METHODS

    /**
     * Get reports by type
     */
    public List<Report> getReportsByType(String reportType) {
        return reportRepository.findAll().stream()
            .filter(report -> report.getReportType().equals(reportType))
            .toList();
    }

    /**
     * Get reports by date range
     */
    public List<Report> getReportsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return reportRepository.findAll().stream()
            .filter(report -> report.getCreatedAt() != null)
            .filter(report -> !report.getCreatedAt().isBefore(startDate) && 
                             !report.getCreatedAt().isAfter(endDate))
            .toList();
    }

    /**
     * Get recent reports (last 30 days)
     */
    public List<Report> getRecentFinancialReports() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return getReportsByDateRange(thirtyDaysAgo, LocalDateTime.now()).stream()
            .filter(report -> report.getReportType().contains("FINANCIAL") || 
                            report.getReportType().equals("DAILY") ||
                            report.getReportType().equals("WEEKLY") ||
                            report.getReportType().equals("MONTHLY"))
            .toList();
    }

    // ✅ INNER CLASS FOR FINANCIAL DATA

    public static class FinancialData {
        public BigDecimal totalRevenue = BigDecimal.ZERO;
        public BigDecimal totalRefunds = BigDecimal.ZERO;
        public BigDecimal netIncome = BigDecimal.ZERO;
        public int totalTransactions = 0;
        public int completedTransactions = 0;
        public int refundedTransactions = 0;
        public int pendingTransactions = 0;
        public double completionRate = 0.0;
        public double refundRate = 0.0;
        public LocalDate startDate;
        public LocalDate endDate;
        
        // Getters and setters
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public BigDecimal getTotalRefunds() { return totalRefunds; }
        public void setTotalRefunds(BigDecimal totalRefunds) { this.totalRefunds = totalRefunds; }
        
        public BigDecimal getNetIncome() { return netIncome; }
        public void setNetIncome(BigDecimal netIncome) { this.netIncome = netIncome; }
        
        public int getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
        
        public int getCompletedTransactions() { return completedTransactions; }
        public void setCompletedTransactions(int completedTransactions) { this.completedTransactions = completedTransactions; }
        
        public int getRefundedTransactions() { return refundedTransactions; }
        public void setRefundedTransactions(int refundedTransactions) { this.refundedTransactions = refundedTransactions; }
        
        public int getPendingTransactions() { return pendingTransactions; }
        public void setPendingTransactions(int pendingTransactions) { this.pendingTransactions = pendingTransactions; }
        
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
        
        public double getRefundRate() { return refundRate; }
        public void setRefundRate(double refundRate) { this.refundRate = refundRate; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }
}
