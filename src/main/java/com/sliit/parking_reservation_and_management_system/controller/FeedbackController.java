package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.Feedback;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.FeedbackService;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customer")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/feedback")
    public String feedbackForm(Model model) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get user's previous feedback
            List<Feedback> userFeedback = feedbackService.getFeedbackByCustomerId(currentUser.getUserID());
            
            // Create new feedback object for form
            Feedback feedback = new Feedback();
            
            model.addAttribute("user", currentUser);
            model.addAttribute("feedback", feedback);
            model.addAttribute("userFeedback", userFeedback);
            
            System.out.println("Feedback form loaded for user: " + currentUser.getEmail());
            System.out.println("Previous feedback count: " + userFeedback.size());
            
        } catch (Exception e) {
            System.err.println("Error loading feedback form: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading feedback form");
        }
        
        return "customer-feedback";
    }
    
    @PostMapping("/feedback/submit")
    public String submitFeedback(@RequestParam Integer rating,
                                @RequestParam String comment,
                                RedirectAttributes redirectAttributes) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Validate rating
            if (rating < 1 || rating > 5) {
                redirectAttributes.addFlashAttribute("error", "Rating must be between 1 and 5 stars");
                return "redirect:/customer/feedback";
            }
            
            // Validate comment
            if (comment == null || comment.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please provide your feedback comment");
                return "redirect:/customer/feedback";
            }
            
            if (comment.length() > 1000) {
                redirectAttributes.addFlashAttribute("error", "Comment must be less than 1000 characters");
                return "redirect:/customer/feedback";
            }
            
            // Create and save feedback
            Feedback feedback = feedbackService.createFeedback(
                currentUser.getUserID(), 
                rating, 
                comment.trim()
            );
            
            System.out.println("Feedback submitted successfully:");
            System.out.println("- Feedback ID: " + feedback.getFeedbackID());
            System.out.println("- Customer ID: " + feedback.getCustomerID());
            System.out.println("- Rating: " + feedback.getRating());
            System.out.println("- Comment: " + feedback.getComment());
            System.out.println("- Date: " + feedback.getDate());
            
            redirectAttributes.addFlashAttribute("success", "Thank you for your feedback! We appreciate your input.");
            
        } catch (Exception e) {
            System.err.println("Error submitting feedback: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error submitting feedback. Please try again.");
        }
        
        return "redirect:/customer/feedback";
    }
    
    @GetMapping("/feedback/history")
    public String feedbackHistory(Model model) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get user's feedback history
            List<Feedback> userFeedback = feedbackService.getFeedbackByCustomerId(currentUser.getUserID());
            
            model.addAttribute("user", currentUser);
            model.addAttribute("feedbackList", userFeedback);
            
            System.out.println("Feedback history loaded for user: " + currentUser.getEmail());
            System.out.println("Total feedback: " + userFeedback.size());
            
        } catch (Exception e) {
            System.err.println("Error loading feedback history: " + e.getMessage());
            model.addAttribute("error", "Error loading feedback history");
        }
        
        return "customer-feedback-history";
    }
    
    @PostMapping("/feedback/{id}/delete")
    public String deleteFeedback(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get feedback to verify ownership
            Feedback feedback = feedbackService.getFeedbackById(id).orElse(null);
            if (feedback == null) {
                redirectAttributes.addFlashAttribute("error", "Feedback not found");
                return "redirect:/customer/feedback/history";
            }
            
            // Check if user owns this feedback
            if (!feedback.getCustomerID().equals(currentUser.getUserID())) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/customer/feedback/history";
            }
            
            // Delete feedback
            boolean deleted = feedbackService.deleteFeedback(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Feedback deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Error deleting feedback");
            }
            
        } catch (Exception e) {
            System.err.println("Error deleting feedback: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error deleting feedback");
        }
        
        return "redirect:/customer/feedback/history";
    }
    
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                return userService.getAllUsers().stream()
                    .filter(user -> user.getEmail().equals(username))
                    .findFirst()
                    .orElse(null);
            }
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
        }
        
        // Return default user for testing if no authentication
        User defaultUser = new User();
        defaultUser.setUserID(1L);
        defaultUser.setFirstName("Test");
        defaultUser.setLastName("User");
        defaultUser.setEmail("test@example.com");
        return defaultUser;
    }
}
