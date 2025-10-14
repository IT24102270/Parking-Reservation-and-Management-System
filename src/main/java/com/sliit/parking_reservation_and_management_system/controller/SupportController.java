package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.SupportIssue;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.SupportIssueService;
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
@RequestMapping("/customer/support")
public class SupportController {
    
    @Autowired
    private SupportIssueService supportIssueService;
    
    @Autowired
    private UserService userService;
    
    // Display support form
    @GetMapping("/help")
    public String showSupportForm(Model model) {
        System.out.println("=== Support Form Request ===");
        
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            model.addAttribute("user", currentUser);
            model.addAttribute("supportIssue", new SupportIssue());
            
            System.out.println("Support form loaded for user: " + currentUser.getEmail());
            return "customer-support-form";
            
        } catch (Exception e) {
            System.err.println("Error loading support form: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/customer/dashboard";
        }
    }
    
    // Submit support issue
    @PostMapping("/submit")
    public String submitSupportIssue(@RequestParam("issueType") String issueType,
                                   @RequestParam("description") String description,
                                   @RequestParam(value = "priority", defaultValue = "MEDIUM") String priority,
                                   RedirectAttributes redirectAttributes) {
        
        System.out.println("=== Support Issue Submission ===");
        System.out.println("Issue Type: " + issueType);
        System.out.println("Priority: " + priority);
        System.out.println("Description: " + description);
        
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Please log in to submit a support request.");
                return "redirect:/login";
            }
            
            // Create support issue based on type
            SupportIssue supportIssue;
            String fullDescription = "[Priority: " + priority + "] " + description;
            
            switch (issueType.toUpperCase()) {
                case "PAYMENT":
                    supportIssue = supportIssueService.createPaymentIssue(currentUser.getUserID(), fullDescription);
                    break;
                case "BOOKING":
                    supportIssue = supportIssueService.createBookingIssue(currentUser.getUserID(), fullDescription);
                    break;
                case "TECHNICAL":
                    supportIssue = supportIssueService.createTechnicalIssue(currentUser.getUserID(), fullDescription);
                    break;
                case "GENERAL":
                    supportIssue = supportIssueService.createGeneralInquiry(currentUser.getUserID(), fullDescription);
                    break;
                default:
                    supportIssue = supportIssueService.createSupportIssue(currentUser.getUserID(), fullDescription);
            }
            
            System.out.println("Support issue created: " + supportIssue.getTicketId());
            
            redirectAttributes.addFlashAttribute("success", 
                "Support ticket created successfully! Ticket ID: " + supportIssue.getTicketId() + 
                ". We'll get back to you within 24 hours.");
            
            return "redirect:/customer/support/tickets";
            
        } catch (Exception e) {
            System.err.println("Error creating support issue: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to create support ticket. Please try again.");
            return "redirect:/customer/support/help";
        }
    }
    
    // View support tickets
    @GetMapping("/tickets")
    public String viewSupportTickets(Model model) {
        System.out.println("=== Support Tickets Request ===");
        
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get user's support issues
            List<SupportIssue> supportIssues = supportIssueService.getSupportIssuesByCustomerId(currentUser.getUserID());
            long totalTickets = supportIssueService.countSupportIssuesByCustomerId(currentUser.getUserID());
            long openTickets = supportIssueService.countOpenSupportIssuesByCustomerId(currentUser.getUserID());
            
            model.addAttribute("user", currentUser);
            model.addAttribute("supportIssues", supportIssues);
            model.addAttribute("totalTickets", totalTickets);
            model.addAttribute("openTickets", openTickets);
            
            System.out.println("Support tickets loaded for user: " + currentUser.getEmail());
            System.out.println("Total tickets: " + totalTickets + ", Open tickets: " + openTickets);
            
            return "customer-support-tickets";
            
        } catch (Exception e) {
            System.err.println("Error loading support tickets: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/customer/dashboard";
        }
    }
    
    // View specific support ticket
    @GetMapping("/ticket/{id}")
    public String viewSupportTicket(@PathVariable Long id, Model model) {
        System.out.println("=== Support Ticket Detail Request: " + id + " ===");
        
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get support issue
            SupportIssue supportIssue = supportIssueService.getSupportIssueById(id).orElse(null);
            
            if (supportIssue == null || !supportIssue.getCustomerId().equals(currentUser.getUserID())) {
                return "redirect:/customer/support/tickets";
            }
            
            model.addAttribute("user", currentUser);
            model.addAttribute("supportIssue", supportIssue);
            
            return "customer-support-detail";
            
        } catch (Exception e) {
            System.err.println("Error loading support ticket detail: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/customer/support/tickets";
        }
    }
    
    // Helper method to get current user
    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            
            // Try to get user from database by email/username
            return userService.getAllUsers().stream()
                .filter(user -> user.getEmail().equals(username))
                .findFirst()
                .orElse(null);
        }
        return null;
    }
}
