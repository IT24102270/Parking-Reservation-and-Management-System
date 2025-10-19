package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.SuspiciousReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SuspiciousReportRepository extends JpaRepository<SuspiciousReport, Long> {

    // Find all reports by security officer ID
    List<SuspiciousReport> findBySecurityOfficerIDOrderByDateDesc(Long securityOfficerID);

    // Find reports by status
    List<SuspiciousReport> findByStatusOrderByDateDesc(String status);

    // Find reports by security officer and status
    List<SuspiciousReport> findBySecurityOfficerIDAndStatusOrderByDateDesc(Long securityOfficerID, String status);

    // Find reports within date range
    @Query("SELECT sr FROM SuspiciousReport sr WHERE sr.date BETWEEN :startDate AND :endDate ORDER BY sr.date DESC")
    List<SuspiciousReport> findByDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Count reports by security officer
    long countBySecurityOfficerID(Long securityOfficerID);

    // Count reports by status
    long countByStatus(String status);

    // Find recent reports (last 7 days)
    @Query("SELECT sr FROM SuspiciousReport sr WHERE sr.date >= :weekAgo ORDER BY sr.date DESC")
    List<SuspiciousReport> findRecentReports(@Param("weekAgo") LocalDateTime weekAgo);
    
    // Find recent reports by security officer (last 7 days)
    @Query("SELECT sr FROM SuspiciousReport sr WHERE sr.securityOfficerID = :securityOfficerID AND sr.date >= :weekAgo ORDER BY sr.date DESC")
    List<SuspiciousReport> findRecentReportsBySecurityOfficer(@Param("securityOfficerID") Long securityOfficerID, @Param("weekAgo") LocalDateTime weekAgo);
    
    // Find latest reports (limit to top N)
    @Query("SELECT sr FROM SuspiciousReport sr ORDER BY sr.date DESC")
    List<SuspiciousReport> findLatestReports();
    
    // Find latest reports by security officer (limit to top N)
    @Query("SELECT sr FROM SuspiciousReport sr WHERE sr.securityOfficerID = :securityOfficerID ORDER BY sr.date DESC")
    List<SuspiciousReport> findLatestReportsBySecurityOfficer(@Param("securityOfficerID") Long securityOfficerID);
}
