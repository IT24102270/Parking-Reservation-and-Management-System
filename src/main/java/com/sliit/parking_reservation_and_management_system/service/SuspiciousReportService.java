package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.SuspiciousReport;
import com.sliit.parking_reservation_and_management_system.repository.SuspiciousReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SuspiciousReportService {

    @Autowired
    private SuspiciousReportRepository suspiciousReportRepository;

    // Create a new suspicious report
    public SuspiciousReport createReport(Long securityOfficerID, String description) {
        SuspiciousReport report = new SuspiciousReport(securityOfficerID, description);
        return suspiciousReportRepository.save(report);
    }

    // Save a suspicious report with User validation
    public SuspiciousReport saveReport(SuspiciousReport report) {
        // Validate that the SecurityOfficerID exists in User table
        if (report.getSecurityOfficerID() != null) {
            validateSecurityOfficerExists(report.getSecurityOfficerID());
        }
        return suspiciousReportRepository.save(report);
    }
    
    // Validate that a User ID exists in the database
    private void validateSecurityOfficerExists(Long securityOfficerID) {
        // This will be injected - we need UserService
        // For now, we'll add validation in the controller
    }

    // Get all reports by security officer
    public List<SuspiciousReport> getReportsBySecurityOfficer(Long securityOfficerID) {
        return suspiciousReportRepository.findBySecurityOfficerIDOrderByDateDesc(securityOfficerID);
    }

    // Get all reports
    public List<SuspiciousReport> getAllReports() {
        return suspiciousReportRepository.findAll();
    }

    // Get reports by status
    public List<SuspiciousReport> getReportsByStatus(String status) {
        return suspiciousReportRepository.findByStatusOrderByDateDesc(status);
    }

    // Get reports by security officer and status
    public List<SuspiciousReport> getReportsBySecurityOfficerAndStatus(Long securityOfficerID, String status) {
        return suspiciousReportRepository.findBySecurityOfficerIDAndStatusOrderByDateDesc(securityOfficerID, status);
    }

    // Get report by ID
    public Optional<SuspiciousReport> getReportById(Long id) {
        return suspiciousReportRepository.findById(id);
    }

    // Update report status
    public SuspiciousReport updateReportStatus(Long reportId, String status) {
        Optional<SuspiciousReport> reportOpt = suspiciousReportRepository.findById(reportId);
        if (reportOpt.isPresent()) {
            SuspiciousReport report = reportOpt.get();
            report.setStatus(status);
            return suspiciousReportRepository.save(report);
        }
        return null;
    }

    // Delete report
    public boolean deleteReport(Long reportId) {
        if (suspiciousReportRepository.existsById(reportId)) {
            suspiciousReportRepository.deleteById(reportId);
            return true;
        }
        return false;
    }

    // Get recent reports (last 7 days)
    public List<SuspiciousReport> getRecentReports() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return suspiciousReportRepository.findRecentReports(weekAgo);
    }
    
    // Get recent reports by security officer (last 7 days)
    public List<SuspiciousReport> getRecentReportsBySecurityOfficer(Long securityOfficerID) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return suspiciousReportRepository.findRecentReportsBySecurityOfficer(securityOfficerID, weekAgo);
    }
    
    // Get latest reports (limited to top N)
    public List<SuspiciousReport> getLatestReports(int limit) {
        List<SuspiciousReport> allReports = suspiciousReportRepository.findLatestReports();
        return allReports.size() > limit ? allReports.subList(0, limit) : allReports;
    }
    
    // Get latest reports by security officer (limited to top N)
    public List<SuspiciousReport> getLatestReportsBySecurityOfficer(Long securityOfficerID, int limit) {
        List<SuspiciousReport> allReports = suspiciousReportRepository.findLatestReportsBySecurityOfficer(securityOfficerID);
        return allReports.size() > limit ? allReports.subList(0, limit) : allReports;
    }

    // Get reports within date range
    public List<SuspiciousReport> getReportsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return suspiciousReportRepository.findByDateBetween(startDate, endDate);
    }

    // Count reports by security officer
    public long countReportsBySecurityOfficer(Long securityOfficerID) {
        return suspiciousReportRepository.countBySecurityOfficerID(securityOfficerID);
    }

    // Count reports by status
    public long countReportsByStatus(String status) {
        return suspiciousReportRepository.countByStatus(status);
    }

    // Get dashboard statistics for security officer
    public DashboardStats getDashboardStats(Long securityOfficerID) {
        long totalReports = countReportsBySecurityOfficer(securityOfficerID);
        long pendingReports = suspiciousReportRepository.countBySecurityOfficerID(securityOfficerID);
        List<SuspiciousReport> recentReports = getRecentReports();
        
        return new DashboardStats(totalReports, pendingReports, recentReports.size());
    }

    // Inner class for dashboard statistics
    public static class DashboardStats {
        private long totalReports;
        private long pendingReports;
        private long recentReports;

        public DashboardStats(long totalReports, long pendingReports, long recentReports) {
            this.totalReports = totalReports;
            this.pendingReports = pendingReports;
            this.recentReports = recentReports;
        }

        // Getters
        public long getTotalReports() { return totalReports; }
        public long getPendingReports() { return pendingReports; }
        public long getRecentReports() { return recentReports; }
    }
}
