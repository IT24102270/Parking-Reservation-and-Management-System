package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.SupportIssue;
import com.sliit.parking_reservation_and_management_system.repository.SupportIssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupportIssueService {
    
    @Autowired
    private SupportIssueRepository supportIssueRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    // Create a new support issue
    public SupportIssue createSupportIssue(Long customerId, String description) {
        SupportIssue supportIssue = new SupportIssue(customerId, description);
        SupportIssue savedIssue = supportIssueRepository.save(supportIssue);
        
        // Create notification for support issue creation
        try {
            notificationService.createNotification(
                customerId, 
                "SUPPORT", 
                "Your support ticket " + savedIssue.getTicketId() + " has been created. We'll get back to you soon!"
            );
        } catch (Exception e) {
            System.err.println("Failed to create notification for support issue: " + e.getMessage());
        }
        
        return savedIssue;
    }
    
    // Get support issue by ID
    public Optional<SupportIssue> getSupportIssueById(Long issueId) {
        return supportIssueRepository.findById(issueId);
    }
    
    // Get all support issues for a customer
    public List<SupportIssue> getSupportIssuesByCustomerId(Long customerId) {
        return supportIssueRepository.findByCustomerIdOrderByRaisedDateDesc(customerId);
    }
    
    // Get recent support issues with limit
    public List<SupportIssue> getRecentSupportIssues(Long customerId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return supportIssueRepository.findRecentByCustomerId(customerId, pageRequest);
    }
    
    // Get support issues by status
    public List<SupportIssue> getSupportIssuesByStatus(Long customerId, String status) {
        return supportIssueRepository.findByCustomerIdAndStatus(customerId, status);
    }
    
    // Count total support issues for customer
    public long countSupportIssuesByCustomerId(Long customerId) {
        return supportIssueRepository.countByCustomerId(customerId);
    }
    
    // Count open support issues for customer
    public long countOpenSupportIssuesByCustomerId(Long customerId) {
        return supportIssueRepository.countOpenByCustomerId(customerId);
    }
    
    // Update support issue status
    public SupportIssue updateSupportIssueStatus(Long issueId, String newStatus) {
        Optional<SupportIssue> optionalIssue = supportIssueRepository.findById(issueId);
        if (optionalIssue.isPresent()) {
            SupportIssue issue = optionalIssue.get();
            String oldStatus = issue.getStatus();
            issue.setStatus(newStatus);
            SupportIssue updatedIssue = supportIssueRepository.save(issue);
            
            // Create notification for status change
            try {
                String statusMessage = getStatusChangeMessage(newStatus);
                notificationService.createNotification(
                    issue.getCustomerId(), 
                    "SUPPORT", 
                    "Your support ticket " + updatedIssue.getTicketId() + " status has been updated to " + newStatus + ". " + statusMessage
                );
            } catch (Exception e) {
                System.err.println("Failed to create notification for status update: " + e.getMessage());
            }
            
            return updatedIssue;
        }
        return null;
    }
    
    // Delete support issue
    public boolean deleteSupportIssue(Long issueId) {
        if (supportIssueRepository.existsById(issueId)) {
            supportIssueRepository.deleteById(issueId);
            return true;
        }
        return false;
    }
    
    // Get all support issues (for admin)
    public List<SupportIssue> getAllSupportIssues() {
        return supportIssueRepository.findAllOrderByRaisedDateDesc();
    }
    
    // Get support issues by status (for admin)
    public List<SupportIssue> getSupportIssuesByStatus(String status) {
        return supportIssueRepository.findByStatus(status);
    }
    
    // Get support statistics
    public List<Object[]> getSupportStatistics() {
        return supportIssueRepository.getSupportStatsByStatus();
    }
    
    // Helper method to get status change message
    private String getStatusChangeMessage(String status) {
        switch (status.toUpperCase()) {
            case "OPEN":
                return "Your issue is now open and waiting for review.";
            case "IN_PROGRESS":
                return "Our support team is actively working on your issue.";
            case "RESOLVED":
                return "Your issue has been resolved. Please check if the solution works for you.";
            case "CLOSED":
                return "Your support ticket has been closed. Thank you for contacting us.";
            default:
                return "Status updated.";
        }
    }
    
    // Create predefined support issue types
    public SupportIssue createPaymentIssue(Long customerId, String details) {
        String description = "Payment Issue: " + details;
        return createSupportIssue(customerId, description);
    }
    
    public SupportIssue createBookingIssue(Long customerId, String details) {
        String description = "Booking Issue: " + details;
        return createSupportIssue(customerId, description);
    }
    
    public SupportIssue createTechnicalIssue(Long customerId, String details) {
        String description = "Technical Issue: " + details;
        return createSupportIssue(customerId, description);
    }
    
    public SupportIssue createGeneralInquiry(Long customerId, String details) {
        String description = "General Inquiry: " + details;
        return createSupportIssue(customerId, description);
    }
}
