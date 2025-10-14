package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find payment by reservation ID
    Optional<Payment> findByReservationID(Long reservationID);
    
    // Find all payments by reservation ID (in case of multiple payments)
    List<Payment> findAllByReservationID(Long reservationID);
    
    // Find payments by status
    List<Payment> findByStatus(String status);
    
    // Find payments by method
    List<Payment> findByMethod(String method);
    
    // Find payments within date range
    @Query("SELECT p FROM Payment p WHERE p.date BETWEEN ?1 AND ?2")
    List<Payment> findPaymentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find payments by status and date range
    @Query("SELECT p FROM Payment p WHERE p.status = ?1 AND p.date BETWEEN ?2 AND ?3")
    List<Payment> findByStatusAndDateBetween(String status, LocalDateTime startDate, LocalDateTime endDate);
    
    // Get total amount by status
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = ?1")
    BigDecimal getTotalAmountByStatus(String status);
    
    // Count payments by status
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = ?1")
    Long countByStatus(String status);
    
    // Find recent payments (last 10)
    @Query("SELECT p FROM Payment p ORDER BY p.createdAt DESC")
    List<Payment> findRecentPayments();
    
    // Find pending payments older than specified hours
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < ?1")
    List<Payment> findPendingPaymentsOlderThan(LocalDateTime cutoffTime);
    
    // Get payment statistics
    @Query("SELECT p.status, COUNT(p), SUM(p.amount) FROM Payment p GROUP BY p.status")
    List<Object[]> getPaymentStatistics();
    
    // Find payments by reservation IDs (for bulk operations)
    @Query("SELECT p FROM Payment p WHERE p.reservationID IN ?1")
    List<Payment> findByReservationIDIn(List<Long> reservationIDs);
    
    // Check if payment exists for reservation
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.reservationID = ?1")
    boolean existsByReservationID(Long reservationID);
}
