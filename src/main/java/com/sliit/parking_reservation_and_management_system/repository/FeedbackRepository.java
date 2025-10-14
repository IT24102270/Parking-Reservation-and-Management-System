package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    // Find feedback by customer ID
    List<Feedback> findByCustomerID(Long customerID);
    
    // Find feedback by rating
    List<Feedback> findByRating(Integer rating);
    
    // Get recent feedback (last 10)
    @Query("SELECT f FROM Feedback f ORDER BY f.date DESC")
    List<Feedback> findRecentFeedback();
    
    // Get feedback by customer ID ordered by date descending
    @Query("SELECT f FROM Feedback f WHERE f.customerID = ?1 ORDER BY f.date DESC")
    List<Feedback> findByCustomerIDOrderByDateDesc(Long customerID);
    
    // Count total feedback
    @Query("SELECT COUNT(f) FROM Feedback f")
    Long countTotalFeedback();
    
    // Get average rating
    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double getAverageRating();
    
    // Count feedback by rating
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.rating = ?1")
    Long countByRating(Integer rating);
}
