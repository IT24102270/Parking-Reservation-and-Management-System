package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.Feedback;
import com.sliit.parking_reservation_and_management_system.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    // Save feedback
    public Feedback saveFeedback(Feedback feedback) {
        try {
            if (feedback.getDate() == null) {
                feedback.setDate(LocalDateTime.now());
            }
            return feedbackRepository.save(feedback);
        } catch (Exception e) {
            System.err.println("Error saving feedback: " + e.getMessage());
            throw new RuntimeException("Failed to save feedback", e);
        }
    }
    
    // Get all feedback
    public List<Feedback> getAllFeedback() {
        try {
            return feedbackRepository.findAll();
        } catch (Exception e) {
            System.err.println("Error retrieving all feedback: " + e.getMessage());
            return List.of();
        }
    }
    
    // Get feedback by ID
    public Optional<Feedback> getFeedbackById(Long id) {
        try {
            return feedbackRepository.findById(id);
        } catch (Exception e) {
            System.err.println("Error retrieving feedback by ID " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    
    // Get feedback by customer ID
    public List<Feedback> getFeedbackByCustomerId(Long customerID) {
        try {
            return feedbackRepository.findByCustomerIDOrderByDateDesc(customerID);
        } catch (Exception e) {
            System.err.println("Error retrieving feedback for customer " + customerID + ": " + e.getMessage());
            return List.of();
        }
    }
    
    // Get recent feedback
    public List<Feedback> getRecentFeedback() {
        try {
            return feedbackRepository.findRecentFeedback();
        } catch (Exception e) {
            System.err.println("Error retrieving recent feedback: " + e.getMessage());
            return List.of();
        }
    }
    
    // Get feedback by rating
    public List<Feedback> getFeedbackByRating(Integer rating) {
        try {
            return feedbackRepository.findByRating(rating);
        } catch (Exception e) {
            System.err.println("Error retrieving feedback by rating " + rating + ": " + e.getMessage());
            return List.of();
        }
    }
    
    // Get total feedback count
    public Long getTotalFeedbackCount() {
        try {
            return feedbackRepository.countTotalFeedback();
        } catch (Exception e) {
            System.err.println("Error counting total feedback: " + e.getMessage());
            return 0L;
        }
    }
    
    // Get average rating
    public Double getAverageRating() {
        try {
            Double average = feedbackRepository.getAverageRating();
            return average != null ? average : 0.0;
        } catch (Exception e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
            return 0.0;
        }
    }
    
    // Get feedback count by rating
    public Long getFeedbackCountByRating(Integer rating) {
        try {
            return feedbackRepository.countByRating(rating);
        } catch (Exception e) {
            System.err.println("Error counting feedback by rating " + rating + ": " + e.getMessage());
            return 0L;
        }
    }
    
    // Delete feedback
    public boolean deleteFeedback(Long id) {
        try {
            if (feedbackRepository.existsById(id)) {
                feedbackRepository.deleteById(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting feedback " + id + ": " + e.getMessage());
            return false;
        }
    }
    
    // Create feedback
    public Feedback createFeedback(Long customerID, Integer rating, String comment) {
        try {
            Feedback feedback = new Feedback();
            feedback.setCustomerID(customerID);
            feedback.setRating(rating);
            feedback.setComment(comment);
            feedback.setDate(LocalDateTime.now());
            
            return saveFeedback(feedback);
        } catch (Exception e) {
            System.err.println("Error creating feedback: " + e.getMessage());
            throw new RuntimeException("Failed to create feedback", e);
        }
    }
}
