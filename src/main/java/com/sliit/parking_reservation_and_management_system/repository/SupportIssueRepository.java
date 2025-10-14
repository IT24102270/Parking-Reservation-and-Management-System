package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.SupportIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SupportIssueRepository extends JpaRepository<SupportIssue, Long> {
    
    // Find support issues by customer ID
    @Query("SELECT s FROM SupportIssue s WHERE s.customerId = :customerId ORDER BY s.raisedDate DESC")
    List<SupportIssue> findByCustomerIdOrderByRaisedDateDesc(@Param("customerId") Long customerId);
    
    // Find support issues by status
    @Query("SELECT s FROM SupportIssue s WHERE s.customerId = :customerId AND s.status = :status ORDER BY s.raisedDate DESC")
    List<SupportIssue> findByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("status") String status);
    
    // Find recent support issues (limit)
    @Query("SELECT s FROM SupportIssue s WHERE s.customerId = :customerId ORDER BY s.raisedDate DESC")
    List<SupportIssue> findRecentByCustomerId(@Param("customerId") Long customerId, org.springframework.data.domain.Pageable pageable);
    
    // Count support issues by customer
    @Query("SELECT COUNT(s) FROM SupportIssue s WHERE s.customerId = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);
    
    // Count open support issues by customer
    @Query("SELECT COUNT(s) FROM SupportIssue s WHERE s.customerId = :customerId AND s.status IN ('OPEN', 'IN_PROGRESS')")
    long countOpenByCustomerId(@Param("customerId") Long customerId);
    
    // Find support issues by date range
    @Query("SELECT s FROM SupportIssue s WHERE s.customerId = :customerId AND s.raisedDate BETWEEN :startDate AND :endDate ORDER BY s.raisedDate DESC")
    List<SupportIssue> findByCustomerIdAndDateRange(@Param("customerId") Long customerId, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    // Find all support issues for admin (all customers)
    @Query("SELECT s FROM SupportIssue s ORDER BY s.raisedDate DESC")
    List<SupportIssue> findAllOrderByRaisedDateDesc();
    
    // Find support issues by status for admin
    @Query("SELECT s FROM SupportIssue s WHERE s.status = :status ORDER BY s.raisedDate DESC")
    List<SupportIssue> findByStatus(@Param("status") String status);
    
    // Get support statistics by status
    @Query("SELECT s.status, COUNT(s) FROM SupportIssue s GROUP BY s.status")
    List<Object[]> getSupportStatsByStatus();
}
