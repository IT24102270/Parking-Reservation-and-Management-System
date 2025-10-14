package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find notifications by customer ID
    @Query("SELECT n FROM Notification n WHERE n.customerId = :customerId ORDER BY n.dateSent DESC")
    List<Notification> findByCustomerIdOrderByDateSentDesc(@Param("customerId") Long customerId);
    
    // Find notifications by type
    @Query("SELECT n FROM Notification n WHERE n.customerId = :customerId AND n.type = :type ORDER BY n.dateSent DESC")
    List<Notification> findByCustomerIdAndType(@Param("customerId") Long customerId, @Param("type") String type);
    
    // Find recent notifications (limit) - using JPQL with Pageable
    @Query("SELECT n FROM Notification n WHERE n.customerId = :customerId ORDER BY n.dateSent DESC")
    List<Notification> findRecentByCustomerId(@Param("customerId") Long customerId, org.springframework.data.domain.Pageable pageable);
    
    // Count all notifications for a customer
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.customerId = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);
    
    // Find notifications by date range
    @Query("SELECT n FROM Notification n WHERE n.customerId = :customerId AND n.dateSent BETWEEN :startDate AND :endDate ORDER BY n.dateSent DESC")
    List<Notification> findByCustomerIdAndDateRange(@Param("customerId") Long customerId, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    // Delete old notifications (older than specified date)
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find notifications by type for all customers (admin use)
    @Query("SELECT n FROM Notification n WHERE n.type = :type ORDER BY n.dateSent DESC")
    List<Notification> findByType(@Param("type") String type);
    
    // Get notification statistics by type
    @Query("SELECT n.type, COUNT(n) FROM Notification n GROUP BY n.type")
    List<Object[]> getNotificationStatsByType();
}
