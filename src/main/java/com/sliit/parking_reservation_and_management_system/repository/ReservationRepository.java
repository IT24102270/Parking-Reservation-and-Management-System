package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByUserId(Long userId);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId AND r.status = 'ACTIVE' AND r.endTime > :currentTime")
    Long countActiveByUserId(@Param("userId") Long userId, @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.status = 'ACTIVE' AND r.endTime > :currentTime")
    List<Reservation> findActiveByUserId(@Param("userId") Long userId, @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    List<Reservation> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Reservation r WHERE r.slotId = :slotId AND r.status = 'ACTIVE' AND r.endTime > :currentTime")
    List<Reservation> findActiveBySlotId(@Param("slotId") Long slotId, @Param("currentTime") LocalDateTime currentTime);
    
    // Notification scheduler queries
    @Query("SELECT r FROM Reservation r WHERE r.startTime BETWEEN :startTime AND :endTime AND r.status = 'CONFIRMED'")
    List<Reservation> findUpcomingReservations(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT r FROM Reservation r WHERE r.endTime BETWEEN :startTime AND :endTime AND r.status = 'ACTIVE'")
    List<Reservation> findExpiringReservations(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT r FROM Reservation r WHERE r.createdAt BETWEEN :startTime AND :endTime AND r.status = 'PENDING'")
    List<Reservation> findPendingPayments(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT r FROM Reservation r WHERE r.createdAt <= :cutoffTime AND r.status = 'PENDING'")
    List<Reservation> findUrgentPayments(@Param("cutoffTime") LocalDateTime cutoffTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT r FROM Reservation r WHERE r.startTime <= :currentTime AND r.status = 'CONFIRMED'")
    List<Reservation> findStartingReservations(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT r FROM Reservation r WHERE r.endTime <= :currentTime AND r.status = 'ACTIVE'")
    List<Reservation> findCompletingReservations(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT r FROM Reservation r WHERE r.createdAt <= :paymentDeadline AND r.status = 'PENDING'")
    List<Reservation> findOverduePayments(@Param("paymentDeadline") LocalDateTime paymentDeadline);
    
    @Query("SELECT r FROM Reservation r WHERE r.startTime >= :startDate AND r.startTime < :endDate")
    List<Reservation> findReservationsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Recent reservations for dashboard
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    List<Reservation> findRecentByUserId(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);
}
