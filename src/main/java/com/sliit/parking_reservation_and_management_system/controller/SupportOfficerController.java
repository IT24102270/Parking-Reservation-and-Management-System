package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.SupportIssue;
import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.entity.Feedback;
import com.sliit.parking_reservation_and_management_system.service.SupportIssueService;
import com.sliit.parking_reservation_and_management_system.service.ReservationService;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import com.sliit.parking_reservation_and_management_system.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/support")
public class SupportOfficerController {
    
    @Autowired
    private SupportIssueService supportIssueService;
    
    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FeedbackService feedbackService;
    
    // Support Officer Dashboard
    @GetMapping("/dashboard")
    public String showSupportOfficerDashboard(Model model) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user is support officer
            if (!"CUSTOMER_SUPPORT_OFFICER".equals(currentUser.getRole())) {
                return "redirect:/login?error=access_denied";
            }
            
            // Get all support tickets statistics
            List<SupportIssue> allTickets = supportIssueService.getAllSupportIssues();
            long totalTickets = allTickets.size();
            long openTickets = allTickets.stream().filter(ticket -> "OPEN".equals(ticket.getStatus())).count();
            long inProgressTickets = allTickets.stream().filter(ticket -> "IN_PROGRESS".equals(ticket.getStatus())).count();
            long resolvedTickets = allTickets.stream().filter(ticket -> "RESOLVED".equals(ticket.getStatus())).count();
            
            // Get recent tickets (last 10)
            List<SupportIssue> recentTickets = allTickets.stream()
                .sorted((a, b) -> b.getRaisedDate().compareTo(a.getRaisedDate()))
                .limit(10)
                .toList();
            
            // Get booking statistics
            List<Reservation> allReservations = reservationService.getAllReservations();
            long totalBookings = allReservations.size();
            long activeBookings = allReservations.stream().filter(res -> "ACTIVE".equals(res.getStatus())).count();
            long pendingBookings = allReservations.stream().filter(res -> "PENDING".equals(res.getStatus())).count();
            
            model.addAttribute("user", currentUser);
            model.addAttribute("totalTickets", totalTickets);
            model.addAttribute("openTickets", openTickets);
            model.addAttribute("inProgressTickets", inProgressTickets);
            model.addAttribute("resolvedTickets", resolvedTickets);
            model.addAttribute("recentTickets", recentTickets);
            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("activeBookings", activeBookings);
            model.addAttribute("pendingBookings", pendingBookings);
            
            return "support-officer-dashboard";
            
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
    
    // View All Support Tickets
    @GetMapping("/tickets")
    public String viewAllSupportTickets(Model model,
                                      @RequestParam(value = "status", required = false) String status,
                                      @RequestParam(value = "search", required = false) String search) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null || !"CUSTOMER_SUPPORT_OFFICER".equals(currentUser.getRole())) {
                return "redirect:/login";
            }
            
            List<SupportIssue> tickets = supportIssueService.getAllSupportIssues();
            
            // Filter by status if provided
            if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
                tickets = tickets.stream()
                    .filter(ticket -> status.equals(ticket.getStatus()))
                    .toList();
            }
            
            // Filter by search term if provided
            if (search != null && !search.isEmpty()) {
                tickets = tickets.stream()
                    .filter(ticket -> 
                        ticket.getDescription().toLowerCase().contains(search.toLowerCase()) ||
                        ticket.getTicketId().toLowerCase().contains(search.toLowerCase()))
                    .toList();
            }
            
            model.addAttribute("user", currentUser);
            model.addAttribute("tickets", tickets);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("searchTerm", search);
            
            return "support-officer-tickets";
            
        } catch (Exception e) {
            return "redirect:/support/dashboard";
        }
    }
    
    // View Specific Support Ticket
    @GetMapping("/ticket/{id}")
    public String viewSupportTicket(@PathVariable Long id, Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null || !"CUSTOMER_SUPPORT_OFFICER".equals(currentUser.getRole())) {
                return "redirect:/login";
            }
            
            Optional<SupportIssue> ticketOpt = supportIssueService.getSupportIssueById(id);
            if (!ticketOpt.isPresent()) {
                return "redirect:/support/tickets";
            }
            
            SupportIssue ticket = ticketOpt.get();
            User customer = userService.getUserById(ticket.getCustomerId()).orElse(null);
            
            model.addAttribute("user", currentUser);
            model.addAttribute("ticket", ticket);
            model.addAttribute("customer", customer);
            
            return "support-officer-ticket-detail";
            
        } catch (Exception e) {
            return "redirect:/support/tickets";
        }
    }
    
    // Update Support Ticket Status
    @PostMapping("/ticket/{id}/update")
    public String updateTicketStatus(@PathVariable Long id,
                                   @RequestParam("status") String status,
                                   @RequestParam(value = "response", required = false) String response,
                                   RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null || !"CUSTOMER_SUPPORT_OFFICER".equals(currentUser.getRole())) {
                return "redirect:/login";
            }
            
            Optional<SupportIssue> ticketOpt = supportIssueService.getSupportIssueById(id);
            if (!ticketOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Support ticket not found");
                return "redirect:/support/tickets";
            }
            
            SupportIssue ticket = ticketOpt.get();
            ticket.setStatus(status);
            
            // Add response to description if provided
            if (response != null && !response.trim().isEmpty()) {
                String updatedDescription = ticket.getDescription() + 
                    "\n\n--- SUPPORT RESPONSE (" + LocalDateTime.now() + ") ---\n" + response;
                ticket.setDescription(updatedDescription);
            }
            
            supportIssueService.updateSupportIssue(ticket);
            
            redirectAttributes.addFlashAttribute("success", 
                "Support ticket #" + ticket.getTicketId() + " updated successfully");
            
            return "redirect:/support/ticket/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating support ticket");
            return "redirect:/support/tickets";
        }
    }
    
    // View All Bookings/Reservations
    @GetMapping("/bookings")
    public String viewAllBookings(Model model,
                                @RequestParam(value = "status", required = false) String status,
                                @RequestParam(value = "search", required = false) String search) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null || !"CUSTOMER_SUPPORT_OFFICER".equals(currentUser.getRole())) {
                return "redirect:/login";
            }
            
            List<Reservation> bookings = reservationService.getAllReservations();
            
            // Filter by status if provided
            if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
                bookings = bookings.stream()
                    .filter(booking -> status.equals(booking.getStatus()))
                    .toList();
            }
            
            // Filter by search term if provided (vehicle number or booking ID)
            if (search != null && !search.isEmpty()) {
                bookings = bookings.stream()
                    .filter(booking -> 
                        (booking.getVehicleNumber() != null && 
                         booking.getVehicleNumber().toLowerCase().contains(search.toLowerCase())) ||
                        booking.getBookingId().toLowerCase().contains(search.toLowerCase()))
                    .toList();
            }
            
            model.addAttribute("user", currentUser);
            model.addAttribute("bookings", bookings);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("searchTerm", search);
            
            return "support-officer-bookings";
            
        } catch (Exception e) {
            return "redirect:/support/dashboard";
        }
    }
    
    // View Specific Booking
    @GetMapping("/booking/{id}")
    public String viewBooking(@PathVariable Long id, Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null || !"CUSTOMER_SUPPORT_OFFICER".equals(currentUser.getRole())) {
                return "redirect:/login";
            }
            
            Optional<Reservation> bookingOpt = reservationService.getReservationById(id);
            if (!bookingOpt.isPresent()) {
                return "redirect:/support/bookings";
            }
            
            Reservation booking = bookingOpt.get();
            User customer = userService.getUserById(booking.getUserId()).orElse(null);
            
            model.addAttribute("user", currentUser);
            model.addAttribute("booking", booking);
            model.addAttribute("customer", customer);
            
            return "support-officer-booking-detail";
            
        } catch (Exception e) {
            return "redirect:/support/bookings";
        }
    }
    
    // Update Booking Status
    @PostMapping("/booking/{id}/update")
    public String updateBookingStatus(@PathVariable Long id,
                                    @RequestParam("status") String status,
                                    @RequestParam(value = "vehicleNumber", required = false) String vehicleNumber,
                                    RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null || !"CUSTOMER_SUPPORT_OFFICER".equals(currentUser.getRole())) {
                return "redirect:/login";
            }
            
            Optional<Reservation> bookingOpt = reservationService.getReservationById(id);
            if (!bookingOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Booking not found");
                return "redirect:/support/bookings";
            }
            
            Reservation booking = bookingOpt.get();
            booking.setStatus(status);
            
            if (vehicleNumber != null && !vehicleNumber.trim().isEmpty()) {
                booking.setVehicleNumber(vehicleNumber.trim());
            }
            
            reservationService.updateReservation(booking);
            
            redirectAttributes.addFlashAttribute("success", 
                "Booking #" + booking.getBookingId() + " updated successfully");
            
            return "redirect:/support/booking/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating booking");
            return "redirect:/support/bookings";
        }
    }
    
    // Customer Feedback Management
    @GetMapping("/feedback")
    public String showCustomerFeedback(Model model,
                                     @RequestParam(value = "rating", required = false) String rating,
                                     @RequestParam(value = "status", required = false) String status,
                                     @RequestParam(value = "search", required = false) String search) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user is support officer
            if (!"CUSTOMER_SUPPORT_OFFICER".equals(currentUser.getRole())) {
                return "redirect:/login?error=access_denied";
            }
            
            // Get all feedback from database
            List<Feedback> allFeedback = feedbackService.getAllFeedback();
            
            // Apply filters
            List<Feedback> filteredFeedback = allFeedback.stream()
                .filter(feedback -> {
                    // Filter by rating
                    if (rating != null && !rating.isEmpty()) {
                        try {
                            int ratingFilter = Integer.parseInt(rating);
                            if (feedback.getRating() != ratingFilter) {
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            // Invalid rating filter, ignore
                        }
                    }
                    
                    // Filter by search term in comment
                    if (search != null && !search.trim().isEmpty()) {
                        String searchLower = search.toLowerCase().trim();
                        String comment = feedback.getComment();
                        if (comment == null || !comment.toLowerCase().contains(searchLower)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .toList();
            
            // Calculate statistics from real data
            long totalFeedback = feedbackService.getTotalFeedbackCount();
            long positiveFeedback = feedbackService.getFeedbackCountByRating(4) + 
                                  feedbackService.getFeedbackCountByRating(5);
            long negativeFeedback = feedbackService.getFeedbackCountByRating(1) + 
                                  feedbackService.getFeedbackCountByRating(2);
            long neutralFeedback = feedbackService.getFeedbackCountByRating(3);
            
            // Get customer information for each feedback
            List<User> allUsers = userService.getAllUsers();
            
            model.addAttribute("user", currentUser);
            model.addAttribute("feedbackList", filteredFeedback);
            model.addAttribute("allUsers", allUsers);
            model.addAttribute("totalFeedback", totalFeedback);
            model.addAttribute("positiveFeedback", positiveFeedback);
            model.addAttribute("negativeFeedback", negativeFeedback);
            model.addAttribute("neutralFeedback", neutralFeedback);
            model.addAttribute("averageRating", feedbackService.getAverageRating());
            
            // Add filter parameters back to model
            model.addAttribute("selectedRating", rating);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("searchQuery", search);
            
            return "support-officer-feedback";
            
        } catch (Exception e) {
            System.err.println("Error loading feedback: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/support/dashboard";
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
